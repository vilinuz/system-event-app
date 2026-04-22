package com.monitor.service;

import com.monitor.domain.Event;
import com.monitor.domain.ServiceEntity;
import com.monitor.domain.ServiceStatus;
import com.monitor.domain.Severity;
import com.monitor.repository.EventRepository;
import com.monitor.repository.ServiceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates mock events every 30 seconds using Java 25 Structured Concurrency.
 * <p>
 * Tech Stack & Architecture Notes:
 * This service leverages {@link StructuredTaskScope} (introduced in earlier JEPs and refined in Java 25)
 * to fan out event generation across all registered services concurrently.
 * 
 * Why Structured Concurrency instead of ExecutorService/CompletableFuture?
 * 1. Readability: The code reads top-to-bottom sequentially. The `try-with-resources` block cleanly defines the boundary of concurrent execution.
 * 2. Error Handling & Cancellation: If one subtask fails or the parent thread is interrupted, all other running subtasks in the scope are automatically cancelled. This prevents thread leaks and orphaned tasks.
 * 3. Observability: Structured concurrency preserves the hierarchy of tasks, meaning thread dumps and observability tools can show exactly which parent task spawned which subtasks.
 * 4. Virtual Threads: Under the hood, this works seamlessly with Java Virtual Threads (Project Loom). Because virtual threads are lightweight, we can easily fork thousands of tasks simultaneously without thread pool exhaustion.
 * </p>
 */
@Service
public class EventGeneratorService {

    private static final Logger log = LoggerFactory.getLogger(EventGeneratorService.class);

    private static final String[] INFO_MESSAGES = {
            "Health check passed successfully",
            "Configuration reloaded",
            "Cache cleared and rebuilt",
            "Metrics snapshot exported",
            "Scheduled maintenance window started",
            "Connection pool refreshed"
    };

    private static final String[] WARN_MESSAGES = {
            "Response latency above threshold (> 500ms)",
            "Memory usage exceeded 80%",
            "Connection pool nearing capacity",
            "Retry attempt #3 for downstream dependency",
            "Certificate expiry in 7 days",
            "Disk usage above 75%"
    };

    private static final String[] ERROR_MESSAGES = {
            "Failed to connect to database replica",
            "Timeout waiting for upstream response",
            "Authentication token validation failed",
            "Message queue consumer lag detected",
            "Circuit breaker tripped for external API",
            "Out of memory on worker thread"
    };

    private static final String[] CRITICAL_MESSAGES = {
            "Service is unresponsive — restarting",
            "Data corruption detected in primary store",
            "Complete loss of connectivity to all replicas",
            "Security breach: unauthorized access attempt blocked",
            "Cascading failure across dependent services"
    };

    private final EventRepository eventRepository;
    private final ServiceRepository serviceRepository;
    private final AlertService alertService;

    public EventGeneratorService(EventRepository eventRepository,
                                 ServiceRepository serviceRepository,
                                 AlertService alertService) {
        this.eventRepository = eventRepository;
        this.serviceRepository = serviceRepository;
        this.alertService = alertService;
    }

    /**
     * Runs every 30 seconds. For each registered service, forks a virtual-thread
     * subtask that generates a random event. 
     * <p>
     * Concurrency Model:
     * - We open a {@link StructuredTaskScope} with a `Joiner.allSuccessfulOrThrow()`.
     * - This joiner policy means we wait for ALL subtasks to succeed. If ANY subtask throws an exception,
     *   the scope immediately cancels all other ongoing subtasks and throws the exception to the parent.
     * - `scope.fork()` runs the task on a new virtual thread (if the application is configured to use virtual threads).
     * - `scope.join()` blocks the parent thread until the scope's policy is satisfied (in this case, all done or one failed).
     * </p>
     */
    @Scheduled(fixedRate = 30_000, initialDelay = 5_000)
    @Transactional
    public void generateEvents() {
        List<ServiceEntity> services = serviceRepository.findAll();
        if (services.isEmpty()) {
            return;
        }

        log.info("⚡ Generating events for {} services using Structured Concurrency", services.size());

        try (var scope = StructuredTaskScope.open(
                StructuredTaskScope.Joiner.<Event>allSuccessfulOrThrow())) {

            // Fork a subtask per service
            var subtasks = services.stream()
                    .map(service -> scope.fork(() -> createRandomEvent(service)))
                    .toList();

            // Join all — blocks until every subtask completes (or one fails)
            scope.join();

            // Collect results
            List<Event> events = subtasks.stream()
                    .map(StructuredTaskScope.Subtask::get)
                    .toList();

            eventRepository.saveAll(events);
            log.info("✅ Persisted {} events", events.size());

            // Dispatch alerts asynchronously using Virtual Threads for any CRITICAL events
            events.stream()
                    .filter(e -> e.getSeverity() == Severity.CRITICAL)
                    .forEach(e -> Thread.ofVirtual().start(() -> alertService.sendAlert(e)));

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Event generation interrupted", e);
        } catch (Exception e) {
            log.error("Event generation failed", e);
        }
    }

    private Event createRandomEvent(ServiceEntity service) {
        var random = ThreadLocalRandom.current();
        var severity = pickSeverity(random);
        var message = pickMessage(severity, random);

        // Possibly degrade service status based on severity
        if (severity == Severity.CRITICAL) {
            service.setStatus(ServiceStatus.DOWN);
        } else if (severity == Severity.ERROR && random.nextBoolean()) {
            service.setStatus(ServiceStatus.DEGRADED);
        } else if (severity == Severity.INFO && random.nextInt(3) == 0) {
            service.setStatus(ServiceStatus.HEALTHY);
        }

        return new Event(service, severity, message);
    }

    private Severity pickSeverity(ThreadLocalRandom random) {
        int roll = random.nextInt(100);
        if (roll < 45) return Severity.INFO;
        if (roll < 75) return Severity.WARN;
        if (roll < 92) return Severity.ERROR;
        return Severity.CRITICAL;
    }

    private String pickMessage(Severity severity, ThreadLocalRandom random) {
        return switch (severity) {
            case INFO     -> INFO_MESSAGES[random.nextInt(INFO_MESSAGES.length)];
            case WARN     -> WARN_MESSAGES[random.nextInt(WARN_MESSAGES.length)];
            case ERROR    -> ERROR_MESSAGES[random.nextInt(ERROR_MESSAGES.length)];
            case CRITICAL -> CRITICAL_MESSAGES[random.nextInt(CRITICAL_MESSAGES.length)];
        };
    }
}
