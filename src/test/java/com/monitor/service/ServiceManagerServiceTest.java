package com.monitor.service;

import com.monitor.domain.Environment;
import com.monitor.domain.ServiceEntity;
import com.monitor.domain.ServiceStatus;
import com.monitor.repository.ServiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceManagerServiceTest {

    @Mock
    private ServiceRepository serviceRepository;

    @InjectMocks
    private ServiceManagerService serviceManager;

    private ServiceEntity testService;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testService = new ServiceEntity("Payment-Service", Environment.PROD, "Team Alpha");
        testId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should return all services")
    void findAll_returnsAllServices() {
        var services = List.of(
                testService,
                new ServiceEntity("Auth-Service", Environment.PROD, "Team Security")
        );
        when(serviceRepository.findAll()).thenReturn(services);

        List<ServiceEntity> result = serviceManager.findAll();

        assertThat(result).hasSize(2);
        verify(serviceRepository).findAll();
    }

    @Test
    @DisplayName("Should find service by ID")
    void findById_existingId_returnsService() {
        when(serviceRepository.findById(testId)).thenReturn(Optional.of(testService));

        ServiceEntity result = serviceManager.findById(testId);

        assertThat(result.getName()).isEqualTo("Payment-Service");
        verify(serviceRepository).findById(testId);
    }

    @Test
    @DisplayName("Should throw when service not found by ID")
    void findById_nonExistentId_throws() {
        when(serviceRepository.findById(testId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> serviceManager.findById(testId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Service not found");
    }

    @Test
    @DisplayName("Should create a new service")
    void create_newService_savesAndReturns() {
        when(serviceRepository.existsByName("Payment-Service")).thenReturn(false);
        when(serviceRepository.save(any(ServiceEntity.class))).thenReturn(testService);

        ServiceEntity result = serviceManager.create("Payment-Service", Environment.PROD, "Team Alpha");

        assertThat(result.getName()).isEqualTo("Payment-Service");
        assertThat(result.getStatus()).isEqualTo(ServiceStatus.HEALTHY);
        verify(serviceRepository).save(any(ServiceEntity.class));
    }

    @Test
    @DisplayName("Should reject duplicate service names")
    void create_duplicateName_throws() {
        when(serviceRepository.existsByName("Payment-Service")).thenReturn(true);

        assertThatThrownBy(() ->
                serviceManager.create("Payment-Service", Environment.PROD, "Team Alpha"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");

        verify(serviceRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update an existing service")
    void update_existingService_updatesFields() {
        when(serviceRepository.findById(testId)).thenReturn(Optional.of(testService));
        when(serviceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ServiceEntity result = serviceManager.update(
                testId, "Updated-Service", Environment.STAGING, "Team Beta");

        assertThat(result.getName()).isEqualTo("Updated-Service");
        assertThat(result.getEnvironment()).isEqualTo(Environment.STAGING);
        assertThat(result.getOwner()).isEqualTo("Team Beta");
    }

    @Test
    @DisplayName("Should delete an existing service")
    void delete_existingService_deletesSuccessfully() {
        when(serviceRepository.existsById(testId)).thenReturn(true);

        serviceManager.delete(testId);

        verify(serviceRepository).deleteById(testId);
    }

    @Test
    @DisplayName("Should throw when deleting non-existent service")
    void delete_nonExistentService_throws() {
        when(serviceRepository.existsById(testId)).thenReturn(false);

        assertThatThrownBy(() -> serviceManager.delete(testId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Service not found");

        verify(serviceRepository, never()).deleteById(any());
    }
}
