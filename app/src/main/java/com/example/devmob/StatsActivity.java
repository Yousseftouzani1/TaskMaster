package com.example.devmob;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// ... (imports remain unchanged)

public class StatsActivity extends AppCompatActivity {

    private static final String TAG = "StatsActivity";

    private TextView completedTextView, rateTextView;
    private FirebaseFirestore db;
    private PieChart pieChart;
    private BarChart barChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        pieChart = findViewById(R.id.pieChart);
        barChart = findViewById(R.id.barChart);
        db = FirebaseFirestore.getInstance();

        Log.d(TAG, "onCreate: Initialized UI components and Firestore");

        fetchAndShowStats();
    }

    private void fetchBarStats(DataSnapshot snapshot) {
        int completedBeforeDue = 0;
        int completedAfterDue = 0;
        int notCompleted = 0;

        Log.d(TAG, "fetchBarStats: Starting to process snapshot with " + snapshot.getChildrenCount() + " children");

        for (DataSnapshot doc : snapshot.getChildren()) {
            Task task = doc.getValue(Task.class);
            if (task != null) {
                int progress = task.getProgressPercent();
                Log.d(TAG, "Task ID: " + doc.getKey() + " Progress: " + progress);

                if (progress == 100) { // Task is completed
                    try {
                        Date dueDate = new Date(task.getDueDate());
                        Date finishedDate = new Date(task.getFinishedDate());

                        Log.d(TAG, "Finished date: " + finishedDate + " | Due date: " + dueDate);

                        if (!finishedDate.after(dueDate)) { // Finished before or on the due date
                            completedBeforeDue++;
                        } else { // Finished after the due date
                            completedAfterDue++;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Date parsing error for task: " + doc.getKey(), e);
                        completedAfterDue++; // Handle the error case by considering it as completed late
                    }
                } else { // Task is not completed yet
                    notCompleted++;
                }
            } else {
                Log.w(TAG, "Task is null for key: " + doc.getKey());
            }
            Log.d(TAG, "completedBeforeDue=" + completedBeforeDue +
                    ", completedAfterDue=" + completedAfterDue +
                    ", notCompleted=" + notCompleted);
        }

        Log.d(TAG, "Summary -> CompletedBeforeDue: " + completedBeforeDue +
                ", CompletedAfterDue: " + completedAfterDue + ", NotCompleted: " + notCompleted);

        showBarChart(completedBeforeDue, completedAfterDue, notCompleted);
    }


    private void fetchAndShowStats() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("tasks");
        Log.d(TAG, "fetchAndShowStats: Fetching tasks from Firebase Realtime Database");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int completed = 0;
                int inProgress = 0;
                int total = (int) snapshot.getChildrenCount();

                Log.d(TAG, "onDataChange: Total tasks: " + total);

                for (DataSnapshot doc : snapshot.getChildren()) {
                    Task task = doc.getValue(Task.class);
                    if (task != null) {
                        int progress = task.getProgressPercent();
                        Log.d(TAG, "Processing Task ID: " + doc.getKey() + " Progress: " + progress);

                        if (progress != -1) {
                            if (progress == 100) {
                                completed++;
                            } else {
                                inProgress++;
                            }
                        }
                    } else {
                        Log.w(TAG, "Null task for key: " + doc.getKey());
                    }
                }

                Log.d(TAG, "Completed: " + completed + ", In Progress: " + inProgress);
                showPieChart(completed, inProgress);

                // Call bar stats processing
                fetchBarStats(snapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: ", error.toException());
            }
        });
    }

    private void showPieChart(int completed, int incomplete) {
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(completed, "Completed"));
        entries.add(new PieEntry(incomplete, "Incomplete"));

        Log.d(TAG, "PieChart: Entries -> Completed: " + completed + ", Incomplete: " + incomplete);

        PieDataSet dataSet = new PieDataSet(entries, " ");
        dataSet.setColors(new int[]{
                getResources().getColor(android.R.color.holo_blue_dark, getTheme()),
                getResources().getColor(android.R.color.darker_gray, getTheme())
        });

        PieData data = new PieData(dataSet);
        pieChart.setData(data);

        Description desc = new Description();
        desc.setText("Tasks completion rate");
        pieChart.setDescription(desc);

        pieChart.invalidate();
        Log.d(TAG, "PieChart rendered successfully");
    }

    private void showBarChart(int completedBeforeDue, int completedAfterDue, int notCompleted) {
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0f, completedBeforeDue));
        entries.add(new BarEntry(1f, completedAfterDue));
        entries.add(new BarEntry(2f, notCompleted));

        Log.d(TAG, "BarChart: Entries -> BeforeDue: " + completedBeforeDue +
                ", AfterDue: " + completedAfterDue + ", NotCompleted: " + notCompleted);

        BarDataSet dataSet = new BarDataSet(entries, "Tasks by Completion Timing");
        dataSet.setColors(
                ContextCompat.getColor(this, android.R.color.holo_green_dark),
                ContextCompat.getColor(this, android.R.color.holo_orange_light),
                ContextCompat.getColor(this, android.R.color.holo_red_dark)
        );

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.4f);
        barChart.setData(data);
        barChart.setFitBars(true);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAxisMinimum(-0.5f);
        xAxis.setAxisMaximum(2.5f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{
                "Completed on time", "Late completion", "Not completed"
        }));

        barChart.getAxisLeft().setAxisMinimum(0f);
        barChart.getAxisRight().setEnabled(false);
        barChart.getDescription().setText("Task Completion Breakdown");

        barChart.invalidate();
        Log.d(TAG, "BarChart rendered successfully");
    }
}

