package com.monitor.service;

import com.monitor.domain.Environment;
import com.monitor.domain.ServiceEntity;
import com.monitor.domain.ServiceStatus;
import com.monitor.repository.ServiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ServiceManagerService {

    private final ServiceRepository serviceRepository;

    public ServiceManagerService(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    @Transactional(readOnly = true)
    public List<ServiceEntity> findAll() {
        return serviceRepository.findAll();
    }

    @Transactional(readOnly = true)
    public ServiceEntity findById(UUID id) {
        return serviceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Service not found: " + id));
    }

    @Transactional(readOnly = true)
    public ServiceEntity findByName(String name) {
        return serviceRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("Service not found: " + name));
    }

    public ServiceEntity create(String name, Environment environment, String owner) {
        if (serviceRepository.existsByName(name)) {
            throw new IllegalArgumentException("Service already exists: " + name);
        }
        var entity = new ServiceEntity(name, environment, owner);
        return serviceRepository.save(entity);
    }

    public ServiceEntity update(UUID id, String name, Environment environment, String owner) {
        var entity = findById(id);
        entity.setName(name);
        entity.setEnvironment(environment);
        entity.setOwner(owner);
        return serviceRepository.save(entity);
    }

    public ServiceEntity updateStatus(UUID id, ServiceStatus status) {
        var entity = findById(id);
        entity.setStatus(status);
        return serviceRepository.save(entity);
    }

    public void delete(UUID id) {
        if (!serviceRepository.existsById(id)) {
            throw new IllegalArgumentException("Service not found: " + id);
        }
        serviceRepository.deleteById(id);
    }
}
