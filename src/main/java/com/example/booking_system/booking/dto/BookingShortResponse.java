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
public class BookingShortResponse {

    private Long id;
    private String serviceName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BookingStatus status;
}
