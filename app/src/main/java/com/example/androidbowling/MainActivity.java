package com.example.androidbowling;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
    }
}
