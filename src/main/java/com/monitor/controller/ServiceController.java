package com.monitor.controller;

import com.monitor.controller.dto.ServiceRequest;
import com.monitor.controller.dto.ServiceResponse;
import com.monitor.service.ServiceManagerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for managing Service entities.
 * <p>
 * Architecture Note:
 * We use REST for Services because they represent a standard resource with well-defined
 * Create, Read, Update, Delete (CRUD) operations. The data shape for a Service is mostly flat,
 * making REST a natural and simple fit compared to GraphQL for these operations.
 * </p>
 */
@RestController
@RequestMapping("/api/services")
public class ServiceController {

    private final ServiceManagerService serviceManager;

    public ServiceController(ServiceManagerService serviceManager) {
        this.serviceManager = serviceManager;
    }

    @GetMapping
    public List<ServiceResponse> listAll() {
        return serviceManager.findAll().stream()
                .map(ServiceResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public ServiceResponse getById(@PathVariable UUID id) {
        return ServiceResponse.from(serviceManager.findById(id));
    }

    @PostMapping
    public ResponseEntity<ServiceResponse> create(@RequestBody ServiceRequest request) {
        var entity = serviceManager.create(
                request.name(),
                request.environment(),
                request.owner()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ServiceResponse.from(entity));
    }

    @PutMapping("/{id}")
    public ServiceResponse update(@PathVariable UUID id, @RequestBody ServiceRequest request) {
        var entity = serviceManager.update(
                id,
                request.name(),
                request.environment(),
                request.owner()
        );
        return ServiceResponse.from(entity);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        serviceManager.delete(id);
    }
}
