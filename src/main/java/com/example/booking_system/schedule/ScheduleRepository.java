package com.example.booking_system.schedule;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    @Query("""
        SELECT COUNT(s) > 0 FROM Schedule s
        WHERE s.employee.id = :employeeId
          AND s.weekDay = :weekDay
          AND s.isActive = true
          AND s.workDayStart < :end
          AND s.workDayEnd > :start
    """)
    boolean existsOverlappingSchedule(
            @Param("employeeId") Long employeeId,
            @Param("weekDay") Day weekDay,
            @Param("start") LocalTime start,
            @Param("end") LocalTime end
    );

    Optional<Schedule> findByEmployeeIdAndWeekDayAndIsActiveTrue(Long employeeId, Day weekDay);

    List<Schedule> findByEmployeeIdAndIsActiveTrue(Long employeeId);

}
