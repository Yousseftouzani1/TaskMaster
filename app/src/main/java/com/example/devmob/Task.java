package com.example.devmob;

import java.util.Date;
import java.util.List;

public class Task {
    // Basic Task Details
    private String title;
    private String description;
    private long dueDate;
    private String priorityLevel;
    private String status;

    // Additional Details
    private List<String> attachments;
    private List<String> comments;
    private long timeSpent;
    private boolean reminderEnabled;
    private boolean isfinished;
    private List<String> tags;

    // Timestamps
    private long createdDate;
    private long lastUpdatedDate;
    private long finishedDate;

    // Recurring & Dependency
    private String recurrence;
    private List<Task> dependencies;
    private int progressPercent;
    
    // Integration & Feedback
    private boolean calendarSync;
    private String performanceSummary;
    private String userFeedback;
    private String id;

    // Constructor
    public Task() {
        // Required empty constructor for Firebase deserialization
    }

    public Task(String id, String title, String description, long dueDate,
                String priorityLevel, String status, List<String> attachments,
                List<String> comments, long timeSpent, boolean reminderEnabled,
                List<String> tags, long createdDate, long lastUpdatedDate,
                String recurrence, List<Task> dependencies,
                int progressPercent, boolean calendarSync,
                String performanceSummary, String userFeedback, long finishedDate) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.priorityLevel = priorityLevel;
        this.status = status;
        this.attachments = attachments;
        this.comments = comments;
        this.timeSpent = timeSpent;
        this.reminderEnabled = reminderEnabled;
        this.tags = tags;
        this.createdDate = createdDate;
        this.lastUpdatedDate = lastUpdatedDate;
        this.recurrence = recurrence;
        this.dependencies = dependencies;
        this.progressPercent = progressPercent;
        this.calendarSync = calendarSync;
        this.performanceSummary = performanceSummary;
        this.userFeedback = userFeedback;
        this.isfinished = false;
        this.finishedDate = finishedDate;
    }

    public long getFinishedDate() {
        return this.finishedDate;
    }

    public void setFinishedDate(long date) {
        this.finishedDate = date;
    }

    public boolean getfinished() {
        return this.isfinished;
    }

    public void markfinished() {
        this.isfinished = true;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // Getters
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public long getDueDate() { return dueDate; }
    public String getPriorityLevel() { return priorityLevel; }
    public String getStatus() { return status; }
    public List<String> getAttachments() { return attachments; }
    public List<String> getComments() { return comments; }
    public long getTimeSpent() { return timeSpent; }
    public boolean isRemindersEnabled() { return reminderEnabled; }
    public List<String> getTags() { return tags; }
    public long getCreatedDate() { return createdDate; }
    public long getLastUpdatedDate() { return lastUpdatedDate; }
    public String getRecurrence() { return recurrence; }
    public List<Task> getDependencies() { return dependencies; }
    public int getProgressPercent() { return progressPercent; }
    public boolean isCalendarSynced() { return calendarSync; }
    public String getPerformanceSummary() { return performanceSummary; }
    public String getUserFeedback() { return userFeedback; }

    // Setters
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setDueDate(long dueDate) { this.dueDate = dueDate; }
    public void setPriorityLevel(String priorityLevel) { this.priorityLevel = priorityLevel; }
    public void setStatus(String status) { this.status = status; }
    public void setAttachments(List<String> attachments) { this.attachments = attachments; }
    public void setComments(List<String> comments) { this.comments = comments; }
    public void setTimeSpent(long timeSpent) { this.timeSpent = timeSpent; }
    public void setRemindersEnabled(boolean reminderEnabled) { this.reminderEnabled = reminderEnabled; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public void setCreatedDate(long createdDate) { this.createdDate = createdDate; }
    public void setLastUpdatedDate(long lastUpdatedDate) { this.lastUpdatedDate = lastUpdatedDate; }
    public void setRecurrence(String recurrence) { this.recurrence = recurrence; }
    public void setDependencies(List<Task> dependencies) { this.dependencies = dependencies; }
    public void setProgressPercent(int progressPercent) { this.progressPercent = progressPercent; }
    public void setCalendarSynced(boolean calendarSync) { this.calendarSync = calendarSync; }
    public void setPerformanceSummary(String performanceSummary) { this.performanceSummary = performanceSummary; }
    public void setUserFeedback(String userFeedback) { this.userFeedback = userFeedback; }
}
