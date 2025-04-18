package com.example.devmob;

import java.util.Date;
import java.util.List;

public class Task {
    // Basic Task Details
    private String title;
    private String description;
    private Date dueDate;
    private String priorityLevel;
    private String status;

    // Additional Details
    private List<String> attachments; // file paths or URLs
    private List<String> comments;
    private long timeSpentMinutes; // total time spent in minutes
    private boolean remindersEnabled;
    private List<String> tags;

    // Timestamps
    private Date createdDate;
    private Date lastUpdatedDate;

    // Recurring & Dependency
    private String recurrencePattern; // e.g., "Daily", "Weekly", "None"
    private List<Task> dependencies; // other tasks this one depends on
    private int progressPercent; // from 0 to 100

    // Integration & Feedback
    private boolean calendarSynced;
    private String performanceSummary;
    private String userFeedback;

    // Constructor
    public Task() {
        // Required empty constructor for Firebase deserialization
    }

    public Task(String title, String description, Date dueDate,
                String priorityLevel, String status, List<String> attachments,
                List<String> comments, long timeSpentMinutes, boolean remindersEnabled,
                List<String> tags, Date createdDate, Date lastUpdatedDate,
                String recurrencePattern, List<Task> dependencies,
                int progressPercent, boolean calendarSynced,
                String performanceSummary, String userFeedback) {

        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.priorityLevel = priorityLevel;
        this.status = status;
        this.attachments = attachments;
        this.comments = comments;
        this.timeSpentMinutes = timeSpentMinutes;
        this.remindersEnabled = remindersEnabled;
        this.tags = tags;
        this.createdDate = createdDate;
        this.lastUpdatedDate = lastUpdatedDate;
        this.recurrencePattern = recurrencePattern;
        this.dependencies = dependencies;
        this.progressPercent = progressPercent;
        this.calendarSynced = calendarSynced;
        this.performanceSummary = performanceSummary;
        this.userFeedback = userFeedback;
    }

    // Getters
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Date getDueDate() { return dueDate; }
    public String getPriorityLevel() { return priorityLevel; }
    public String getStatus() { return status; }
    public List<String> getAttachments() { return attachments; }
    public List<String> getComments() { return comments; }
    public long getTimeSpentMinutes() { return timeSpentMinutes; }
    public boolean isRemindersEnabled() { return remindersEnabled; }
    public List<String> getTags() { return tags; }
    public Date getCreatedDate() { return createdDate; }
    public Date getLastUpdatedDate() { return lastUpdatedDate; }
    public String getRecurrencePattern() { return recurrencePattern; }
    public List<Task> getDependencies() { return dependencies; }// tasks that this task depend on
    public int getProgressPercent() { return progressPercent; }
    public boolean isCalendarSynced() { return calendarSynced; }
    public String getPerformanceSummary() { return performanceSummary; }
    public String getUserFeedback() { return userFeedback; }

    // Setters
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setDueDate(Date dueDate) { this.dueDate = dueDate; }
    public void setPriorityLevel(String priorityLevel) { this.priorityLevel = priorityLevel; }
    public void setStatus(String status) { this.status = status; }
    public void setAttachments(List<String> attachments) { this.attachments = attachments; }
    public void setComments(List<String> comments) { this.comments = comments; }
    public void setTimeSpentMinutes(long timeSpentMinutes) { this.timeSpentMinutes = timeSpentMinutes; }
    public void setRemindersEnabled(boolean remindersEnabled) { this.remindersEnabled = remindersEnabled; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public void setCreatedDate(Date createdDate) { this.createdDate = createdDate; }
    public void setLastUpdatedDate(Date lastUpdatedDate) { this.lastUpdatedDate = lastUpdatedDate; }
    public void setRecurrencePattern(String recurrencePattern) { this.recurrencePattern = recurrencePattern; }
    public void setDependencies(List<Task> dependencies) { this.dependencies = dependencies; }
    public void setProgressPercent(int progressPercent) { this.progressPercent = progressPercent; }
    public void setCalendarSynced(boolean calendarSynced) { this.calendarSynced = calendarSynced; }
    public void setPerformanceSummary(String performanceSummary) { this.performanceSummary = performanceSummary; }
    public void setUserFeedback(String userFeedback) { this.userFeedback = userFeedback; }
    // function to



}
