package com.example.booking_system.servicecatalog.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRequest {

    @NotBlank(message = "Service name is required")
    @Size(max = 255, message = "Service name must not exceed 255 characters")
    private String name;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    private Integer durationMinutes;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @Min(value = 0, message = "Price must be greater than or equal to 0")
    private BigDecimal price;

    private Boolean isActive;
}
