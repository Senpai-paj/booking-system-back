package com.example.booking_system.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeShortResponse {

    private Long id;
    private String fullName;
    private String specialization;
    private Boolean isActive;
}
