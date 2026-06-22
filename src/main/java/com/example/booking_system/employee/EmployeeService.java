package com.example.booking_system.employee;

import com.example.booking_system.user.Role;
import com.example.booking_system.employee.dto.EmployeeRequest;
import com.example.booking_system.employee.dto.EmployeeResponse;
import com.example.booking_system.employee.dto.EmployeeShortResponse;
import com.example.booking_system.exception.ResourceNotFoundException;
import com.example.booking_system.exception.ValidationException;
import com.example.booking_system.user.User;
import com.example.booking_system.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeService {

    private final UserRepository userRepository;
    private final EmployeeMapper employeeMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        log.info("Creating new employee for userId: {}", request.getUserId());

        // Проверка существования пользователя
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        // Проверка, что пользователь уже сотрудник
        if (user.isEmployee()) {
            throw new ValidationException("User is already an employee");
        }

        // Обновляем пользователя
        employeeMapper.updateUser(user, request);
        user.setRole(Role.EMPLOYEE);
        user.setActive(true);

        User saved = userRepository.save(user);
        log.info("Employee created with id: {}", saved.getId());
        return employeeMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(Long id) {
        User user = findEmployeeOrThrow(id);
        return employeeMapper.toResponse(user);
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
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        // Ищем только сотрудников (EMPLOYEE или ADMIN)
        Page<User> pageResult = userRepository.findByRoleIn(
                List.of(Role.EMPLOYEE, Role.ADMIN),
                pageable
        );

        return employeeMapper.toShortResponsePage(pageResult);
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (!user.isEmployee()) {
            throw new ResourceNotFoundException("User is not an employee");
        }
        return employeeMapper.toResponse(user);
    }

    @Transactional(readOnly = true)
    public long countActiveEmployeesBySpecialization(String specialization) {
        return userRepository.countByIsActiveTrueAndSpecialization(specialization);
    }

    @Transactional
    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request) {
        User user = findEmployeeOrThrow(id);
        employeeMapper.updateUser(user, request);
        User updated = userRepository.save(user);
        return employeeMapper.toResponse(updated);
    }

    @Transactional
    public void toggleActiveStatus(Long id) {
        User user = findEmployeeOrThrow(id);
        user.setActive(!user.isActive());
        userRepository.save(user);
    }

    @Transactional
    public void deleteEmployee(Long id) {
        User user = findEmployeeOrThrow(id);
        user.setActive(false);
        user.setRole(Role.CUSTOMER);
        user.setSpecialization(null);
        user.setDescription(null);
        user.setExperienceYears(null);
        userRepository.save(user);
    }

    private User findEmployeeOrThrow(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (!user.isEmployee()) {
            throw new ResourceNotFoundException("User is not an employee");
        }
        return user;
    }
}
