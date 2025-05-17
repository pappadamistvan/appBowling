package com.example.androidbowling;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
    private MaterialButton datePickerButton, reserveButton;
    private AutoCompleteTextView timeSpinner;
    private TextView selectedDateTimeTextView;
    private RadioGroup laneRadioGroup;

    private Calendar selectedCalendar = Calendar.getInstance();
    private boolean isDateSelected = false;
    private boolean isTimeSelected = false;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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
        reserveButton = findViewById(R.id.reserveButton);
        timeSpinner = findViewById(R.id.timeSpinner);
        selectedDateTimeTextView = findViewById(R.id.selectedDateTimeTextView);
        laneRadioGroup = findViewById(R.id.laneRadioGroup);
    }

    private void setupTimeSpinner() {
        String[] hours = new String[13];
        for (int i = 0; i < 13; i++) {
            hours[i] = String.format(Locale.getDefault(), "%02d:00", 10 + i);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, hours);
        timeSpinner.setAdapter(adapter);

        timeSpinner.setOnItemClickListener((parent, view, position, id) -> {
            String selectedTime = (String) parent.getItemAtPosition(position);
            if (selectedTime != null && selectedTime.contains(":")) {
                int hour = Integer.parseInt(selectedTime.split(":")[0]);
                selectedCalendar.set(Calendar.HOUR_OF_DAY, hour);
                selectedCalendar.set(Calendar.MINUTE, 0);
                isTimeSelected = true;
                updateSelectedDateTimeText();
            }
        });
    }

    private void setupEventListeners() {
        datePickerButton.setOnClickListener(v -> {
            int year = selectedCalendar.get(Calendar.YEAR);
            int month = selectedCalendar.get(Calendar.MONTH);
            int day = selectedCalendar.get(Calendar.DAY_OF_MONTH);

            new DatePickerDialog(this, (view, y, m, d) -> {
                selectedCalendar.set(Calendar.YEAR, y);
                selectedCalendar.set(Calendar.MONTH, m);
                selectedCalendar.set(Calendar.DAY_OF_MONTH, d);
                isDateSelected = true;
                updateSelectedDateTimeText();
            }, year, month, day).show();
        });

        reserveButton.setOnClickListener(v -> handleReservation());
    }

    private void updateSelectedDateTimeText() {
        if (isDateSelected && isTimeSelected) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            String formatted = sdf.format(selectedCalendar.getTime());
            selectedDateTimeTextView.setText("Kiválasztott időpont: " + formatted);
        }
    }

    private void handleReservation() {
        String name = nameEditText.getText() != null ? nameEditText.getText().toString().trim() : "";
        String email = emailEditText.getText() != null ? emailEditText.getText().toString().trim() : "";

        int selectedLaneId = laneRadioGroup.getCheckedRadioButtonId();
        if (TextUtils.isEmpty(name)) {
            nameInputLayout.setError("Név megadása kötelező");
            return;
        } else {
            nameInputLayout.setError(null);
        }

        if (TextUtils.isEmpty(email)) {
            emailInputLayout.setError("Email megadása kötelező");
            return;
        } else {
            emailInputLayout.setError(null);
        }

        if (!isDateSelected || !isTimeSelected) {
            Toast.makeText(this, "Kérlek válassz dátumot és időpontot!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedLaneId == -1) {
            Toast.makeText(this, "Kérlek válassz egy pályát!", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedRadioButton = findViewById(selectedLaneId);
        String lane = selectedRadioButton.getText().toString();
        String[] cuttedLane = lane.split("-");

        // Dátum/idő string formátumban
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        String dateTime = sdf.format(selectedCalendar.getTime());

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user != null ? user.getUid() : "guest";

        String reservationId = UUID.randomUUID().toString();

        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", name);
        reservation.put("email", email);
        reservation.put("date", dateTime);
        reservation.put("track", cuttedLane[0]);
        reservation.put("id", userId);

        db.collection("Reservation")
                .whereEqualTo("date", dateTime)
                .whereEqualTo("track", cuttedLane[0])
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(this, "Erre az időpontra és pályára már van foglalás!", Toast.LENGTH_SHORT).show();
                    } else {
                        db.collection("Reservation").document(reservationId)
                                .set(reservation)
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(this, "Foglalás sikeres!", Toast.LENGTH_LONG).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Foglalás sikertelen", e);
                                    Toast.makeText(this, "Hiba történt a foglalás során", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Hiba történt az ellenőrzés során", Toast.LENGTH_SHORT).show();
                });

    }
}
