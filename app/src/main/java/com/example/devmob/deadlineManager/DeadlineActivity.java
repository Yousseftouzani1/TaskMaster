package com.example.devmob.deadlineManager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.devmob.R;
import com.example.devmob.Task;
import com.example.devmob.TaskDetailActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DeadlineActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TaskDeadlineAdapter adapter;
    private List<Task> deadlineList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deadline);

        recyclerView = findViewById(R.id.deadlineRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        deadlineList = new ArrayList<>();

        adapter = new TaskDeadlineAdapter(task -> {
            Intent intent = new Intent(DeadlineActivity.this, TaskDetailActivity.class);
            intent.putExtra("title", task.getTitle());
            intent.putExtra("description", task.getDescription());
            intent.putExtra("task_status", task.getStatus());
            intent.putExtra("task_priority", task.getPriorityLevel());
            intent.putExtra("dueDate", task.getDueDate());
            intent.putExtra("progressPercent", task.getProgressPercent());
            intent.putExtra("feedback", task.getUserFeedback());
            intent.putStringArrayListExtra("tags", task.getTags() != null ? new ArrayList<>(task.getTags()) : new ArrayList<>());
            startActivity(intent);
        }, deadlineList);

        recyclerView.setAdapter(adapter);

        loadDeadlineTasks();
    }

    private void loadDeadlineTasks() {
        Query tasksquery=FirebaseDatabase.getInstance().getReference("tasks").orderByChild("isfinished").equalTo(false);
        tasksquery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                deadlineList.clear();
                for (DataSnapshot taskSnapshot : snapshot.getChildren()) {
                    Task task = taskSnapshot.getValue(Task.class);

                    // Validate task fields
                    if (task != null && task.getDueDate() > 0 && !Boolean.TRUE.equals(task.getFinishedDate())) {
                        task.setId(taskSnapshot.getKey());
                        deadlineList.add(task);
                    }
                }

                // Sort by due date
                Collections.sort(deadlineList, Comparator.comparingLong(Task::getDueDate));
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DeadlineActivity.this, "Failed to load deadlines: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
