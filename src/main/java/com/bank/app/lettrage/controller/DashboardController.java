package com.bank.app.lettrage.controller;

import com.bank.app.lettrage.entity.Reconciliation;
import com.bank.app.lettrage.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "http://localhost:4200")
public class DashboardController {

    private final DashboardService dashboardService;

    @Autowired
    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/transaction-volume")
    public ResponseEntity<Map<String, Long>> getTransactionVolume() {
        try {
            Map<String, Long> result = dashboardService.getTransactionVolume();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/reconciliation-status-count")
    public ResponseEntity<Map<Boolean, Long>> getReconciliationStatusCount() {
        try {
            Map<Boolean, Long> result = dashboardService.getReconciliationStatusCount();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/reconciliation-performance")
    public ResponseEntity<Map<Boolean, Double>> getReconciliationPerformance() {
        try {
            Map<Boolean, Double> result = dashboardService.getReconciliationPerformance();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/pending-transactions")
    public ResponseEntity<List<String>> getPendingTransactions() {
        try {
            List<String> result = dashboardService.getPendingTransactions();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/unmatched-reconciliations")
    public ResponseEntity<List<Reconciliation>> getUnmatchedReconciliations() {
        try {
            List<Reconciliation> result = dashboardService.getUnmatchedReconciliations();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/reconciliation-status-summary")
    public ResponseEntity<String> getReconciliationStatusSummary() {
        try {
            String result = dashboardService.getReconciliationStatus();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
