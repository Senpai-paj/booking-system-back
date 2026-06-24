package com.example.booking_system.employee;

import com.example.booking_system.employee.dto.EmployeeRequest;
import com.example.booking_system.employee.dto.EmployeeResponse;
import com.example.booking_system.employee.dto.EmployeeShortResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    // Список сотрудников
    @GetMapping
    public ResponseEntity<Page<EmployeeShortResponse>> getAllEmployees(
            @RequestParam(required = false) String specialization,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection
    ) {
        Page<EmployeeShortResponse> employees = employeeService.getAllEmployees(
                specialization, isActive, page, size, sortBy, sortDirection
        );
        return ResponseEntity.ok(employees);
    }

    // Получить сотрудника по ID
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> getEmployeeById(@PathVariable Long id) {
        EmployeeResponse response = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(response);
    }

    // Получить сотрудника по ID пользователя
    @GetMapping("/user/{userId}")
    public ResponseEntity<EmployeeResponse> getEmployeeByUserId(@PathVariable Long userId) {
        EmployeeResponse response = employeeService.getEmployeeByUserId(userId);
        return ResponseEntity.ok(response);
    }

    // Создание сотрудника - только ADMIN
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmployeeResponse> createEmployee(@Valid @RequestBody EmployeeRequest request) {
        EmployeeResponse response = employeeService.createEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Обновление сотрудника - ADMIN или сам сотрудник
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isEmployeeOwner(#id, authentication)")
    public ResponseEntity<EmployeeResponse> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeRequest request
    ) {
        EmployeeResponse response = employeeService.updateEmployee(id, request);
        return ResponseEntity.ok(response);
    }

    // Переключение статуса активности - только ADMIN
    @PatchMapping("/{id}/toggle-active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> toggleEmployeeActive(@PathVariable Long id) {
        employeeService.toggleActiveStatus(id);
        return ResponseEntity.ok().build();
    }

    // Мягкое удаление (деактивация) - только ADMIN
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    // Получить количество активных сотрудников по специализации
    @GetMapping("/count")
    public ResponseEntity<Long> countActiveBySpecialization(@RequestParam String specialization) {
        long count = employeeService.countActiveEmployeesBySpecialization(specialization);
        return ResponseEntity.ok(count);
    }
}
