package com.example.androidproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = findViewById(R.id.map_button);
        button.setOnClickListener(view -> {
            Intent intent = new Intent(this, Map.class);
            startActivity(intent);
        });
    }

    public void startSearch(View view) {
        Intent intent = new Intent(this, SearchPage.class);
        startActivity(intent);
    }
}