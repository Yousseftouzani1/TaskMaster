package com.example.devmob;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
public class TaskDetailActivity extends AppCompatActivity {

    private TextView taskTitle, taskStatus, taskPriority, taskDueDate, taskDescription;
    private ProgressBar taskProgressBar;
    private ChipGroup taskTags;
    private Button finishedbutton;

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

        taskProgressBar = findViewById(R.id.taskProgressBar);
        taskTags = findViewById(R.id.taskTags);
        finishedbutton = findViewById(R.id.finished);
        String taskId = getIntent().getStringExtra("taskId");

        // Receive task data from intent
        String title = getIntent().getStringExtra("title");
        String description = getIntent().getStringExtra("description");
        String status = getIntent().getStringExtra("task_status");
        String priority = getIntent().getStringExtra("task_priority");
        long dueDateMillis = getIntent().getLongExtra("dueDate", 0);
        int progress = getIntent().getIntExtra("progressPercent", 0);
        List<String> tags = getIntent().getStringArrayListExtra("tags");

        // Populate views
        taskTitle.setText(title != null ? title : "No title");
        taskStatus.setText("Status: " + (status != null ? status : "Unknown"));
        taskPriority.setText(priority != null ? priority : "Normal");
        taskDescription.setText(description != null ? description : "No description");



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
        finishedbutton.setOnClickListener(v -> {
            if (taskId != null && !taskId.isEmpty()) {
                DatabaseReference taskRef = FirebaseDatabase.getInstance()
                        .getReference("tasks")
                        .child(taskId);
                // Get today's date
                String today = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date());
                taskRef.child("finishedDate").setValue(today);
                taskRef.child("isfinished").setValue(true)
                        .addOnSuccessListener(aVoid -> {
                            finishedbutton.setText("Task marked as done");
                            Toast.makeText(this, "Task successfully marked as done", Toast.LENGTH_SHORT).show();
                            finishedbutton.setEnabled(false); // Immediately disable to avoid multiple clicks
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            finishedbutton.setText("Failed to update");
                        });

            } else {
                finishedbutton.setText("No task ID");
            }
        });

    }
}
