package com.example.booking_system.schedule;


import com.example.booking_system.schedule.dto.ScheduleRequest;
import com.example.booking_system.schedule.dto.ScheduleResponse;
import com.example.booking_system.schedule.dto.ScheduleShortResponse;
import com.example.booking_system.user.User;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ScheduleMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "employee", source = "employee")
    @Mapping(target = "workDayStart", source = "request.workDayStart")
    @Mapping(target = "workDayEnd", source = "request.workDayEnd")
    @Mapping(target = "weekDay", source = "request.weekDay")
    @Mapping(target = "slotGranularityMinutes", source = "request.slotGranularityMinutes")
    @Mapping(target = "isActive", source = "request.active")
    Schedule toEntity(ScheduleRequest request, User employee);

    @Mapping(target = "employeeId", source = "schedule.employee.id")
    @Mapping(target = "employeeFullName", expression = "java(schedule.getEmployee().getFirstName() + \" \" + schedule.getEmployee().getLastName())")
    @Mapping(target = "isActive", source = "schedule.active")
    ScheduleResponse toResponse(Schedule schedule);

    @Mapping(target = "isActive", source = "schedule.active")
    ScheduleShortResponse toShortResponse(Schedule schedule);

    List<ScheduleResponse> toResponseList(List<Schedule> schedules);
    List<ScheduleShortResponse> toShortResponseList(List<Schedule> schedules);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget Schedule schedule, ScheduleRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employee", ignore = true)
    void updateEntityFromDto(ScheduleRequest dto, @MappingTarget Schedule entity);

    default Page<ScheduleResponse> toResponsePage(Page<Schedule> page) {
        if (page == null) {
            return null;
        }
        return page.map(this::toResponse);
    }

    default Page<ScheduleShortResponse> toShortResponsePage(Page<Schedule> page) {
        if (page == null) {
            return null;
        }
        return page.map(this::toShortResponse);
    }

}
