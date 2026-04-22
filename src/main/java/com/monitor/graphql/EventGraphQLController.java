package com.monitor.graphql;

import com.monitor.domain.Event;
import com.monitor.domain.ServiceEntity;
import com.monitor.domain.Severity;
import com.monitor.repository.EventRepository;
import com.monitor.repository.ServiceRepository;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

/**
 * GraphQL Controller for querying events.
 * <p>
 * Architecture Note:
 * We use a dual-API approach. While REST is used for simple CRUD operations on Services,
 * GraphQL is used for querying Events. This is because Events have nested relationships (e.g., Service)
 * and clients might want to query events based on complex filters (severity, serviceId) while
 * retrieving varied nested data shapes (e.g., just the service name vs the entire service object).
 * GraphQL solves the over-fetching/under-fetching problem perfectly here.
 * </p>
 */
@Controller
public class EventGraphQLController {

    private final EventRepository eventRepository;
    private final ServiceRepository serviceRepository;

    public EventGraphQLController(EventRepository eventRepository,
                                  ServiceRepository serviceRepository) {
        this.eventRepository = eventRepository;
        this.serviceRepository = serviceRepository;
    }

    @QueryMapping
    public List<Event> events(@Argument String serviceId, @Argument String severity) {
        // Both filters provided
        if (serviceId != null && severity != null) {
            return eventRepository.findByServiceIdAndSeverityOrderByTimestampDesc(
                    UUID.fromString(serviceId),
                    Severity.valueOf(severity.toUpperCase())
            );
        }

        // Filter by service only
        if (serviceId != null) {
            return eventRepository.findByServiceIdOrderByTimestampDesc(
                    UUID.fromString(serviceId)
            );
        }

        // Filter by severity only
        if (severity != null) {
            return eventRepository.findBySeverityOrderByTimestampDesc(
                    Severity.valueOf(severity.toUpperCase())
            );
        }

        // No filters — return all
        return eventRepository.findAllByOrderByTimestampDesc();
    }

    @QueryMapping
    public List<Event> eventsByService(@Argument String serviceName) {
        var service = serviceRepository.findByName(serviceName)
                .orElseThrow(() -> new IllegalArgumentException("Service not found: " + serviceName));
        return eventRepository.findByServiceIdOrderByTimestampDesc(service.getId());
    }

    @SchemaMapping(typeName = "Event", field = "service")
    public ServiceEntity service(Event event) {
        return event.getService();
    }
}
