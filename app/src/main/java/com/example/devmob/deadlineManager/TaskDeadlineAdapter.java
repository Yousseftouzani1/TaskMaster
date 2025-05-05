package com.example.devmob.deadlineManager;

import android.app.AlertDialog;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.devmob.R;
import com.example.devmob.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskDeadlineAdapter extends RecyclerView.Adapter<TaskDeadlineAdapter.TaskViewHolder> {

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
    }

    private List<Task> taskList;
    private OnTaskClickListener listener;

    public TaskDeadlineAdapter(OnTaskClickListener listener, List<Task> taskList) {
        this.listener = listener;
        this.taskList = taskList;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_deadline, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
//set the title to the holder
        holder.title.setText(task.getTitle());

        // Format due date
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        holder.date.setText("Due: " + sdf.format(new Date(task.getDueDate())));

        // Set progress UI
        int progress = task.getProgressPercent();
        holder.deadlineProgress.setProgress(progress);//set the progress to the holder
        holder.progressValue.setText(progress + "%");

        // Set status
        long now = System.currentTimeMillis();
        long daysLeft = (task.getDueDate() - now) / (1000 * 60 * 60 * 24);

        if (daysLeft < 0) {
            holder.status.setText("Overdue");
        } else if (daysLeft == 0) {
            holder.status.setText("Today");
        } else {
            holder.status.setText("In " + daysLeft + " days");
        }

        // Progress update
        holder.updateProgressButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            builder.setTitle("Update Progress");

            final EditText input = new EditText(v.getContext());
            input.setInputType(InputType.TYPE_CLASS_NUMBER);
            input.setHint("Enter progress %");
            builder.setView(input);

            builder.setPositiveButton("OK", (dialog, which) -> {
                String value = input.getText().toString().trim();
                if (!value.isEmpty()) {
                    int newProgress = Math.min(100, Math.max(0, Integer.parseInt(value)));

                    // Update UI
                    holder.deadlineProgress.setProgress(newProgress);
                    holder.progressValue.setText(newProgress + "%");

                    // Update Firebase
                    task.setProgressPercent(newProgress);
                    if (task.getId() != null) {
                        DatabaseReference ref = FirebaseDatabase.getInstance()
                                .getReference("tasks")
                                .child(task.getId());
                        ref.child("progressPercent").setValue(newProgress);
                    }
                }
            });

            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
            builder.show();
        });

        // On item click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTaskClick(task);
            }
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView title, date, status, progressValue;
        ProgressBar deadlineProgress;
        Button updateProgressButton;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.deadlineTitle);
            date = itemView.findViewById(R.id.deadlineDate);
            status = itemView.findViewById(R.id.deadlineStatus);
            deadlineProgress = itemView.findViewById(R.id.deadlineProgress);
            progressValue = itemView.findViewById(R.id.progressValue);
            updateProgressButton = itemView.findViewById(R.id.updateProgressButton);
        }
    }
}
