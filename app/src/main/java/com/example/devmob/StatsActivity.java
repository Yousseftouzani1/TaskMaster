package com.example.devmob;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class StatsActivity extends AppCompatActivity {
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

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        Long progress = doc.getLong("progressPercent");

                        if (progress != null) {
                            if (progress == 100) {
                                completed++;
                            } else {
                                inProgress++;
                            }
                        } else {
                            // If progressPercent is missing, assume it's in progress
                            inProgress++;
                        }
                    }

                    showPieChart(completed, inProgress);
                    showBarChart(completed);
                });
    }


    private void showPieChart(int completed, int incomplete) {
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(completed, "Completed"));
        entries.add(new PieEntry(incomplete, "Incomplete"));

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
    }

    private void showBarChart(int completed) {
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0f, completed));

        BarDataSet dataSet = new BarDataSet(entries, "Tasks Completed");
        dataSet.setColors(new int[]{
                getResources().getColor(android.R.color.holo_blue_dark, getTheme()),
                getResources().getColor(android.R.color.darker_gray, getTheme())
        });


        BarData data = new BarData(dataSet);
        data.setBarWidth(0.9f); // set custom bar width

        barChart.setData(data);
        barChart.setFitBars(true); // make the x-axis fit exactly all bars
        barChart.getDescription().setText("Total Completed Tasks");
        barChart.invalidate(); // refresh
    }
}