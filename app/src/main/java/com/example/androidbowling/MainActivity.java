package com.example.androidbowling;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button btnNewReservation, btnProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnNewReservation = findViewById(R.id.btnNewReservation);
        btnProfile = findViewById(R.id.btnProfile);

        btnNewReservation.setOnClickListener(v -> {
            startActivity(new Intent(this, ReservationActivity.class));
        });

        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        });

        Intent intent = new Intent(MainActivity.this, ReservationActivity.class);
        startActivity(intent);
        overridePendingTransition(R.transition.slide_in_right, R.transition.slide_out_left);
    }
}
