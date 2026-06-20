package com.example.booking_system.employee;

import com.example.booking_system.employee.dto.EmployeeRequest;
import com.example.booking_system.employee.dto.EmployeeResponse;
import com.example.booking_system.employee.dto.EmployeeShortResponse;
import com.example.booking_system.exception.ResourceNotFoundException;
import com.example.booking_system.exception.ValidationException;
import com.example.booking_system.User.User;
import com.example.booking_system.User.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeService {

    private final EmployeeProfileRepository employeeRepository;
    private final UserRepository userRepository;
    private final EmployeeMapper employeeMapper;

    @Transactional
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        log.info("Creating new employee profile for userId: {}", request.getUserId());

        // Проверка существования пользователя
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        // Проверка, что у пользователя нет уже профиля сотрудника
        if (employeeRepository.existsByUserId(request.getUserId())) {
            throw new ValidationException("User already has an employee profile");
        }

        // Создание профиля
        EmployeeProfile employee = employeeMapper.toEntity(request, user);
        EmployeeProfile saved = employeeRepository.save(employee);

        log.info("Employee profile created with id: {}", saved.getId());
        return employeeMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(Long id) {
        EmployeeProfile employee = findEmployeeOrThrow(id);
        return employeeMapper.toResponse(employee);
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeByUserId(Long userId) {
        EmployeeProfile employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found for userId: " + userId));
        return employeeMapper.toResponse(employee);
    }

    @Transactional(readOnly = true)
    public Page<EmployeeShortResponse> getAllEmployees(
            String specialization,
            Boolean isActive,
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {
        log.debug("Getting employees with filters - specialization: {}, isActive: {}, page: {}, size: {}",
                specialization, isActive, page, size);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<EmployeeProfile> pageResult;

        // Логика фильтрации
        if (isActive != null && !isActive) {
            // Если запрошены неактивные - просто возвращаем все с фильтром
            if (specialization != null && !specialization.isEmpty()) {
                pageResult = employeeRepository.findBySpecializationContainingIgnoreCase(specialization, pageable);
            } else {
                pageResult = employeeRepository.findAll(pageable);
            }
        } else if (specialization != null && !specialization.isEmpty()) {
            // Активные + фильтр по специализации
            if (isActive == null || isActive) {
                pageResult = employeeRepository.findByIsActiveTrueAndSpecializationContainingIgnoreCase(specialization, pageable);
            } else {
                pageResult = employeeRepository.findBySpecializationContainingIgnoreCase(specialization, pageable);
            }
        } else {
            // Только активные
            if (isActive == null || isActive) {
                pageResult = employeeRepository.findByIsActiveTrue(pageable);
            } else {
                pageResult = employeeRepository.findAll(pageable);
            }
        }

        return pageResult.map(employeeMapper::toShortResponse);
    }

    @Transactional
    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request) {
        log.info("Updating employee profile with id: {}", id);

        EmployeeProfile employee = findEmployeeOrThrow(id);

        // Если меняется пользователь
        if (request.getUserId() != null && !request.getUserId().equals(employee.getUser().getId())) {
            User newUser = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

            if (employeeRepository.existsByUserId(request.getUserId()) &&
                    !employeeRepository.findByUserId(request.getUserId()).get().getId().equals(id)) {
                throw new ValidationException("User already has an employee profile");
            }
            employee.setUser(newUser);
        }

        // Обновление полей
        if (request.getSpecialization() != null) {
            employee.setSpecialization(request.getSpecialization());
        }
        if (request.getDescription() != null) {
            employee.setDescription(request.getDescription());
        }
        if (request.getExperienceYears() != null) {
            employee.setExperienceYears(request.getExperienceYears());
        }
        if (request.getIsActive() != null) {
            employee.setIsActive(request.getIsActive());
        }

        EmployeeProfile updated = employeeRepository.save(employee);
        log.info("Employee profile updated with id: {}", updated.getId());

        return employeeMapper.toResponse(updated);
    }

    @Transactional
    public void toggleActiveStatus(Long id) {
        EmployeeProfile employee = findEmployeeOrThrow(id);
        employee.setActive(!employee.isActive());
        employeeRepository.save(employee);
        log.info("Employee profile {} status toggled to: {}", id, employee.isActive());
    }

    @Transactional
    public void deleteEmployee(Long id) {
        EmployeeProfile employee = findEmployeeOrThrow(id);
        // Soft delete - просто делаем неактивным
        employee.setActive(false);
        employeeRepository.save(employee);
        log.info("Employee profile soft-deleted with id: {}", id);
    }

    @Transactional(readOnly = true)
    public long countActiveEmployeesBySpecialization(String specialization) {
        return employeeRepository.countActiveBySpecialization(specialization);
    }

    // Helper method
    private EmployeeProfile findEmployeeOrThrow(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
    }
}
