package com.example.devmob;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
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
    private View aiCard;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_airesponse);
        aiCard = findViewById(R.id.ai_card); // <-- initialize it
        aiSuggestionTextView = findViewById(R.id.ai_suggestion);
        progressBar = findViewById(R.id.progress_bar);
// get the tasks from the realtime databse firebase
        progressBar.setVisibility(View.VISIBLE);

        FirebaseTaskReader.fetchTasksAsString(new FirebaseTaskReader.OnStringResult() {
            @Override
            public void onSuccess(String result) {
                // This is your plain Java String
                Log.d("TASK_JSON_STRING", result);
                String taskPrompt1 = "Here are my tasks:\n" +
                        result+ "What should I do first and why? give me a plan using PERT logic in few clear lines ";
                // You can also show it in a TextView if needed
                AIRequest.getSmartSuggestion(taskPrompt1, new AIRequest.SuggestionCallback() {
                    @Override
                    public void onSuccess(String suggestion) {
                        progressBar.setVisibility(View.GONE);
                        aiCard.setVisibility(View.VISIBLE);
                        aiSuggestionTextView.setText(suggestion);
                    }

                    @Override
                    public void onFailure(String error) {
                        progressBar.setVisibility(View.GONE);
                        aiSuggestionTextView.setText(error);
                    }
                });
                // myTextView.setText(result);
            }

            @Override
            public void onFailure(String error) {
                Log.e("TASK_FETCH_ERROR", error);
            }
        });




//send the request through to the api

    }
}
