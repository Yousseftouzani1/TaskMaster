package com.example.devmob;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AIResponseActivity extends AppCompatActivity {

    private TextView aiSuggestionTextView;
    private ProgressBar progressBar;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_airesponse);

        aiSuggestionTextView = findViewById(R.id.ai_suggestion);
        progressBar = findViewById(R.id.progress_bar);

        String taskPrompt = "Here are my tasks:\n" +
                "1. Finish AI report (due tomorrow, high priority)\n" +
                "2. Water the plants (no due date, low priority)\n" +
                "3. Revise math notes (due in 2 days, medium priority).\n" +
                "What should I do first and why?";

        progressBar.setVisibility(View.VISIBLE);

        AIRequest.getSmartSuggestion(taskPrompt, new AIRequest.SuggestionCallback() {
            @Override
            public void onSuccess(String suggestion) {
                progressBar.setVisibility(View.GONE);
                aiSuggestionTextView.setText(suggestion);
            }

            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                aiSuggestionTextView.setText(error);
            }
        });
    }
}
