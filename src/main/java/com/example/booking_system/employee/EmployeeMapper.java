package com.example.booking_system.employee;

import com.example.booking_system.employee.dto.EmployeeRequest;
import com.example.booking_system.employee.dto.EmployeeResponse;
import com.example.booking_system.employee.dto.EmployeeShortResponse;
import com.example.booking_system.user.User;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface EmployeeMapper {

    // Создание сотрудника из запроса (обновляем поля User)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "firstName", ignore = true)
    @Mapping(target = "lastName", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "specialization", source = "specialization")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "experienceYears", source = "experienceYears")
    @Mapping(target = "isActive", source = "isActive")
    void updateUser(@MappingTarget User user, EmployeeRequest request);

    // Преобразование User в DTO
    @Mapping(target = "userId", source = "id")
    @Mapping(target = "userFullName", expression = "java(user.getFirstName() + \" \" + user.getLastName())")
    @Mapping(target = "userEmail", source = "email")
    @Mapping(target = "specialization", source = "specialization")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "experienceYears", source = "experienceYears")
    @Mapping(target = "isActive", source = "isActive")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    EmployeeResponse toResponse(User user);

    // Преобразование User в краткий ответ
    @Mapping(target = "fullName", expression = "java(user.getFirstName() + \" \" + user.getLastName())")
    @Mapping(target = "specialization", source = "specialization")
    @Mapping(target = "isActive", source = "isActive")
    EmployeeShortResponse toShortResponse(User user);

    List<EmployeeResponse> toResponseList(List<User> users);

    List<EmployeeShortResponse> toShortResponseList(List<User> users);

    default Page<EmployeeResponse> toResponsePage(Page<User> page) {
        if (page == null) {
            return null;
        }
        return page.map(this::toResponse);
    }

    default Page<EmployeeShortResponse> toShortResponsePage(Page<User> page) {
        if (page == null) {
            return null;
        }
        return page.map(this::toShortResponse);
    }
}
