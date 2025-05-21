package com.example.devmob;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EditTaskActivity extends AppCompatActivity {
    private TextInputEditText titleEditText;
    private TextInputEditText dueDateEditText;
    private Slider progressSlider;
    private TextView progressText;        // Correction : TextView au lieu de TextInputEditText
    private SwitchMaterial notificationsSwitch;
    private AutoCompleteTextView notificationsPerDayDropdown;
    private AutoCompleteTextView reminderOffsetDropdown;
    private AutoCompleteTextView snoozeDurationDropdown;
    private MaterialButton saveButton;

    private Calendar calendar;
    private SimpleDateFormat dateFormat;
    private Task currentTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task2);

        // 1) Bind views
        titleEditText               = findViewById(R.id.title_edit_text);
        dueDateEditText             = findViewById(R.id.due_date_edit_text);
        progressSlider              = findViewById(R.id.progress_slider);
        progressText                = findViewById(R.id.progress_text);
        notificationsSwitch         = findViewById(R.id.notifications_switch);
        notificationsPerDayDropdown = findViewById(R.id.notifications_per_day_dropdown);
        reminderOffsetDropdown      = findViewById(R.id.reminder_offset_dropdown);
        snoozeDurationDropdown      = findViewById(R.id.snooze_duration_dropdown);
        saveButton                  = findViewById(R.id.save_button);

        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        // 2) Setup dropdown adapters
        String[] perDayOptions = {"1","2","3","4"};
        notificationsPerDayDropdown.setAdapter(
                new ArrayAdapter<>(this,
                        android.R.layout.simple_dropdown_item_1line,
                        perDayOptions)
        );

        String[] offsetOptions = {"2 heures avant","4 heures avant","6 heures avant"};
        reminderOffsetDropdown.setAdapter(
                new ArrayAdapter<>(this,
                        android.R.layout.simple_dropdown_item_1line,
                        offsetOptions)
        );

        String[] snoozeOptions = {"5 minutes","10 minutes","15 minutes","20 minutes"};
        snoozeDurationDropdown.setAdapter(
                new ArrayAdapter<>(this,
                        android.R.layout.simple_dropdown_item_1line,
                        snoozeOptions)
        );

        // 3) Retrieve Task from Intent
        String taskId = getIntent().getStringExtra("taskId");
        if (taskId == null) {
            Toast.makeText(this, "ID de tâche invalide", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 4) Load existing Task from Firebase
        FirebaseDatabase.getInstance()
                .getReference("tasks")
                .child(taskId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snap) {
                        currentTask = snap.getValue(Task.class);
                        if (currentTask == null) {
                            Toast.makeText(EditTaskActivity.this, "Tâche introuvable", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                        populateFields();
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) {
                        Toast.makeText(EditTaskActivity.this, "Erreur lecture: "+e.getMessage(), Toast.LENGTH_LONG).show();
                        finish();
                    }
                });

        // 5) DatePicker
        dueDateEditText.setOnClickListener(v -> {
            DatePickerDialog dpd = new DatePickerDialog(
                    EditTaskActivity.this,
                    (view, y, m, d) -> {
                        calendar.set(y, m, d);
                        dueDateEditText.setText(dateFormat.format(calendar.getTime()));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            dpd.show();
        });

        // 6) Slider listener
        progressSlider.addOnChangeListener((slider, value, fromUser) -> {
            int p = Math.round(value);
            progressText.setText(p + "%");
        });

        // 7) Save button
        saveButton.setOnClickListener(v -> saveTask(taskId));
    }

    private void populateFields() {
        // Title hint
        titleEditText.setHint(currentTask.getTitle());

        // Due date hint (suppression du != null sur un primitive long)
        if (currentTask.getDueDate() > 0) {
            String fmt = dateFormat.format(new Date(currentTask.getDueDate()));
            dueDateEditText.setHint(fmt);
            calendar.setTimeInMillis(currentTask.getDueDate());
        }

        // Progress
        progressSlider.setValue(currentTask.getProgressPercent());
        progressText.setText(currentTask.getProgressPercent() + "%");

        // Notifications
        notificationsSwitch.setChecked(currentTask.isNotificationsEnabled());
        notificationsPerDayDropdown.setText(
                String.valueOf(currentTask.getNotificationsPerDay()),
                false
        );
        reminderOffsetDropdown.setText(
                formatOffset(currentTask.getReminderOffsetMillis()),
                false
        );
        snoozeDurationDropdown.setText(
                currentTask.getSnoozeMinutes() + " minutes",
                false
        );
    }

    private void saveTask(String taskId) {
        // Title
        String newTitle = titleEditText.getText().toString().trim();
        if (newTitle.isEmpty()) {
            newTitle = currentTask.getTitle();
        }

        // Due date
        String dueInput = dueDateEditText.getText().toString().trim();
        long newDue = currentTask.getDueDate();
        if (!dueInput.isEmpty()) {
            newDue = calendar.getTimeInMillis();
        }

        // Progress
        int newProg = Math.round(progressSlider.getValue());

        // Notifications
        boolean newNotify = notificationsSwitch.isChecked();

        String perDayInput = notificationsPerDayDropdown.getText().toString().trim();
        int newPerDay = perDayInput.isEmpty()
                ? currentTask.getNotificationsPerDay()
                : Integer.parseInt(perDayInput);

        String offInput = reminderOffsetDropdown.getText().toString().trim();
        long newOffset = offInput.isEmpty()
                ? currentTask.getReminderOffsetMillis()
                : parseOffset(offInput);

        String snoozeInput = snoozeDurationDropdown.getText().toString().trim();
        int newSnooze = snoozeInput.isEmpty()
                ? currentTask.getSnoozeMinutes()
                : Integer.parseInt(snoozeInput.split(" ")[0]);

        // Build updates map
        Map<String,Object> updates = new HashMap<>();
        updates.put("title", newTitle);
        updates.put("dueDate", newDue);
        updates.put("progressPercent", newProg);
        updates.put("notificationsEnabled", newNotify);
        updates.put("notificationsPerDay", newPerDay);
        updates.put("reminderOffsetMillis", newOffset);
        updates.put("snoozeMinutes", newSnooze);
        updates.put("lastUpdatedDate", new Date().getTime());

        // Apply to Firebase
        FirebaseDatabase.getInstance()
                .getReference("tasks")
                .child(taskId)
                .updateChildren(updates)
                .addOnSuccessListener(a -> {
                    Toast.makeText(this, "Tâche mise à jour", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erreur: "+e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private String formatOffset(long millis) {
        return (millis / 3600000) + " heures avant";
    }
    private long parseOffset(String s) {
        return Long.parseLong(s.split(" ")[0]) * 3600000L;
    }
}