package com.example.booking_system.schedule.dto;

import com.example.booking_system.schedule.Day;
import com.example.booking_system.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleResponse {

    private Long id;
    private Long employeeId;
    private String employeeFullName;
    private LocalTime workDayStart;
    private LocalTime workDayEnd;
    private Integer slotGranularityMinutes;
    private Day weekDay;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
