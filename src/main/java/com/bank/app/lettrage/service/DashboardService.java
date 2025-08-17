package com.bank.app.lettrage.service;

import com.bank.app.lettrage.entity.Reconciliation;
import com.bank.app.lettrage.repository.DashboardRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private final DashboardRepository dashboardRepository;

    public DashboardService(DashboardRepository dashboardRepository) {
        this.dashboardRepository = dashboardRepository;
    }

    public Map<String, Long> getTransactionVolume() {
        List<Object[]> results = dashboardRepository.getTransactionVolumeByPeriod();
        Map<String, Long> volumeMap = new HashMap<>();

        for (Object[] result : results) {
            String period = (String) result[0];
            Long volume = (Long) result[1];
            volumeMap.put(period, volume);
        }

        return volumeMap;
    }

    public Map<Boolean, Long> getReconciliationStatusCount() {
        List<Object[]> results = dashboardRepository.getReconciliationStatus();
        Map<Boolean, Long> statusMap = new HashMap<>();

        for (Object[] result : results) {
            Boolean matched = (Boolean) result[0];
            Long count = (Long) result[1];
            statusMap.put(matched, count);
        }

        return statusMap;
    }

    public Map<Boolean, Double> getReconciliationPerformance() {
        List<Object[]> results = dashboardRepository.getReconciliationPerformance();
        Map<Boolean, Double> performanceMap = new HashMap<>();

        for (Object[] result : results) {
            Boolean matched = (Boolean) result[0];
            Double avgAmount = (Double) result[1];
            performanceMap.put(matched, avgAmount);
        }

        return performanceMap;
    }

    public List<String> getPendingTransactions() {
        return dashboardRepository.getPendingTransactions();
    }

    public List<Reconciliation> getUnmatchedReconciliations() {
        return dashboardRepository.getUnmatchedReconciliations();
    }

    public String getReconciliationStatus() {
        Map<Boolean, Long> statusMap = getReconciliationStatusCount();
        long matched = statusMap.getOrDefault(true, 0L);
        long unmatched = statusMap.getOrDefault(false, 0L);

        if (matched > 0 && unmatched == 0) {
            return "Réconciliation terminée";
        } else if (matched > 0 && unmatched > 0) {
            return "Réconciliation en cours";
        } else {
            return "Aucune réconciliation effectuée";
        }
    }
}
