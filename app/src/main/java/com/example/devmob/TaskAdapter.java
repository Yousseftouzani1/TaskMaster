package com.example.devmob;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    public interface OnTaskClickListener {
        void onTaskClick(Task task);
    }
    private final OnTaskClickListener listener;
    private final List<Task> taskList;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public TaskAdapter(OnTaskClickListener listener, List<Task> taskList) {
        this.listener = listener;
        this.taskList = taskList;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.title.setText(task.getTitle());
        holder.label.setText(task.getDescription() != null && !task.getDescription().isEmpty() ? task.getDescription() : "No description");
        holder.dueDate.setText(task.getDueDate() > 0
                ? dateFormat.format(new Date(task.getDueDate()))
                : "No Due Date");
        holder.itemView.setOnClickListener(v -> listener.onTaskClick(task));

    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public void swapItems(int fromPosition, int toPosition) {
        Collections.swap(taskList, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView title, dueDate, label;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.task_title);
            dueDate = itemView.findViewById(R.id.task_due_date);
            label = itemView.findViewById(R.id.task_label);
        }
    }
}
