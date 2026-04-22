package com.monitor.service;

import com.monitor.domain.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.http.MediaType;

@Service
public class WebhookAlertService implements AlertService {

    private static final Logger log = LoggerFactory.getLogger(WebhookAlertService.class);
    
    @Value("${alert.webhook.url:}")
    private String webhookUrl;

    @Override
    public void sendAlert(Event event) {
        String message = String.format("🚨 CRITICAL ALERT 🚨\nService: %s\nEnvironment: %s\nMessage: %s",
                event.getService().getName(),
                event.getService().getEnvironment(),
                event.getMessage());

        if (webhookUrl == null || webhookUrl.isEmpty() || webhookUrl.isBlank()) {
            log.warn("Webhook URL not configured. Simulating alert payload:\n{}", message);
            return;
        }

        try {
            RestClient restClient = RestClient.create();
            // In a real application, create a proper JSON payload mapping to Slack/Discord webhook format
            String payload = "{\"text\": \"" + message.replace("\n", "\\n") + "\"}";
            
            restClient.post()
                    .uri(webhookUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
            
            log.info("Successfully dispatched critical alert to webhook for service: {}", event.getService().getName());
        } catch (Exception e) {
            log.error("Failed to send webhook alert for event ID: {}", event.getId(), e);
        }
    }
}
