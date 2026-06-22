package com.example.booking_system.servicecatalog;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceEntity, Long> {

    // Search for active services
    Page<ServiceEntity> findByIsActiveTrue(Pageable pageable);

    // Search for active services by name (partial match)
    Page<ServiceEntity> findByIsActiveTrueAndNameContainingIgnoreCase(String name, Pageable pageable);

    // Search by name (without taking into account activity)
    Page<ServiceEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // Checking existence by name
    boolean existsByNameIgnoreCase(String name);

    // All active services
    List<ServiceEntity> findAllByIsActiveTrue();

    // Get an active service by ID
    Optional<ServiceEntity> findByIdAndIsActiveTrue(Long id);
}
