package com.example.devmob;

import android.content.Context;
import android.content.Intent;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
        holder.label.setText(task.getDescription() != null && !task.getDescription().isEmpty() ? task.getDescription() : "task description ");
        holder.dueDate.setText(task.getDueDate() > 0
                ? dateFormat.format(new Date(task.getDueDate()))
                : "17/06/2025");
        holder.itemView.setOnClickListener(v -> listener.onTaskClick(task));
// Clic sur l'ImageButton de suppression
        holder.btndelete.setOnClickListener(v -> {
            String taskId = task.getId();
            DatabaseReference ref = FirebaseDatabase
                    .getInstance()
                    .getReference("tasks")
                    .child(taskId);

            // Supprime la tâche dans Firebase
            ref.removeValue()
                    .addOnSuccessListener(aVoid -> {
                        // Retire de la liste locale et notifie le RecyclerView
                        taskList.remove(position);
                        notifyItemRemoved(position);
                        Toast.makeText(
                                holder.itemView.getContext(),
                                "Tâche supprimée",
                                Toast.LENGTH_SHORT
                        ).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(
                                holder.itemView.getContext(),
                                "Erreur de suppression : " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    });
        });

        holder.btnEdit.setOnClickListener(v -> {
            Context ctx = holder.itemView.getContext();
            Intent intent = new Intent(ctx, EditTaskActivity.class);
            intent.putExtra("taskId", task.getId());
            // pass any other fields you need to prefill the editor:
            intent.putExtra("title", task.getTitle());
            intent.putExtra("description", task.getDescription());
            intent.putExtra("dueDate", task.getDueDate());
            intent.putExtra("progressPercent", task.getProgressPercent());
            // …
            ctx.startActivity(intent);
        });
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
ImageButton btndelete,btnEdit;
        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.task_title);
            dueDate = itemView.findViewById(R.id.task_due_date);
            label = itemView.findViewById(R.id.task_label);
            btndelete=itemView.findViewById(R.id.imageButton2);
            btnEdit=itemView.findViewById(R.id.imageButton3);
        }
    }
}
