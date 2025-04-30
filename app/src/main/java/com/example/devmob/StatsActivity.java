package com.example.devmob;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

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

        fetchAndShowStats();
    }

    private void fetchAndShowStats() {
        db.collection("tasks")
                .get()
                .addOnSuccessListener(snapshot -> {
                    int completed = 0;
                    int inProgress = 0;
                    int total = snapshot.size();

                    Log.d(TAG, "Total tasks fetched: " + total);

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        Long progress = doc.getLong("progressPercent");
                        Log.d(TAG, "Task progressPercent: " + progress);

                        if (progress != null) {
                            if (progress == 100) {
                                completed++;
                            } else {
                                inProgress++;
                            }
                        } else {
                            inProgress++;
                            Log.d(TAG, "progressPercent missing, counted as inProgress.");
                        }
                    }

                    Log.d(TAG, "Completed: " + completed + ", InProgress: " + inProgress);
                    showPieChart(completed, inProgress);
                    showBarChart(completed, total);
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching tasks", e));
    }

    private void showPieChart(int completed, int incomplete) {
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(completed, "Completed"));
        entries.add(new PieEntry(incomplete, "Incomplete"));

        Log.d(TAG, "Pie chart entries: Completed=" + completed + ", Incomplete=" + incomplete);

        PieDataSet dataSet = new PieDataSet(entries, "Task Completion Rate");
        dataSet.setColors(new int[]{
                getResources().getColor(android.R.color.holo_blue_dark, getTheme()),
                getResources().getColor(android.R.color.darker_gray, getTheme())
        });

        PieData data = new PieData(dataSet);
        pieChart.setData(data);

        Description desc = new Description();
        desc.setText("Completion Ratio");
        pieChart.setDescription(desc);

        pieChart.invalidate(); // refresh
        Log.d(TAG, "Pie chart rendered");
    }

    private void showBarChart(int completed, int total) {
        int incomplete = total - completed; // You can replace this with actual count if you want.

        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0f, completed));
        entries.add(new BarEntry(1f, incomplete));

        Log.d(TAG, "Bar chart entries: Completed=" + completed + ", Incomplete=" + incomplete);

        BarDataSet dataSet = new BarDataSet(entries, "Tasks");
        dataSet.setColors(
                ContextCompat.getColor(this, android.R.color.holo_blue_dark),
                ContextCompat.getColor(this, android.R.color.darker_gray)
        );


        BarData data = new BarData(dataSet);
        data.setBarWidth(0.4f);

        barChart.setData(data);
        barChart.setFitBars(true);
        barChart.getDescription().setText("Total Completed Tasks");
        barChart.getXAxis().setGranularity(1f); // Good: Ensures each bar is centered
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM); // Good: Places X axis at the bottom
        barChart.getXAxis().setAxisMinimum(0.0f); // Good: Starts X axis at 0
        barChart.getXAxis().setAxisMaximum(2f); // Good: Because you have only two categories (0 and 1)
        barChart.getAxisLeft().setAxisMinimum(0f); // Great: Ensures no negative bar heights
        barChart.getAxisRight().setEnabled(false); // Good: Hides right Y axis

        barChart.invalidate(); // refresh
        Log.d(TAG, "Bar chart rendered");
    }
}
