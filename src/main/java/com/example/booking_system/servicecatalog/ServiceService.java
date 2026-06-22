package com.example.booking_system.servicecatalog;

import com.example.booking_system.exception.ResourceNotFoundException;
import com.example.booking_system.exception.ValidationException;
import com.example.booking_system.servicecatalog.dto.ServiceRequest;
import com.example.booking_system.servicecatalog.dto.ServiceResponse;
import com.example.booking_system.servicecatalog.dto.ServiceShortResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ServiceService {

    private final ServiceRepository serviceRepository;
    private final ServiceMapper serviceMapper;

    // create
    @Transactional
    public ServiceResponse createService(ServiceRequest request) {
        log.info("Creating new service: {}", request.getName());

        if (serviceRepository.existsByNameIgnoreCase(request.getName())) {
            throw new ValidationException("Service with name '" + request.getName() + "' already exists");
        }

        ServiceEntity service = serviceMapper.toEntity(request);
        service.setActive(request.getIsActive() != null ? request.getIsActive() : true);

        ServiceEntity saved = serviceRepository.save(service);
        log.info("Service created with id: {}", saved.getId());

        return serviceMapper.toResponse(saved);
    }

    // read
    @Transactional(readOnly = true)
    public ServiceResponse getServiceById(Long id) {
        ServiceEntity service = findServiceOrThrow(id);
        return serviceMapper.toResponse(service);
    }

    @Transactional(readOnly = true)
    public ServiceResponse getActiveServiceById(Long id) {
        ServiceEntity service = serviceRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Active service not found with id: " + id));
        return serviceMapper.toResponse(service);
    }

    @Transactional(readOnly = true)
    public Page<ServiceShortResponse> getAllServices(
            String name,
            Boolean isActive,
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {
        log.debug("Getting services with filters - name: {}, isActive: {}, page: {}, size: {}",
                name, isActive, page, size);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ServiceEntity> pageResult;

        if (isActive != null && !isActive) {
            // Inactive
            if (name != null && !name.isEmpty()) {
                pageResult = serviceRepository.findByNameContainingIgnoreCase(name, pageable);
            } else {
                pageResult = serviceRepository.findAll(pageable);
            }
        } else if (name != null && !name.isEmpty()) {
            // Active + filter by name
            if (isActive == null || isActive) {
                pageResult = serviceRepository.findByIsActiveTrueAndNameContainingIgnoreCase(name, pageable);
            } else {
                pageResult = serviceRepository.findByNameContainingIgnoreCase(name, pageable);
            }
        } else {
            // Only active
            if (isActive == null || isActive) {
                pageResult = serviceRepository.findByIsActiveTrue(pageable);
            } else {
                pageResult = serviceRepository.findAll(pageable);
            }
        }

        return serviceMapper.toShortResponsePage(pageResult);
    }

    @Transactional(readOnly = true)
    public List<ServiceShortResponse> getAllActiveServices() {
        List<ServiceEntity> services = serviceRepository.findAllByIsActiveTrue();
        return serviceMapper.toShortResponseList(services);
    }

    // update
    @Transactional
    public ServiceResponse updateService(Long id, ServiceRequest request) {
        log.info("Updating service with id: {}", id);

        ServiceEntity service = findServiceOrThrow(id);

        // Checking the uniqueness of a name (if the name has changed)
        if (!service.getName().equalsIgnoreCase(request.getName()) &&
                serviceRepository.existsByNameIgnoreCase(request.getName())) {
            throw new ValidationException("Service with name '" + request.getName() + "' already exists");
        }

        serviceMapper.updateEntity(service, request);

        ServiceEntity updated = serviceRepository.save(service);
        log.info("Service updated with id: {}", updated.getId());

        return serviceMapper.toResponse(updated);
    }

    // delete
    @Transactional
    public void toggleActiveStatus(Long id) {
        ServiceEntity service = findServiceOrThrow(id);
        service.setActive(!service.isActive());
        serviceRepository.save(service);
        log.info("Service {} status toggled to: {}", id, service.isActive());
    }

    @Transactional
    public void deleteService(Long id) {
        ServiceEntity service = findServiceOrThrow(id);
        service.setActive(false);
        serviceRepository.save(service);
        log.info("Service soft-deleted with id: {}", id);
    }

    // helper
    private ServiceEntity findServiceOrThrow(Long id) {
        return serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with id: " + id));
    }
}
