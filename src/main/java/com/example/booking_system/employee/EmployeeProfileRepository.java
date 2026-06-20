package com.example.booking_system.employee;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeProfileRepository extends JpaRepository<EmployeeProfile, Long> {

    // Поиск сотрудников с пагинацией и фильтром по активности
    Page<EmployeeProfile> findByIsActiveTrue(Pageable pageable);

    // Поиск всех сотрудников с фильтром по специализации
    Page<EmployeeProfile> findBySpecializationContainingIgnoreCase(String specialization, Pageable pageable);

    // Поиск только активных сотрудников с фильтром по специализации
    Page<EmployeeProfile> findByIsActiveTrueAndSpecializationContainingIgnoreCase(String specialization, Pageable pageable);

    // Проверка существования сотрудника по user_id
    boolean existsByUserId(Long userId);

    // Поиск сотрудника по id пользователя
    Optional<EmployeeProfile> findByUserId(Long userId);

    // Подсчет активных сотрудников по специализации
    @Query("SELECT COUNT(e) FROM EmployeeProfile e WHERE e.isActive = true AND e.specialization = :specialization")
    long countActiveBySpecialization(@Param("specialization") String specialization);
}
