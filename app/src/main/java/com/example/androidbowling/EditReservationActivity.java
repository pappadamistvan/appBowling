package com.example.androidbowling;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.*;

public class EditReservationActivity extends AppCompatActivity {

    private TextView currentInfo, selectedDateText;
    private Button pickDateButton, saveButton, cancelButton;
    private AutoCompleteTextView hourSpinner, laneSpinner;
    private String currentDate, currentLane, userId;
    private Calendar newDateTime;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_reservation);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        currentInfo = findViewById(R.id.currentInfo);
        selectedDateText = findViewById(R.id.selectedDateText);
        pickDateButton = findViewById(R.id.pickDateButton);
        hourSpinner = findViewById(R.id.hourSpinner);
        laneSpinner = findViewById(R.id.laneSpinner);
        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);

        currentDate = getIntent().getStringExtra("date");
        currentLane = getIntent().getStringExtra("lane");
        userId = getIntent().getStringExtra("userId");

        currentInfo.setText("Eredeti foglalás: " + currentDate + " - Pálya " + currentLane);
        newDateTime = Calendar.getInstance();

        List<String> lanes = Arrays.asList("1", "2", "3", "4");
        ArrayAdapter<String> laneAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, lanes);
        laneSpinner.setAdapter(laneAdapter);
        laneSpinner.setText(currentLane, false);

        List<String> hours = new ArrayList<>();
        for (int i = 10; i <= 22; i++) {
            hours.add(String.format("%02d:00", i));
        }
        ArrayAdapter<String> hourAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, hours);
        hourSpinner.setAdapter(hourAdapter);

        pickDateButton.setOnClickListener(v -> showDatePicker());

        saveButton.setOnClickListener(v -> {
            String selectedHourStr = hourSpinner.getText().toString(); // Pl. "10:00"
            String[] hourParts = selectedHourStr.split(":");

            if (hourParts.length == 2) {
                int hour = Integer.parseInt(hourParts[0]);
                int minute = Integer.parseInt(hourParts[1]);

                newDateTime.set(Calendar.HOUR_OF_DAY, hour);
                newDateTime.set(Calendar.MINUTE, minute);
                newDateTime.set(Calendar.SECOND, 0);
                newDateTime.set(Calendar.MILLISECOND, 0);
            }

            if (isValid()) {
                updateReservationInFirestore();
            } else {
                Toast.makeText(this, "Csak 24 órán túli időpontra lehet módosítani.", Toast.LENGTH_SHORT).show();
            }
        });


        cancelButton.setOnClickListener(v -> finish());
    }

    private void showDatePicker() {
        Calendar now = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            newDateTime.set(Calendar.YEAR, year);
            newDateTime.set(Calendar.MONTH, month);
            newDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            selectedDateText.setText(format.format(newDateTime.getTime()));
        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show();
    }

    private boolean isValid() {
        Calendar now = Calendar.getInstance();
        long diff = newDateTime.getTimeInMillis() - now.getTimeInMillis();
        return diff >= 24 * 60 * 60 * 1000;
    }

    private void updateReservationInFirestore() {
        String hour = hourSpinner.getText().toString();
        if (hour.isEmpty()) {
            Toast.makeText(this, "Kérlek válassz ki egy órát!", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] timeParts = hour.split(":");
        newDateTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeParts[0]));
        newDateTime.set(Calendar.MINUTE, 0);
        newDateTime.set(Calendar.SECOND, 0);

        String newDateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(newDateTime.getTime());
        String newLane = laneSpinner.getText().toString();

        if (newDateStr.equals(currentDate) && newLane.equals(currentLane)) {
            Toast.makeText(this, "Nincs változás a foglaláson.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("Reservation")
                .whereEqualTo("date", newDateStr)
                .whereEqualTo("track", newLane)
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (!snapshots.isEmpty()) {
                        Toast.makeText(this, "Ez az időpont és pálya már foglalt!", Toast.LENGTH_SHORT).show();
                    } else {
                        // Módosítjuk az eredeti foglalást
                        FirebaseFirestore.getInstance()
                                .collection("Reservation")
                                .whereEqualTo("id", userId)
                                .whereEqualTo("date", currentDate)
                                .whereEqualTo("track", currentLane)
                                .get()
                                .addOnSuccessListener(originalSnapshots -> {
                                    if (!originalSnapshots.isEmpty()) {
                                        originalSnapshots.getDocuments().get(0).getReference()
                                                .update("date", newDateStr, "track", newLane)
                                                .addOnSuccessListener(aVoid -> {
                                                    Toast.makeText(this, "Sikeres módosítás", Toast.LENGTH_SHORT).show();
                                                    finish();
                                                })
                                                .addOnFailureListener(e -> Toast.makeText(this, "Sikertelen módosítás", Toast.LENGTH_SHORT).show());
                                    } else {
                                        Toast.makeText(this, "Nem található foglalás.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Hiba az ellenőrzés során.", Toast.LENGTH_SHORT).show());
    }

}
