// src/main/java/com/example/devmob/notifications/TaskNotifyWorker.java
package com.example.devmob.notif;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.devmob.R;
import com.example.devmob.Task;
import com.google.firebase.database.FirebaseDatabase;

public class TaskNotifyWorker extends Worker {
    public static final String KEY_TASK_ID = "taskId";

    public TaskNotifyWorker(@NonNull Context context,
                            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull @Override
    public Result doWork() {
        String taskId = getInputData().getString(KEY_TASK_ID);
        if (taskId == null) return Result.failure();

        // Récupère la tâche stockée dans Firebase
        FirebaseDatabase.getInstance()
                .getReference("tasks")
                .child(taskId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    Task task = snapshot.getValue(Task.class);
                    if (task != null && task.isNotificationsEnabled()) {
                        showNotification(task);
                    }
                })
                .addOnFailureListener(e -> {
                    // Ignorer, le travail est tout de même considéré comme réussi
                });

        return Result.success();
    }

    private void showNotification(Task task) {
        Context ctx = getApplicationContext();
        String channelId = task.getNotificationChannelId();
        NotificationManager nm = ctx.getSystemService(NotificationManager.class);

        // Création du channel si besoin
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (nm.getNotificationChannel(channelId) == null) {
                nm.createNotificationChannel(new NotificationChannel(
                        channelId,
                        "Rappels de tâches",
                        NotificationManager.IMPORTANCE_DEFAULT
                ));
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, channelId)
                .setSmallIcon(R.drawable.ic_notification)          // votre icône
                .setContentTitle("Rappel : " + task.getTitle())
                .setContentText(task.getDescription() != null
                        ? task.getDescription()
                        : "Vous avez une tâche à accomplir")
                .setAutoCancel(true);

        // Chaque notification doit avoir un ID unique
        int notifId = task.getId().hashCode();
        nm.notify(notifId, builder.build());
    }
}
