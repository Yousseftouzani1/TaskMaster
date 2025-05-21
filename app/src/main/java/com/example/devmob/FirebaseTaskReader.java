package com.example.devmob;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class FirebaseTaskReader {

    public interface OnStringResult {
        void onSuccess(String result);
        void onFailure(String error);
    }

    /**
     * Fetches tasks as JSON string for a specific Firebase Query
     * (e.g. already filtered by userId, or here by isfinished).
     */
    public static void fetchTasksAsString(Query tasksQuery, OnStringResult listener) {
        tasksQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                JsonArray jsonArray = new JsonArray();
                for (DataSnapshot taskSnap : snapshot.getChildren()) {
                    Task task = taskSnap.getValue(Task.class);
                    if (task != null) {
                        JsonObject taskJson = new JsonObject();
                        taskJson.addProperty("title",       task.getTitle());
                        taskJson.addProperty("description", task.getDescription());
                        taskJson.addProperty("priority",    task.getPriorityLevel());
                        taskJson.addProperty("status",      task.getStatus());
                        taskJson.addProperty("progress",    task.getProgressPercent());
                        taskJson.addProperty("dueDate",     task.getDueDate());
                        jsonArray.add(taskJson);
                    }
                }
                listener.onSuccess(jsonArray.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onFailure("Firebase error: " + error.getMessage());
            }
        });
    }

    /**
     * Convenience: fetches only the unfinished tasks (/tasks where isfinished==false)
     * as a JSON string.
     */
    public static void fetchTasksAsString(OnStringResult listener) {
        Query q = FirebaseDatabase.getInstance()
                .getReference("tasks")
                .orderByChild("isfinished")
                .equalTo(false);

        fetchTasksAsString(q, listener);
    }
}
