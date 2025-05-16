package com.example.androidbowling;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Layout;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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
    private Layout formLayout;
    private EditText emailField, passwordField;
    private TextView appName, registrationLink;
    private Button loginButton;
    private ProgressBar progressBar;

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
        appName = findViewById(R.id.registrationTextView);
        registrationLink = findViewById(R.id.registerLink);
        progressBar = findViewById(R.id.progressBar);
        loginButton = findViewById(R.id.loginButton);

        mAuth = FirebaseAuth.getInstance();

        com.google.firebase.FirebaseApp.initializeApp(this);
    }

    public void login(View view){
        String userEmail = this.emailField.getText().toString();
        String userPassword = this.passwordField.getText().toString();

        if (userEmail.isEmpty() || userPassword.isEmpty()){
            Toast.makeText(LoginActivity.this, "Üres adatmezők", Toast.LENGTH_LONG).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    Toast.makeText(LoginActivity.this, "Sikeres bejelentkezés", Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.VISIBLE);
                    emailField.setVisibility(View.GONE);
                    passwordField.setVisibility(View.GONE);
                    appName.setVisibility(View.GONE);
                    registrationLink.setVisibility(View.GONE);
                    loginButton.setVisibility(View.GONE);
                    findViewById(R.id.formLayout).setVisibility(View.GONE);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                            mainPage();
                        }
                    }, 2000); // 2 másodperc "töltés"

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