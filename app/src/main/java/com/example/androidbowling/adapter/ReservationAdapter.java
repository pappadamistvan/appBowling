package com.example.androidbowling.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidbowling.EditReservationActivity;
import com.example.androidbowling.R;
import com.example.androidbowling.model.Reservation;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReservationAdapter extends RecyclerView.Adapter<ReservationAdapter.ReservationViewHolder> {

    private List<Reservation> reservationList;
    private Context context;

    public ReservationAdapter(List<Reservation> reservationList) {
        this.reservationList = reservationList;
    }

    @NonNull
    @Override
    public ReservationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_reservation, parent, false);
        return new ReservationViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservationViewHolder holder, int position) {
        Reservation res = reservationList.get(position);
        String formattedDate = res.getDate();
        String finalDate = formattedDate;

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd - HH:mm", Locale.getDefault());
            Date parsedDate = inputFormat.parse(formattedDate);
            finalDate = outputFormat.format(parsedDate);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String track = res.getLane();
        String reservationText = finalDate + " - Pálya " + track;
        holder.infoText.setText(reservationText);

        // Gombok működése
        holder.btnDelete.setOnClickListener(v -> {
            if (canModifyOrDelete(res.getDate())) {
                confirmDelete(res);
            } else {
                Toast.makeText(context, "A foglalást csak 24 órával előtte lehet törölni.", Toast.LENGTH_SHORT).show();
            }
        });

        holder.btnEdit.setOnClickListener(v -> {
            if (canModifyOrDelete(res.getDate())) {
                Intent intent = new Intent(context, EditReservationActivity.class);
                intent.putExtra("reservationId", "fake"); // opcionális, ha lenne külön ID-d
                intent.putExtra("date", res.getDate());
                intent.putExtra("lane", res.getLane());
                intent.putExtra("userId", res.getId());
                context.startActivity(intent);
            } else {
                Toast.makeText(context, "A foglalást csak 24 órával előtte lehet módosítani.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return reservationList.size();
    }

    public static class ReservationViewHolder extends RecyclerView.ViewHolder {
        TextView infoText;
        Button btnEdit, btnDelete;

        public ReservationViewHolder(@NonNull View itemView) {
            super(itemView);
            infoText = itemView.findViewById(R.id.reservationInfo);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    private boolean canModifyOrDelete(String dateString) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        try {
            Date reservationDate = sdf.parse(dateString);
            Date now = new Date();
            long diff = reservationDate.getTime() - now.getTime();
            return diff >= 24 * 60 * 60 * 1000; // legalább 24 óra
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }


    private void confirmDelete(Reservation reservation) {
        new AlertDialog.Builder(context)
                .setTitle("Törlés megerősítése")
                .setMessage("Biztosan törlöd ezt a foglalást?")
                .setPositiveButton("Igen", (dialog, which) -> deleteReservation(reservation))
                .setNegativeButton("Mégse", null)
                .show();
    }

    private void deleteReservation(Reservation reservation) {
        FirebaseFirestore.getInstance()
                .collection("Reservation")
                .whereEqualTo("id", reservation.getId())
                .whereEqualTo("date", reservation.getDate())
                .whereEqualTo("track", reservation.getLane())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        doc.getReference().delete();
                        Toast.makeText(context, "Foglalás törölve", Toast.LENGTH_SHORT).show();
                        reservationList.remove(reservation);
                        notifyDataSetChanged();
                        break;
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Hiba történt a törlés során.", Toast.LENGTH_SHORT).show();
                });
    }
}
