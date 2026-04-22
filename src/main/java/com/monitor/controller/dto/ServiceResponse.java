package com.monitor.controller.dto;

import com.monitor.domain.Environment;
import com.monitor.domain.ServiceEntity;
import com.monitor.domain.ServiceStatus;

import java.time.Instant;
import java.util.UUID;

public record ServiceResponse(
        UUID id,
        String name,
        Environment environment,
        String owner,
        ServiceStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    public static ServiceResponse from(ServiceEntity entity) {
        return new ServiceResponse(
                entity.getId(),
                entity.getName(),
                entity.getEnvironment(),
                entity.getOwner(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
