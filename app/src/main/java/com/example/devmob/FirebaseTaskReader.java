package com.example.devmob;

import androidx.annotation.NonNull;

import com.google.firebase.database.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class FirebaseTaskReader {

    public interface OnStringResult {
        void onSuccess(String result);
        void onFailure(String error);
    }

    public static void fetchTasksAsString(OnStringResult listener) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("tasks");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                JsonArray jsonArray = new JsonArray();

                for (DataSnapshot taskSnap : snapshot.getChildren()) {
                    Task task = taskSnap.getValue(Task.class);
                    if (task != null) {
                        JsonObject taskJson = new JsonObject();
                        taskJson.addProperty("title", task.getTitle());
                        taskJson.addProperty("description", task.getDescription());
                        taskJson.addProperty("priority", task.getPriorityLevel());
                        taskJson.addProperty("status", task.getStatus());
                        taskJson.addProperty("progress", task.getProgressPercent());
                        taskJson.addProperty("dueDate", task.getDueDate());
                        jsonArray.add(taskJson);
                    }
                }

                String resultString = jsonArray.toString(); // ← ✅ converted to plain Java String
                listener.onSuccess(resultString);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onFailure("Firebase error: " + error.getMessage());
            }
        });
    }
}

