package com.example.devmob;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.devmob.deadlineManager.DeadlineActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TaskListActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private ActionBarDrawerToggle toggle;

    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private List<Task> taskList     = new ArrayList<>();
    private List<Task> filteredList = new ArrayList<>();

    private FloatingActionButton fabAdd;
    private TextInputEditText searchEditText;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // --- find views ---
        toolbar        = findViewById(R.id.toolbar);
        drawerLayout   = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        recyclerView   = findViewById(R.id.recyclerView);
        fabAdd         = findViewById(R.id.fab_add_task);
        searchEditText = findViewById(R.id.search_edit_text);

        // --- setup toolbar & drawer ---
        setSupportActionBar(toolbar);
        toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // --- populate navigation header ---
        updateNavigationHeader();

        // --- handle navigation item clicks ---
        navigationView.setNavigationItemSelectedListener(this);

        // --- set up RecyclerView & adapter ---
        adapter = new TaskAdapter(task -> {
            Intent intent = new Intent(TaskListActivity.this, TaskDetailActivity.class);
            intent.putExtra("title",            task.getTitle());
            intent.putExtra("description",      task.getDescription());
            intent.putExtra("task_status",      task.getStatus());
            intent.putExtra("task_priority",    task.getPriorityLevel());
            intent.putExtra("dueDate",          task.getDueDate());
            intent.putExtra("progressPercent",  task.getProgressPercent());
            intent.putExtra("finished",         task.getfinished());
            intent.putExtra("taskId",           task.getId());
            if (task.getTags() != null) {
                intent.putStringArrayListExtra("tags", new ArrayList<>(task.getTags()));
            } else {
                intent.putStringArrayListExtra("tags", new ArrayList<>());
            }
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }, filteredList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // --- search filtering ---
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int st, int b, int c) {
                filterTasks(s.toString());
            }
        });

        // --- load only unfinished tasks from Firebase ---
        Query tasksQuery = FirebaseDatabase.getInstance()
                .getReference("tasks")
                .orderByChild("isfinished")
                .equalTo(false);

        tasksQuery.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                taskList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Task t = ds.getValue(Task.class);
                    if (t != null) {
                        t.setId(ds.getKey());
                        taskList.add(t);
                    }
                }
                filteredList.clear();
                filteredList.addAll(taskList);
                adapter.notifyDataSetChanged();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TaskListActivity.this,
                        "Erreur de lecture : " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });

        // --- enable drag & drop ---
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP|ItemTouchHelper.DOWN, 0) {
            @Override public boolean onMove(@NonNull RecyclerView rv,
                                            @NonNull RecyclerView.ViewHolder src,
                                            @NonNull RecyclerView.ViewHolder dst) {
                adapter.swapItems(src.getAdapterPosition(), dst.getAdapterPosition());
                return true;
            }
            @Override public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int dir) {}
        }).attachToRecyclerView(recyclerView);

        // --- FAB to create new task ---
        fabAdd.setOnClickListener(v -> {
            new TaskCreationBottomSheet()
                    .show(getSupportFragmentManager(), "TaskCreationBottomSheet");
        });
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

    /** Filters displayed tasks by title or description in real time. */
    private void filterTasks(String query) {
        String lower = query.toLowerCase(Locale.getDefault());
        filteredList.clear();
        if (lower.isEmpty()) {
            filteredList.addAll(taskList);
        } else {
            for (Task t : taskList) {
                boolean inTitle = t.getTitle() != null &&
                        t.getTitle().toLowerCase(Locale.getDefault()).contains(lower);
                boolean inDesc  = t.getDescription() != null &&
                        t.getDescription().toLowerCase(Locale.getDefault()).contains(lower);
                if (inTitle || inDesc) {
                    filteredList.add(t);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_tasks) {
            // Already in tasks, just close drawer
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (id == R.id.nav_calendar) {
            startActivity(new Intent(this, CalendarActivity.class));
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
