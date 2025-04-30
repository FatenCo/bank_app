package com.bank.app.lettrage.entity;

import java.util.List;

public class ImportResult {
    private final int total;
    private final int successCount;
    private final int failureCount;
    private final double failureRate;
    private final boolean alert;
    private final List<LogEntry> logs;

    public ImportResult(int total, int successCount, List<LogEntry> logs, double alertThreshold) {
        this.total = total;
        this.successCount = successCount;
        this.failureCount = total - successCount;
        this.failureRate = total == 0 ? 0 : (double) failureCount / total;
        this.alert = this.failureRate > alertThreshold;
        this.logs = logs;
    }

    public int getTotal() { return total; }
    public int getSuccessCount() { return successCount; }
    public int getFailureCount() { return failureCount; }
    public double getFailureRate() { return failureRate; }
    public boolean isAlert() { return alert; }
    public List<LogEntry> getLogs() { return logs; }
}
