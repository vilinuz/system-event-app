package com.monitor.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Represents a system event associated with a specific service.
 * <p>
 * Architecture Note:
 * This entity represents the core "Event" domain concept.
 * It is mapped to the 'events' table and defines a ManyToOne relationship with 'ServiceEntity'.
 * </p>
 */
@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(nullable = false)
    private Instant timestamp;

    /**
     * Relationship to the service that generated the event.
     * <p>
     * Optimization: We use FetchType.LAZY to prevent the default EAGER fetching of ManyToOne relationships.
     * EAGER fetching would cause an N+1 select problem when loading a list of events.
     * Instead, we handle the fetching eagerly only when necessary using @EntityGraph in the repository.
     * </p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private ServiceEntity service;

    protected Event() {
        // Required by JPA
    }

    public Event(ServiceEntity service, Severity severity, String message) {
        this.service = service;
        this.severity = severity;
        this.message = message;
        this.timestamp = Instant.now();
    }

    // --- Getters ---

    public UUID getId() {
        return id;
    }

    public Severity getSeverity() {
        return severity;
    }

    public String getMessage() {
        return message;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public ServiceEntity getService() {
        return service;
    }
}
