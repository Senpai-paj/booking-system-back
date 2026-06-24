package com.example.booking_system.servicecatalog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceShortResponse {

    private Long id;
    private String name;
    private Integer durationMinutes;
    private BigDecimal price;
    private Boolean isActive;
}
