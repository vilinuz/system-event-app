package com.monitor.config;

import com.monitor.domain.*;
import com.monitor.repository.EventRepository;
import com.monitor.repository.ServiceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final ServiceRepository serviceRepository;
    private final EventRepository eventRepository;

    public DataInitializer(ServiceRepository serviceRepository,
                           EventRepository eventRepository) {
        this.serviceRepository = serviceRepository;
        this.eventRepository = eventRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (serviceRepository.count() > 0) {
            log.info("⏭️ Database already contains data. Skipping initial seeding.");
            return;
        }

        log.info("🌱 Seeding initial data...");

        var paymentService = serviceRepository.save(
                new ServiceEntity("Payment-Service", Environment.PROD, "Team Alpha"));

        var authService = serviceRepository.save(
                new ServiceEntity("Auth-Service", Environment.PROD, "Team Security"));

        var notificationService = serviceRepository.save(
                new ServiceEntity("Notification-Service", Environment.STAGING, "Team Comms"));

        var inventoryService = serviceRepository.save(
                new ServiceEntity("Inventory-Service", Environment.DEV, "Team Logistics"));

        // Seed initial events
        eventRepository.saveAll(List.of(
                new Event(paymentService, Severity.INFO, "Service started successfully"),
                new Event(paymentService, Severity.WARN, "Response latency above threshold (> 500ms)"),
                new Event(paymentService, Severity.ERROR, "Failed to connect to database replica"),

                new Event(authService, Severity.INFO, "Health check passed successfully"),
                new Event(authService, Severity.INFO, "Configuration reloaded"),
                new Event(authService, Severity.CRITICAL, "Security breach: unauthorized access attempt blocked"),

                new Event(notificationService, Severity.INFO, "Cache cleared and rebuilt"),
                new Event(notificationService, Severity.WARN, "Memory usage exceeded 80%"),

                new Event(inventoryService, Severity.INFO, "Metrics snapshot exported"),
                new Event(inventoryService, Severity.WARN, "Certificate expiry in 7 days"),
                new Event(inventoryService, Severity.ERROR, "Timeout waiting for upstream response")
        ));

        // Mark Auth-Service as degraded from the critical event
        authService.setStatus(ServiceStatus.DEGRADED);
        serviceRepository.save(authService);

        log.info("✅ Seeded {} services and {} events",
                serviceRepository.count(), eventRepository.count());
    }
}
