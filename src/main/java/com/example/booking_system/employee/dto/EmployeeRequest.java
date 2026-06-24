package com.example.booking_system.employee.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeRequest {

    @NotNull(message = "User ID must not be null")
    private Long userId;

    @NotBlank(message = "Specialization is required")
    @Size(max = 255, message = "Specialization must not exceed 255 characters")
    private String specialization;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @Min(value = 0, message = "Experience years must be greater than or equal to 0")
    private Integer experienceYears;

    private Boolean isActive;
}
