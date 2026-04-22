package com.monitor.repository;

import com.monitor.domain.ServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceEntity, UUID> {

    Optional<ServiceEntity> findByName(String name);

    boolean existsByName(String name);
}
