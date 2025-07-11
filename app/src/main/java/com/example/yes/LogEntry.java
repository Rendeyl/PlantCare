package com.example.yes;

public class LogEntry {

    private String message;
    private String timestamp;

    public LogEntry(String message, String timestamp) {
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return message + " at " + timestamp;
    }
}


