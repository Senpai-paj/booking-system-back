package com.example.booking_system.booking.dto;

import com.example.booking_system.booking.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {

    private Long id;
    private Long clientId;
    private String clientFullName;
    private Long employeeId;
    private String employeeFullName;
    private Long serviceId;
    private String serviceName;
    private Integer serviceDurationMinutes;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BookingStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
