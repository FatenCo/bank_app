package com.bank.app.lettrage.entity;

public class LogEntry {
    public enum Level { INFO, ERROR, ALERT }

    private final int line;
    private final Level level;
    private final String message;

    public LogEntry(int line, Level level, String message) {
        this.line = line;
        this.level = level;
        this.message = message;
    }

    public int getLine() { return line; }
    public Level getLevel() { return level; }
    public String getMessage() { return message; }
}
