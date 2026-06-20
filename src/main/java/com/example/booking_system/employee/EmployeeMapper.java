package com.example.booking_system.employee;

import com.example.booking_system.employee.dto.EmployeeRequest;
import com.example.booking_system.employee.dto.EmployeeResponse;
import com.example.booking_system.employee.dto.EmployeeShortResponse;
import com.example.booking_system.User.User;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface EmployeeMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", source = "user")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    EmployeeProfile toEntity(EmployeeRequest request, @Context User user);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userFullName", expression = "java(employee.getUser().getFirstName() + \" \" + employee.getUser().getLastName())")
    @Mapping(target = "userEmail", source = "user.email")
    EmployeeResponse toResponse(EmployeeProfile employee);

    @Mapping(target = "fullName", expression = "java(employee.getUser().getFirstName() + \" \" + employee.getUser().getLastName())")
    EmployeeShortResponse toShortResponse(EmployeeProfile employee);

    List<EmployeeResponse> toResponseList(List<EmployeeProfile> employees);
    List<EmployeeShortResponse> toShortResponseList(List<EmployeeProfile> employees);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget EmployeeProfile employee, EmployeeRequest request);

    default Page<EmployeeResponse> toResponsePage(Page<EmployeeProfile> page) {
        return page.map(this::toResponse);
    }

    default Page<EmployeeShortResponse> toShortResponsePage(Page<EmployeeProfile> page) {
        return page.map(this::toShortResponse);
    }
}
