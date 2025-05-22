package com.example.devmob;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Get references to views
        ImageView logo = findViewById(R.id.homeLogo);
        TextView title = findViewById(R.id.homeTitle);
        TextView subtitle = findViewById(R.id.homeSubtitle);
        Button getStartedButton = findViewById(R.id.getStartedButton);

        // Load and apply animations
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        logo.startAnimation(fadeIn);
        title.startAnimation(fadeIn);
        subtitle.startAnimation(fadeIn);

        // Get the user ID from the intent
        String userId = getIntent().getStringExtra("USER_UID");

        // Set up Get Started button
        getStartedButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, TaskListActivity.class);
            intent.putExtra("USER_UID", userId);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        });
    }
} 