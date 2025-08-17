package com.bank.app.lettrage.configuration;

import com.bank.app.lettrage.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class ReconciliationStatusWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private DashboardService dashboardService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        try {
            String status = dashboardService.getReconciliationStatus();
            session.sendMessage(new TextMessage(status));
        } catch (Exception e) {
            e.printStackTrace();
            session.sendMessage(new TextMessage("Erreur lors de la récupération du statut"));
        }
    }
}