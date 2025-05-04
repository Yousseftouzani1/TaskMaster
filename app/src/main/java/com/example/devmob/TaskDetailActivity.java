package com.example.devmob;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskDetailActivity extends AppCompatActivity {

    private TextView taskTitle, taskStatus, taskPriority, taskDueDate, taskDescription, taskFeedback;
    private ProgressBar taskProgressBar;
    private ChipGroup taskTags;
    private Button joinMeetingButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        // Bind views
        taskTitle = findViewById(R.id.taskTitle);
        taskStatus = findViewById(R.id.taskStatus);
        taskPriority = findViewById(R.id.taskPriority);
        taskDueDate = findViewById(R.id.taskDueDate);
        taskDescription = findViewById(R.id.taskDescription);
        taskFeedback = findViewById(R.id.taskFeedback);
        taskProgressBar = findViewById(R.id.taskProgressBar);
        taskTags = findViewById(R.id.taskTags);
        joinMeetingButton = findViewById(R.id.finished);

        // Receive task data from intent
        String title = getIntent().getStringExtra("title");
        String description = getIntent().getStringExtra("description");
        String status = getIntent().getStringExtra("task_status");
        String priority = getIntent().getStringExtra("task_priority");
        long dueDateMillis = getIntent().getLongExtra("dueDate", 0);
        int progress = getIntent().getIntExtra("progressPercent", 0);
        String feedback = getIntent().getStringExtra("feedback");
        List<String> tags = getIntent().getStringArrayListExtra("tags");

        // Populate views
        taskTitle.setText(title != null ? title : "No title");
        taskStatus.setText("Status: " + (status != null ? status : "Unknown"));
        taskPriority.setText(priority != null ? priority : "Normal");
        taskDescription.setText(description != null ? description : "No description");

        if (feedback != null && !feedback.isEmpty()) {
            taskFeedback.setText(feedback);
        } else {
            taskFeedback.setText("No feedback available");
        }

        // Format and set due date
        if (dueDateMillis > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            taskDueDate.setText("Due: " + sdf.format(new Date(dueDateMillis)));
        }

        // Set progress
        taskProgressBar.setProgress(progress);

        // Dynamically add tag chips
        if (tags != null && !tags.isEmpty()) {
            for (String tag : tags) {
                Chip chip = new Chip(this);
                chip.setText(tag);
                chip.setChipBackgroundColorResource(R.color.fond_background);
                chip.setTextColor(getResources().getColor(android.R.color.white));
                chip.setClickable(false);
                chip.setCheckable(false);
                taskTags.addView(chip);
            }
        }

        // Handle finish button click (for now just disable)
        joinMeetingButton.setOnClickListener(v -> {
            joinMeetingButton.setEnabled(false);
            joinMeetingButton.setText("Marked as Done");
        });
    }
}
