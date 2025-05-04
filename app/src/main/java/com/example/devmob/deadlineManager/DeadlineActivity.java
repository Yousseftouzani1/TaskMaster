package com.example.devmob.deadlineManager;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.devmob.R;
import com.example.devmob.Task;
import com.example.devmob.TaskAdapter;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DeadlineActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private List<Task> deadlineList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deadline);

        recyclerView = findViewById(R.id.deadlineRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        deadlineList = new ArrayList<>();

        adapter = new TaskAdapter(task -> {
            // Open details when item clicked
        }, deadlineList);
        recyclerView.setAdapter(adapter);

        loadDeadlineTasks();
    }

    private void loadDeadlineTasks() {
        DatabaseReference tasksRef = FirebaseDatabase.getInstance().getReference("tasks");

        tasksRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                deadlineList.clear();
                for (DataSnapshot taskSnapshot : snapshot.getChildren()) {
                    Task task = taskSnapshot.getValue(Task.class);
                    if (task != null && task.getDueDate() > 0 && !task.getfinished()) {
                        task.setId(taskSnapshot.getKey());
                        deadlineList.add(task);
                    }
                }

                // Sort by due date ascending
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