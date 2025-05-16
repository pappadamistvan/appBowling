package com.example.androidbowling;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class ReservationActivity extends AppCompatActivity {

    private static final String TAG = "ReservationActivity";

    private TextInputLayout nameInputLayout, emailInputLayout;
    private TextInputEditText nameEditText, emailEditText;
    private Button datePickerButton, reserveButton;
    private Spinner timeSpinner;
    private TextView selectedDateTimeTextView;
    private RadioGroup laneRadioGroup;

    private Calendar selectedCalendar = Calendar.getInstance();
    private boolean isDateSelected = false;
    private boolean isTimeSelected = false;

    // Firestore adatbázis
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation);

        // Firestore inicializálása
        db = FirebaseFirestore.getInstance();


        initializeViews();
        setupTimeSpinner();
        setupEventListeners();
    }

    private void initializeViews() {
        nameInputLayout = findViewById(R.id.nameInputLayout);
        emailInputLayout = findViewById(R.id.emailInputLayout);
        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        datePickerButton = findViewById(R.id.datePickerButton);
        timeSpinner = findViewById(R.id.timeSpinner);
        selectedDateTimeTextView = findViewById(R.id.selectedDateTimeTextView);
        laneRadioGroup = findViewById(R.id.laneRadioGroup);
        reserveButton = findViewById(R.id.reserveButton);
    }

    private void setupTimeSpinner() {
        String[] hours = new String[13];
        for (int i = 0; i < 13; i++) {
            hours[i] = String.format(Locale.getDefault(), "%02d:00", 10 + i);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, hours);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeSpinner.setAdapter(adapter);
    }

    private void setupEventListeners() {
        datePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        reserveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAndSubmitReservation();
            }
        });
    }

    private void showDatePickerDialog() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                ReservationActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        selectedCalendar.set(Calendar.YEAR, year);
                        selectedCalendar.set(Calendar.MONTH, monthOfYear);
                        selectedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        isDateSelected = true;
                        updateSelectedDateTimeText();
                    }
                },
                year, month, day);

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void updateSelectedDateTimeText() {
        if (isDateSelected && isTimeSelected) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy. MM. dd. HH:mm", Locale.getDefault());
            String formattedDateTime = dateFormat.format(selectedCalendar.getTime());
            selectedDateTimeTextView.setText(formattedDateTime);
        } else if (isDateSelected) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy. MM. dd.", Locale.getDefault());
            selectedDateTimeTextView.setText(dateFormat.format(selectedCalendar.getTime()) + " (idő kiválasztása szükséges)");
        } else {
            selectedDateTimeTextView.setText("Nincs kiválasztva időpont");
        }
    }

    private void validateAndSubmitReservation() {
        boolean isValid = true;
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            nameInputLayout.setError("A név megadása kötelező");
            isValid = false;
        } else {
            nameInputLayout.setError(null);
        }

        if (TextUtils.isEmpty(email)) {
            emailInputLayout.setError("Az email megadása kötelező");
            isValid = false;
        } else if (!isValidEmail(email)) {
            emailInputLayout.setError("Érvénytelen email cím");
            isValid = false;
        } else {
            emailInputLayout.setError(null);
        }

        if (!isDateSelected) {
            Toast.makeText(this, "Kérjük, válasszon dátumot", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        // Spinnerből kiválasztott időpont beállítása
        String selectedTime = (String) timeSpinner.getSelectedItem();
        if (selectedTime != null && selectedTime.contains(":")) {
            int hour = Integer.parseInt(selectedTime.split(":")[0]);
            selectedCalendar.set(Calendar.HOUR_OF_DAY, hour);
            selectedCalendar.set(Calendar.MINUTE, 0);
            isTimeSelected = true;
        } else {
            isTimeSelected = false;
            Toast.makeText(this, "Kérjük, válasszon időpontot", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        if (laneRadioGroup.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Kérjük, válasszon pályát", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        if (isValid) {
            updateSelectedDateTimeText();
            saveReservationToFirestore(name, email);
        }
    }

    private void saveReservationToFirestore(String name, String email) {
        // Timestamp létrehozása a kiválasztott dátum és időpont alapján
        SimpleDateFormat fullDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String formattedDate = fullDateFormat.format(selectedCalendar.getTime());

        // Kiválasztott pálya számának megszerzése
        int selectedLaneId = laneRadioGroup.getCheckedRadioButtonId();
        RadioButton selectedLaneButton = findViewById(selectedLaneId);
        String laneText = selectedLaneButton.getText().toString();
        String trackNumber = laneText.split("-")[0].trim(); // Például "1-es pálya" esetén kivesszük az "1"-et

        // Egyedi azonosító generálása a foglaláshoz
        String reservationId = UUID.randomUUID().toString();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Foglalási adatok létrehozása
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("date", formattedDate);
        reservation.put("id", currentUser.getUid());
        reservation.put("track", trackNumber);

        // Adatok mentése a Firestore-ba
        db.collection("Reservation")
                .document(reservationId)
                .set(reservation)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Foglalás sikeres: " + reservationId);
                        Toast.makeText(ReservationActivity.this, "Foglalás sikeres!", Toast.LENGTH_LONG).show();
                        clearFormFields();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Hiba történt a foglalás mentése közben", e);
                        Toast.makeText(ReservationActivity.this, "Hiba történt a foglalás során: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    // Foglalás után a mezők törlése
    private void clearFormFields() {
        nameEditText.setText("");
        emailEditText.setText("");
        laneRadioGroup.clearCheck();
        isDateSelected = false;
        isTimeSelected = false;
        selectedDateTimeTextView.setText("Nincs kiválasztva időpont");
        timeSpinner.setSelection(0);
    }

    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}