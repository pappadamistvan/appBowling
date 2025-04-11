package com.example.androidbowling;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText emailField, passwordField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loginLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        emailField = findViewById(R.id.loginEmailField);
        passwordField = findViewById(R.id.loginPasswordField);

        mAuth = FirebaseAuth.getInstance();

        com.google.firebase.FirebaseApp.initializeApp(this);
    }

    public void login(View view){
        String userEmail = this.emailField.getText().toString();
        String userPassword = this.passwordField.getText().toString();

        mAuth.signInWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    Toast.makeText(LoginActivity.this, "Sikeres bejelentkezés", Toast.LENGTH_LONG).show();
                    mainPage();
                } else {
                    Toast.makeText(LoginActivity.this, "Hibás felhasználónév vagy jelszó", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void mainPage() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void register(View view){
        Intent intent = new Intent(this, RegistrationActivity.class);
        startActivity(intent);
    }
}