package com.example.devmob;
import android.Manifest;

import android.content.pm.PackageManager;

import androidx.core.content.ContextCompat;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class TaskCreationBottomSheet extends BottomSheetDialogFragment {

    private TextInputEditText editTitle, editDescription, editTimeSpent, editTagInput;
    private EditText editDueDate;
    private Calendar selectedDate;
    private String formattedDate;
    private Slider seekProgress;
    private MaterialSwitch switchReminder, switchCalendar;
    private MaterialButton btnSave, btnAddTag;
    private ChipGroup chipGroupTags;


    private final ArrayList<String> tags = new ArrayList<>();
    private static final int PERMISSION_REQUEST_CODE = 100;

    private boolean checkCalendarPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{
                    Manifest.permission.WRITE_CALENDAR,
                    Manifest.permission.READ_CALENDAR
            }, PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }

    private long getPrimaryCalendarId() {
        String[] projection = new String[]{CalendarContract.Calendars._ID, CalendarContract.Calendars.CALENDAR_DISPLAY_NAME};
        Cursor cursor = requireActivity().getContentResolver().query(
                CalendarContract.Calendars.CONTENT_URI,
                projection,
                CalendarContract.Calendars.VISIBLE + " = 1 AND " +
                        CalendarContract.Calendars.IS_PRIMARY + "=1",
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            long id = cursor.getLong(0);
            cursor.close();
            return id;
        }

        // fallback: use first visible calendar
        if (cursor != null && cursor.moveToFirst()) {
            long id = cursor.getLong(0);
            cursor.close();
            return id;
        }

        return -1;
    }



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        checkCalendarPermission();

        View view = inflater.inflate(R.layout.bottom_sheet_task_creation, container, false);


        // Bind views
        editTitle = view.findViewById(R.id.edit_title);
        editDescription = view.findViewById(R.id.edit_description);
        editTimeSpent = view.findViewById(R.id.edit_time_spent);
        editTagInput = view.findViewById(R.id.edit_tag_input);
        editDueDate = view.findViewById(R.id.edit_due_date);

        editDueDate.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    requireContext(),
                    (view1, selectedYear, selectedMonth, selectedDay) -> {
                        selectedDate = Calendar.getInstance();
                        selectedDate.set(selectedYear, selectedMonth, selectedDay);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        formattedDate = sdf.format(selectedDate.getTime());
                        editDueDate.setText(formattedDate);
                    },
                    year, month, day
            );

            datePickerDialog.show();
        });

        seekProgress = view.findViewById(R.id.seek_progress);
        switchReminder = view.findViewById(R.id.switch_reminder);
        switchCalendar = view.findViewById(R.id.switch_calendar_sync);

        chipGroupTags = view.findViewById(R.id.chip_group_tags);
        btnAddTag = view.findViewById(R.id.btn_add_tag);
        btnSave = view.findViewById(R.id.btn_save);
        MaterialAutoCompleteTextView spinnerRecurrence = view.findViewById(R.id.spinner_recurrence);
        MaterialAutoCompleteTextView spinnerPriority = view.findViewById(R.id.spinner_priority);

        // Recurrence options
        String[] recurrenceOptions = new String[]{
                "Aucune", "Quotidienne", "Hebdomadaire", "Mensuelle", "Annuelle"
        };
        ArrayAdapter<String> recurrenceAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_dropdown_item_1line, recurrenceOptions);
        spinnerRecurrence.setAdapter(recurrenceAdapter);

        // Priority options
        String[] priorityOptions = new String[]{
                "Basse", "Moyenne", "Haute", "Critique"
        };
        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_dropdown_item_1line, priorityOptions);
        spinnerPriority.setAdapter(priorityAdapter);

        spinnerRecurrence.setOnClickListener(v -> spinnerRecurrence.showDropDown());
        spinnerPriority.setOnClickListener(v -> spinnerPriority.showDropDown());

        btnAddTag.setOnClickListener(v -> {
            String tagText = editTagInput.getText().toString().trim();
            if (!tagText.isEmpty()) {
                tags.add(tagText);
                Chip chip = new Chip(getContext());
                chip.setText(tagText);
                chip.setCloseIconVisible(true);
                chip.setOnCloseIconClickListener(view1 -> {
                    chipGroupTags.removeView(chip);
                    tags.remove(tagText);
                });
                chipGroupTags.addView(chip);
                editTagInput.setText("");
            }
        });

        btnSave.setOnClickListener(v -> {
            String title = editTitle.getText().toString().trim();
            String description = editDescription.getText().toString().trim();
            int progress = (int) seekProgress.getValue();
            boolean reminder = switchReminder.isChecked();
            boolean calendar = switchCalendar.isChecked();
            int timeSpent = getIntFromEditText(editTimeSpent);

            if (title.isEmpty()) {
                Toast.makeText(getContext(), "title required !", Toast.LENGTH_SHORT).show();
                return;
            }

            // Firebase save
            DatabaseReference tasksRef = FirebaseDatabase.getInstance().getReference("tasks");
            String taskId = tasksRef.push().getKey();

            Map<String, Object> taskData = new HashMap<>();
            String recurrence = spinnerRecurrence.getText().toString().trim();
            String priority = spinnerPriority.getText().toString().trim();

            taskData.put("recurrence", recurrence);
            taskData.put("priority", priority);
            taskData.put("id", taskId);
            taskData.put("title", title);
            taskData.put("description", description);
            taskData.put("progressPercent", progress);
            taskData.put("reminderEnabled", reminder);
            taskData.put("calendarSync", calendar);
            taskData.put("timeSpent", timeSpent);
            taskData.put("tags", tags);
            taskData.put("createdAt", new Date().getTime());
            taskData.put("dueDate", selectedDate != null ? selectedDate.getTimeInMillis() : null);
            taskData.put("isfinished", false);
            taskData.put("finishedDate", 124589);

            assert taskId != null;
            tasksRef.child(taskId).setValue(taskData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Tâche créée !", Toast.LENGTH_SHORT).show();

                        if (calendar && selectedDate != null) {
                            addTaskToGoogleCalendar(title, description, selectedDate.getTimeInMillis());
                        }

                        dismiss();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Erreur : " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        });

        return view;
    }

    private int getIntFromEditText(TextInputEditText editText) {
        try {
            return Integer.parseInt(editText.getText().toString().trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "Calendar permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Calendar permission denied. Cannot sync tasks to calendar.", Toast.LENGTH_LONG).show();
                // You might want to disable calendar sync switch or inform user
                if (switchCalendar != null) {
                    switchCalendar.setChecked(false);
                    switchCalendar.setEnabled(false);
                }
            }
        }
    }


    private void addTaskToGoogleCalendar(String title, String description, long dueTimeMillis) {
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.DTSTART, dueTimeMillis);
        values.put(CalendarContract.Events.DTEND, dueTimeMillis + 60 * 60 * 1000); // 1-hour duration
        values.put(CalendarContract.Events.TITLE, title);
        values.put(CalendarContract.Events.DESCRIPTION, description);
        values.put(CalendarContract.Events.CALENDAR_ID, 1); // default calendar
        values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());
        long calendarId = getPrimaryCalendarId();
        if (calendarId == -1) {
            Toast.makeText(getContext(), "Aucun calendrier trouvé", Toast.LENGTH_SHORT).show();
            return;
        }
        values.put(CalendarContract.Events.CALENDAR_ID, calendarId);


        Uri uri = requireActivity().getContentResolver().insert(CalendarContract.Events.CONTENT_URI, values);
        if (uri != null) {
            Toast.makeText(getContext(), "Tâche synchronisée avec Google Calendar", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Échec de la synchronisation avec Calendar", Toast.LENGTH_SHORT).show();
        }
    }

}
