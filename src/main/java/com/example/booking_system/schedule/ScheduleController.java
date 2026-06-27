package com.example.booking_system.schedule;

import com.example.booking_system.servicecatalog.dto.ServiceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
public class ScheduleController {

    @GetMapping("/employee/{id}")
    public



    @GetMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceResponse> getServiceByIdAdmin(@PathVariable Long id) {
        ServiceResponse response = serviceService.getServiceById(id);
        return ResponseEntity.ok(response);
    }

}
