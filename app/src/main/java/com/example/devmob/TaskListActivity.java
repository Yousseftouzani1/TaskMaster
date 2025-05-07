package com.example.devmob;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TaskListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private List<Task> taskList;
    private FloatingActionButton fabAdd;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);

        // Initialisation
        toolbar = findViewById(R.id.toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        recyclerView = findViewById(R.id.recyclerView);
        fabAdd = findViewById(R.id.fab_add_task);

        // Toolbar setup
        setSupportActionBar(toolbar);

        // Drawer toggle (hamburger icon)
        toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Menu item click handling (if needed)
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            int id = menuItem.getItemId();

            if (id == R.id.nav_home) {
                Toast.makeText(this, "Accueil", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_calendar) {
                Toast.makeText(this, "Calendrier", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_priority) {
                Toast.makeText(this, "Priorités", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(TaskListActivity.this, AIResponseActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_stats) {
                Toast.makeText(this, "Statistiques", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(TaskListActivity.this, StatsActivity.class);
                startActivity(intent);
            }else if (id == R.id.nav_deadline) {
                Toast.makeText(this, "Deadlines", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(TaskListActivity.this, DeadlineActivity.class);
                startActivity(intent);
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });


// read from realtime database instance in firebase
        taskList = new ArrayList<>();

        DatabaseReference tasksRef = FirebaseDatabase.getInstance().getReference("tasks");

        tasksRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                taskList.clear();
                for (DataSnapshot taskSnapshot : snapshot.getChildren()) {
                    Task task = taskSnapshot.getValue(Task.class);
                    if (task != null) {
                        task.setId(taskSnapshot.getKey()); //  set Firebase key as ID
                        if(!task.getfinished()){
                            taskList.add(task);
                        }

                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TaskListActivity.this, " Erreur de lecture : " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Adapter
        adapter = new TaskAdapter(task -> {
            Intent intent = new Intent(TaskListActivity.this, TaskDetailActivity.class);

            intent.putExtra("title", task.getTitle());
            intent.putExtra("description", task.getDescription());
            intent.putExtra("task_status", task.getStatus());
            intent.putExtra("task_priority", task.getPriorityLevel());
            intent.putExtra("dueDate", task.getDueDate());
            intent.putExtra("progressPercent", task.getProgressPercent());
            intent.putExtra("finished", task.getfinished());
            intent.putStringArrayListExtra("tags", new ArrayList<>(task.getTags()));
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }, taskList);


        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Drag-and-drop
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView rv,
                                  @NonNull RecyclerView.ViewHolder from,
                                  @NonNull RecyclerView.ViewHolder to) {
                adapter.swapItems(from.getAdapterPosition(), to.getAdapterPosition());
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {}
        });
        itemTouchHelper.attachToRecyclerView(recyclerView);

        // FAB click oppens the buttom sheet
        fabAdd.setOnClickListener(v -> {
            TaskCreationBottomSheet bottomSheet = new TaskCreationBottomSheet();
            bottomSheet.show(getSupportFragmentManager(), "TaskCreationBottomSheet");
        });

    }

    // Gérer le bouton retour quand le drawer est ouvert
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    // Synchroniser le toggle avec l'état du menu
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
