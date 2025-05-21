package com.example.devmob;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalendarActivity extends AppCompatActivity {

    private MaterialCalendarView calendarView;
    private RecyclerView tasksRecyclerView;
    private TaskAdapter adapter;
    private final Map<CalendarDay, List<Task>> tasksByDate = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        calendarView      = findViewById(R.id.calendarView);
        tasksRecyclerView = findViewById(R.id.tasksRecyclerView);

        // Setup RecyclerView with empty list first
        tasksRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TaskAdapter(task -> {
            // handle click if needed
        }, new ArrayList<>());
        tasksRecyclerView.setAdapter(adapter);

        // Load only calendar-synced & unfinished tasks
        fetchTasksFromFirebase();

        // When a date is tapped, show only that day's tasks
        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            List<Task> dayTasks = tasksByDate.getOrDefault(date, new ArrayList<>());
            adapter = new TaskAdapter(task -> {
                // handle click…
            }, dayTasks);
            tasksRecyclerView.setAdapter(adapter);
        });
    }

    private void fetchTasksFromFirebase() {
        // Server-side: only tasks with calendarSync == true
        Query query = FirebaseDatabase.getInstance()
                .getReference("tasks")
                .orderByChild("isfinished")
                .equalTo(false);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                tasksByDate.clear();
                Collection<CalendarDay> eventDates = new ArrayList<>();

                for (DataSnapshot snap : snapshot.getChildren()) {
                    Task task = snap.getValue(Task.class);
                    if (task == null) continue;

                    // Client-side: skip finished tasks
                    boolean finished = task.getfinished()
                            || task.getProgressPercent() >= 100;
                    if (finished) continue;

                    long dueMs = task.getDueDate();
                    if (dueMs <= 0) continue;

                    CalendarDay day = CalendarDay.from(new Date(dueMs));
                    eventDates.add(day);

                    tasksByDate
                            .computeIfAbsent(day, d -> new ArrayList<>())
                            .add(task);
                }

                // Decorate dates that have (unfinished & synced) tasks
                calendarView.addDecorator(new EventDecorator(
                        Color.parseColor("#2E7D32"),
                        eventDates
                ));
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {
                // handle error if you like
            }
        });
    }

    /** Draws a little dot under each date in 'dates'. */
    private static class EventDecorator implements DayViewDecorator {
        private final int color;
        private final Collection<CalendarDay> dates;

        EventDecorator(int color, Collection<CalendarDay> dates) {
            this.color = color;
            this.dates = dates;
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return dates.contains(day);
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.addSpan(new DotSpan(8, color));
        }
    }
}
