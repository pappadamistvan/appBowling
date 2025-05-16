package com.example.androidbowling;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class PlaceholderActivity extends AppCompatActivity {

    public static final String EXTRA_TITLE = "extra_title";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_placeholder);

        TextView tvTitle = findViewById(R.id.tvPlaceholderTitle);

        String title = getIntent().getStringExtra(EXTRA_TITLE);
        tvTitle.setText(title != null ? title : "Helyettesítő oldal");
    }
}
