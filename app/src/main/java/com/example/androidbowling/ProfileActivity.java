package com.example.androidbowling;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidbowling.adapter.ReservationAdapter;
import com.example.androidbowling.model.Reservation;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private TextView profileName, profileEmail, emptyReservationsText;
    private RecyclerView reservationsRecyclerView;
    private ReservationAdapter adapter;
    private List<Reservation> reservationList;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profileName = findViewById(R.id.profileName);
        profileEmail = findViewById(R.id.profileEmail);
        emptyReservationsText = findViewById(R.id.emptyReservationsText);
        reservationsRecyclerView = findViewById(R.id.reservationsRecyclerView);

        MaterialButton btnNewReservation = findViewById(R.id.btnNewReservation);
        MaterialButton btnLogout = findViewById(R.id.btnLogout);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Nem vagy bejelentkezve!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        profileEmail.setText("Email: " + currentUser.getEmail());

        db.collection("User")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        profileName.setText("Név: " + name);
                    } else {
                        profileName.setText("Név: (nem található)");
                    }
                })
                .addOnFailureListener(e -> profileName.setText("Név: (hiba)"));

        reservationList = new ArrayList<>();
        adapter = new ReservationAdapter(reservationList);
        reservationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        reservationsRecyclerView.setAdapter(adapter);

        db.collection("Reservation")
                .whereEqualTo("id", currentUser.getUid())
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    if (querySnapshots.isEmpty()) {
                        emptyReservationsText.setVisibility(View.VISIBLE);
                    } else {
                        emptyReservationsText.setVisibility(View.GONE);
                        for (QueryDocumentSnapshot document : querySnapshots) {
                            Reservation reservation = new Reservation();
                            reservation.setDate(document.getString("date"));
                            reservation.setId(document.getString("id"));
                            reservation.setLane(document.getString("track"));
                            reservationList.add(reservation);
                        }
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Hiba a foglalások betöltésekor.", Toast.LENGTH_SHORT).show();
                });

        btnNewReservation.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, ReservationActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            auth.signOut();
            Toast.makeText(this, "Kijelentkezve", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
            finish();
        });
    }
}
