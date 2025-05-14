package com.example.devmob;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AIResponseActivity extends AppCompatActivity {

    private TextView aiSuggestionTextView;
    private ProgressBar progressBar;
    private View aiCard;
    private ScrollView aiScroll; // parent of ai_card

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_airesponse);

        // Bind views
        aiCard = findViewById(R.id.ai_card);
        aiScroll = findViewById(R.id.ai_scroll);
        aiSuggestionTextView = findViewById(R.id.ai_suggestion);
        progressBar = findViewById(R.id.progress_bar);


        aiScroll.setVisibility(View.GONE);
        aiCard.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        // fetch tasks from Firebase
        FirebaseTaskReader.fetchTasksAsString(new FirebaseTaskReader.OnStringResult() {
            @Override
            public void onSuccess(String result) {
                Log.d("TASK_JSON_STRING", result);

                String prompt = "Here are my tasks:\n" +
                        result + "\nWhat should I do first and why? Give me a plan using PERT logic in a few clear lines.";

                try {
                    AIRequest.getSmartSuggestion(prompt, new AIRequest.SuggestionCallback() {
                        @Override
                        public void onSuccess(String suggestion) {
                            progressBar.setVisibility(View.GONE);
                            aiScroll.setVisibility(View.VISIBLE);
                            aiCard.setVisibility(View.VISIBLE);

                            if (suggestion != null && !suggestion.trim().isEmpty()) {
                                aiSuggestionTextView.setText(suggestion);
                            } else {
                                aiSuggestionTextView.setText("No valid response from AI.");
                            }
                        }

                        @Override
                        public void onFailure(String error) {
                            progressBar.setVisibility(View.GONE);
                            aiScroll.setVisibility(View.VISIBLE);   //  Show error within the same scroll/card
                            aiCard.setVisibility(View.VISIBLE);
                            aiSuggestionTextView.setText("AI Error: " + error);
                        }
                    });
                } catch (Exception e) {
                    progressBar.setVisibility(View.GONE);
                    aiScroll.setVisibility(View.VISIBLE);
                    aiCard.setVisibility(View.VISIBLE);
                    aiSuggestionTextView.setText("Exception during AI request.");
                    Log.e("AI_CALL_ERROR", "Exception while calling AI", e);
                }
            }

            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                aiScroll.setVisibility(View.VISIBLE);
                aiCard.setVisibility(View.VISIBLE);
                aiSuggestionTextView.setText("Failed to fetch tasks: " + error);
                Log.e("TASK_FETCH_ERROR", error);
            }
        });
    }
}
