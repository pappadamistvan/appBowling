package com.example.androidbowling;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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

public class RegistrationActivity extends AppCompatActivity {

    private static final String LOG_TAG = RegistrationActivity.class.getName();

    EditText userNameEditText;
    EditText userEmailEditText;
    EditText userPasswordEditText;
    EditText userPasswordConfirmEditText;

    private FirebaseAuth mAuth;


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

        mAuth = FirebaseAuth.getInstance();
    }

    public void register(View view) {
        String email = userEmailEditText.getText().toString();
        String password = userPasswordEditText.getText().toString();
        String passwordConfirm = userPasswordConfirmEditText.getText().toString();

        if (!password.equals(passwordConfirm)){
            Log.e(LOG_TAG, "Nem egyenlő a jelsző és a megerősítés!");
        }

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    Log.d(LOG_TAG, "User created succesfully");
                } else {
                    Log.d(LOG_TAG, "User was not created succesfully" + task.getException().getMessage());
                    Toast.makeText(RegistrationActivity.this, "User was not created succesfully: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}