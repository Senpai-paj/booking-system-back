package com.example.booking_system.schedule.dto;

import com.example.booking_system.schedule.Day;
import jakarta.validation.constraints.NotNull;
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
public class ScheduleRequest {

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @NotNull(message = "WorkdayStart is required")
    private LocalTime workDayStart;

    @NotNull(message = "WorkdayEnd is required")
    private LocalTime workDayEnd;

    @NotNull(message = "Weekday is required")
    private Day weekDay;

    @Builder.Default
    private Integer slotGranularityMinutes = 30;

    private boolean isActive;

}
