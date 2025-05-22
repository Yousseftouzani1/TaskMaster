package com.example.devmob;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.devmob.deadlineManager.DeadlineActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalendarActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private MaterialCalendarView calendarView;
    private RecyclerView tasksRecyclerView;
    private TaskAdapter adapter;
    private final Map<CalendarDay, List<Task>> tasksByDate = new HashMap<>();
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private ActionBarDrawerToggle toggle;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Setup toolbar and drawer
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Update navigation header
        updateNavigationHeader();

        // Set navigation item click listener
        navigationView.setNavigationItemSelectedListener(this);

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

    private void updateNavigationHeader() {
        View headerView = navigationView.getHeaderView(0);
        TextView headerEmail = headerView.findViewById(R.id.nav_header_email);
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String email = user.getEmail();
            headerEmail.setText((email != null && !email.isEmpty()) ? email : "");
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_tasks) {
            startActivity(new Intent(this, TaskListActivity.class));
        } else if (id == R.id.nav_calendar) {
            // Already in calendar, just close drawer
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (id == R.id.nav_deadline) {
            startActivity(new Intent(this, DeadlineActivity.class));
        } else if (id == R.id.nav_stats) {
            startActivity(new Intent(this, StatsActivity.class));
        } else if (id == R.id.nav_ai) {
            startActivity(new Intent(this, AIResponseActivity.class));
        } else if (id == R.id.nav_disconnect) {
            disconnectUser();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void disconnectUser() {
        mAuth.signOut();
        Toast.makeText(this, "Disconnected successfully", Toast.LENGTH_SHORT).show();
        
        // Navigate to login screen
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) return true;
        return super.onOptionsItemSelected(item);
    }
}
