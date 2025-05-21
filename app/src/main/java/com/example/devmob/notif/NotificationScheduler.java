// src/main/java/com/example/devmob/notifications/NotificationScheduler.java
package com.example.devmob.notif;

import android.content.Context;

import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.devmob.Task;
import com.example.devmob.notif.TaskNotifyWorker;

import java.util.concurrent.TimeUnit;

public class NotificationScheduler {

    /**
     * Planifie (ou re-planifie) les rappels pour une tâche.
     * @param context Contexte Android
     * @param task    Objet Task contenant tous les réglages
     */
    public static void schedule(Context context, Task task) {
        WorkManager wm = WorkManager.getInstance(context);
        String baseTag = "notify-" + task.getId();

        // 1) Annule d'abord tout travail existant pour cette tâche
        wm.cancelAllWorkByTag(baseTag);

        // 2) Si notifications désactivées, on s'arrête là
        if (!task.isNotificationsEnabled()) return;

        long dueMs   = task.getDueDate();
        long offset  = task.getReminderOffsetMillis(); // en ms
        int perDay   = task.getNotificationsPerDay();

        // Par exemple, on répartit les rappels entre 8 h et 20 h
        int startHour = 8;
        int endHour   = 20;
        long windowMs = (endHour - startHour) * 3_600_000L;

        for (int i = 0; i < perDay; i++) {
            long offsetInWindow = windowMs * i / perDay;
            long triggerAt      = dueMs - offset + startHour * 3_600_000L + offsetInWindow;
            long delayMs        = Math.max(triggerAt - System.currentTimeMillis(), 0);

            Data input = new Data.Builder()
                    .putString(TaskNotifyWorker.KEY_TASK_ID, task.getId())
                    .build();

            OneTimeWorkRequest req = new OneTimeWorkRequest.Builder(TaskNotifyWorker.class)
                    .setInputData(input)
                    .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                    .addTag(baseTag)
                    .build();

            // On utilise enqueueUniqueWork pour écraser si déjà existant
            wm.enqueueUniqueWork(
                    baseTag + "-" + i,
                    ExistingWorkPolicy.REPLACE,
                    req
            );
        }
    }
}
