package com.example.androidbowling;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import com.example.androidbowling.model.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseUser;


public class RegistrationActivity extends AppCompatActivity {

    private static final String LOG_TAG = RegistrationActivity.class.getName();


    private ProgressBar progressBar;
    private Layout formLayout;
    EditText userNameEditText;
    EditText userEmailEditText;
    EditText userPasswordEditText;
    EditText userPasswordConfirmEditText;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registration);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        userNameEditText = findViewById(R.id.nameField);
        userEmailEditText = findViewById(R.id.emailField);
        userPasswordEditText = findViewById(R.id.passwordField);
        userPasswordConfirmEditText = findViewById(R.id.confirmPasswordField);
        progressBar = findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

    }

    public void register(View view) {
        String email = userEmailEditText.getText().toString();
        String password = userPasswordEditText.getText().toString();
        String passwordConfirm = userPasswordConfirmEditText.getText().toString();
        String name = userNameEditText.getText().toString();

        if (name.length() < 5){
            Log.e(LOG_TAG, "Érvénytelen név!Legalább 5 karakter)");
            Toast.makeText(RegistrationActivity.this, "Érvénytelen név!\n(Legalább 5 karakter)", Toast.LENGTH_LONG).show();
            return;
        }
        if (!password.equals(passwordConfirm)){
            Log.e(LOG_TAG, "Nem egyenlő a jelsző és a megerősítés!");
            Toast.makeText(RegistrationActivity.this, "Nem egyezik a jelszó a megerősítéssel", Toast.LENGTH_LONG).show();
            return;
        }
        else {
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Log.d(LOG_TAG, "Sikeres regisztráció");

                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String uid = firebaseUser.getUid();
                            String email = firebaseUser.getEmail();

                            User user = new User(name, email);
                            user.setId(uid); // Beállítjuk manuálisan az ID-t

                            db.collection("User").document(uid)
                                    .set(user)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(LOG_TAG, "Felhasználó mentve Firestore-ba");
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(LOG_TAG, "Hiba a Firestore mentésnél", e);
                                    });
                        }

                        Toast.makeText(RegistrationActivity.this, "Sikeres regisztráció!", Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.VISIBLE);
                        findViewById(R.id.formLayout).setVisibility(View.GONE);

                        new Handler().postDelayed(() -> {
                            progressBar.setVisibility(View.GONE);
                            mainPage();
                        }, 2000);
                    } else {
                        Log.d(LOG_TAG, "User was not created succesfully" + task.getException().getMessage());
                        Toast.makeText(RegistrationActivity.this, "Sikertelen regisztráció: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    public void mainPage() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}