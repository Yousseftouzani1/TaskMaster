package com.example.devmob;

import android.os.Bundle;
import android.util.Log;
import android.widget.TableLayout;
import android.widget.TableRow;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StatsActivity extends AppCompatActivity {
    private static final String TAG = "StatsActivity";

    private PieChart pieChart;
    private BarChart barChart;
    private TableLayout tableSummary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        pieChart     = findViewById(R.id.pieChart);
        barChart     = findViewById(R.id.barChart);
        tableSummary = findViewById(R.id.tableSummary);

        loadStats();
    }

    private void loadStats() {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("tasks");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                int total   = 0;
                int onTime  = 0;
                int late    = 0;
                int notDone = 0;
                long now = System.currentTimeMillis();

                for (DataSnapshot ds : snap.getChildren()) {
                    Task t = ds.getValue(Task.class);
                    if (t == null) continue;
                    total++;

                    // Determine if marked finished
                    boolean finishedFlag = t.getfinished()
                            || t.getProgressPercent() >= 100;

                    if (finishedFlag) {
                        // Use finishedDate, or fallback to lastUpdatedDate, then now
                        long finishTs = t.getFinishedDate();
                        if (finishTs <= 0) {
                            long upd = t.getLastUpdatedDate();
                            finishTs = upd > 0 ? upd : now;
                        }
                        long dueTs = t.getDueDate();

                        // If dueTs >0 and finishTs after dueTs => late, else on-time
                        if (dueTs > 0 && finishTs > dueTs) {
                            late++;
                        } else {
                            onTime++;
                        }
                    } else {
                        notDone++;
                    }
                }

                Log.d(TAG, String.format(Locale.US,
                        "Stats: total=%d onTime=%d late=%d notDone=%d",
                        total, onTime, late, notDone));

                showPieChart(onTime + late, notDone);
                showBarChart(onTime, late, notDone);
                fillSummaryTable(onTime, late, notDone, total);
            }

            @Override public void onCancelled(@NonNull DatabaseError e) {
                Log.e(TAG, "loadStats:onCancelled", e.toException());
            }
        });
    }

    private void showPieChart(int done, int notDone) {
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(done,    "Terminées"));
        entries.add(new PieEntry(notDone, "Non terminées"));

        PieDataSet ds = new PieDataSet(entries, "");
        ds.setColors(
                ContextCompat.getColor(this, android.R.color.holo_green_light),
                ContextCompat.getColor(this, android.R.color.darker_gray)
        );

        pieChart.setData(new PieData(ds));
        Description desc = new Description();
        desc.setText("Achèvement global");
        pieChart.setDescription(desc);
        pieChart.invalidate();
    }

    private void showBarChart(int onTime, int late, int notDone) {
        List<BarEntry> ents = new ArrayList<>();
        ents.add(new BarEntry(0f, onTime));
        ents.add(new BarEntry(1f, late));
        ents.add(new BarEntry(2f, notDone));

        BarDataSet ds = new BarDataSet(ents, "");
        ds.setColors(
                ContextCompat.getColor(this, android.R.color.holo_green_dark),
                ContextCompat.getColor(this, android.R.color.holo_orange_light),
                ContextCompat.getColor(this, android.R.color.holo_red_dark)
        );

        BarData data = new BarData(ds);
        data.setBarWidth(0.5f);
        barChart.setData(data);
        barChart.setFitBars(true);

        XAxis x = barChart.getXAxis();
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setGranularity(1f);
        x.setValueFormatter(new IndexAxisValueFormatter(new String[]{
                "À l’heure", "En retard", "Non réalisées"
        }));

        barChart.getAxisRight().setEnabled(false);
        barChart.getDescription().setText("Répartition des tâches");
        barChart.invalidate();
    }

    private void fillSummaryTable(int onTime, int late, int notDone, int total) {
        int count = tableSummary.getChildCount();
        if (count > 1) tableSummary.removeViews(1, count - 1);

        addRow("Terminées à l’heure", onTime, total);
        addRow("Terminées en retard", late,    total);
        addRow("Non terminées",       notDone, total);
        addRow("Taux de réussite",    onTime + late, total, true);
    }

    private void addRow(String label, int count, int total) {
        addRow(label, count, total, false);
    }

    private void addRow(String label, int count, int total, boolean highlight) {
        float pct = total == 0 ? 0f : (count * 100f / total);
        TableRow row = new TableRow(this);

        TextView tvLabel = new TextView(this);
        tvLabel.setText(label);
        if (highlight) {
            tvLabel.setTextColor(
                    ContextCompat.getColor(this, android.R.color.holo_blue_dark)
            );
        }

        TextView tvCount = new TextView(this);
        tvCount.setText(String.valueOf(count));

        TextView tvPct = new TextView(this);
        tvPct.setText(String.format(Locale.getDefault(), "%.1f%%", pct));

        row.addView(tvLabel);
        row.addView(tvCount);
        row.addView(tvPct);
        tableSummary.addView(row);
    }
}
