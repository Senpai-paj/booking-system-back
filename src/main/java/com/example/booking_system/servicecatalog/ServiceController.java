package com.example.booking_system.servicecatalog;

import com.example.booking_system.servicecatalog.dto.ServiceRequest;
import com.example.booking_system.servicecatalog.dto.ServiceResponse;
import com.example.booking_system.servicecatalog.dto.ServiceShortResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ServiceController {

    private final ServiceService serviceService;

    @GetMapping
    public ResponseEntity<Page<ServiceShortResponse>> getAllServices(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection
    ) {
        Page<ServiceShortResponse> services = serviceService.getAllServices(
                name, isActive, page, size, sortBy, sortDirection
        );
        return ResponseEntity.ok(services);
    }

    // List of all ACTIVE services (for customer)
    @GetMapping("/active")
    public ResponseEntity<List<ServiceShortResponse>> getActiveServices() {
        List<ServiceShortResponse> services = serviceService.getAllActiveServices();
        return ResponseEntity.ok(services);
    }

    // Get service by ID (active only)
    @GetMapping("/{id}")
    public ResponseEntity<ServiceResponse> getServiceById(@PathVariable Long id) {
        ServiceResponse response = serviceService.getActiveServiceById(id);
        return ResponseEntity.ok(response);
    }

    // Get any service by ID (including inactive) - for admin
    @GetMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceResponse> getServiceByIdAdmin(@PathVariable Long id) {
        ServiceResponse response = serviceService.getServiceById(id);
        return ResponseEntity.ok(response);
    }

    // ADMIN ENDPOINTS

    // Create a service
    @PostMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceResponse> createService(@Valid @RequestBody ServiceRequest request) {
        ServiceResponse response = serviceService.createService(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Update service
    @PutMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceResponse> updateService(
            @PathVariable Long id,
            @Valid @RequestBody ServiceRequest request
    ) {
        ServiceResponse response = serviceService.updateService(id, request);
        return ResponseEntity.ok(response);
    }

    // Switch activity
    @PatchMapping("/admin/{id}/toggle-active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> toggleServiceActive(@PathVariable Long id) {
        serviceService.toggleActiveStatus(id);
        return ResponseEntity.ok().build();
    }

    // Soft removal
    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteService(@PathVariable Long id) {
        serviceService.deleteService(id);
        return ResponseEntity.noContent().build();
    }
}
