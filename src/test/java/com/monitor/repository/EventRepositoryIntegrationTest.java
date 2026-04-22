package com.monitor.repository;

import com.monitor.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.test.database.replace=none")
@Testcontainers
class EventRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    private ServiceEntity paymentService;
    private ServiceEntity authService;

    @BeforeEach
    void setUp() {
        eventRepository.deleteAll();
        serviceRepository.deleteAll();

        paymentService = serviceRepository.save(
                new ServiceEntity("Payment-Service", Environment.PROD, "Team Alpha"));

        authService = serviceRepository.save(
                new ServiceEntity("Auth-Service", Environment.PROD, "Team Security"));

        eventRepository.saveAll(List.of(
                new Event(paymentService, Severity.INFO, "Health check passed"),
                new Event(paymentService, Severity.ERROR, "Database connection failed"),
                new Event(paymentService, Severity.ERROR, "Timeout on upstream call"),
                new Event(paymentService, Severity.WARN, "Latency spike detected"),

                new Event(authService, Severity.INFO, "Config reloaded"),
                new Event(authService, Severity.CRITICAL, "Unauthorized access attempt")
        ));
    }

    @Test
    @DisplayName("Should find events by service ID ordered by timestamp desc")
    void findByServiceIdOrderByTimestampDesc() {
        List<Event> events = eventRepository
                .findByServiceIdOrderByTimestampDesc(paymentService.getId());

        assertThat(events).hasSize(4);
        assertThat(events).allMatch(e ->
                e.getService().getId().equals(paymentService.getId()));
    }

    @Test
    @DisplayName("Should filter events by service ID and severity")
    void findByServiceIdAndSeverity() {
        List<Event> errorEvents = eventRepository
                .findByServiceIdAndSeverityOrderByTimestampDesc(
                        paymentService.getId(), Severity.ERROR);

        assertThat(errorEvents).hasSize(2);
        assertThat(errorEvents).allMatch(e ->
                e.getSeverity() == Severity.ERROR);
    }

    @Test
    @DisplayName("Should find events by severity across all services")
    void findBySeverity() {
        List<Event> infoEvents = eventRepository
                .findBySeverityOrderByTimestampDesc(Severity.INFO);

        assertThat(infoEvents).hasSize(2);
    }

    @Test
    @DisplayName("Should return all events ordered by timestamp")
    void findAllOrdered() {
        List<Event> allEvents = eventRepository.findAllByOrderByTimestampDesc();

        assertThat(allEvents).hasSize(6);
    }

    @Test
    @DisplayName("Should return empty list for non-existent service")
    void findByNonExistentService() {
        var randomId = java.util.UUID.randomUUID();
        List<Event> events = eventRepository
                .findByServiceIdOrderByTimestampDesc(randomId);

        assertThat(events).isEmpty();
    }
}
