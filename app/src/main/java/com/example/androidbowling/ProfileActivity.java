package com.example.androidbowling;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.TextView;

import com.example.androidbowling.adapter.ReservationAdapter;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidbowling.model.Reservation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private TextView profileName, profileEmail;
    private RecyclerView reservationsRecyclerView;
    private ReservationAdapter adapter;
    private List<Reservation> reservationList;

    private FirebaseFirestore db;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profileName = findViewById(R.id.profileName);
        profileEmail = findViewById(R.id.profileEmail);
        reservationsRecyclerView = findViewById(R.id.reservationsRecyclerView);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            profileEmail.setText("Email: " + currentUser.getEmail());
            profileName.setText("Felhasználó UID: " + currentUser.getUid());

            // Firestore inicializálása és lekérdezés
            db = FirebaseFirestore.getInstance();
            reservationList = new ArrayList<>();
            adapter = new ReservationAdapter(reservationList);
            reservationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            reservationsRecyclerView.setAdapter(adapter);

            db.collection("Reservation")
                    .whereEqualTo("id", currentUser.getUid())
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String date = document.getString("date");
                            String id = document.getString("id");
                            String track = document.getString("track");

                            Reservation reservation = new Reservation();
                            reservation.setDate(date);
                            reservation.setId(id);
                            reservation.setLane(track);

                            reservationList.add(reservation);
                        }
                        adapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> {
                        // hiba kezelése (pl. log, Toast)
                    });
        } else {
            profileEmail.setText("Nincs bejelentkezve");
            profileName.setText("");
        }
    }
}
