package com.example.booking_system.booking;

import com.example.booking_system.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Page<Booking> findByCustomer(User customer, Pageable pageable);
    Page<Booking> findByEmployee(User employee, Pageable pageable);
    Page<Booking> findByEmployeeAndStatus(User employee, BookingStatus status, Pageable pageable);

    // Checking the intersection of armor
    @Query("SELECT b FROM Booking b WHERE b.employee.id = :employeeId " +
            "AND b.status != 'CANCELLED' " +
            "AND ((b.startTime < :endTime AND b.endTime > :startTime))")
    List<Booking> findOverlappingBookings(
            @Param("employeeId") Long employeeId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    // Search for active employee records on a date
    @Query("SELECT b FROM Booking b WHERE b.employee.id = :employeeId " +
            "AND b.status != 'CANCELLED' " +
            "AND DATE(b.startTime) = DATE(:date)")
    List<Booking> findActiveBookingsByEmployeeAndDate(
            @Param("employeeId") Long employeeId,
            @Param("date") LocalDateTime date
    );

    // Counting records by status for an employee
    long countByEmployeeAndStatus(User employee, BookingStatus status);
}
