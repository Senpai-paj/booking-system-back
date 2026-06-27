package com.example.booking_system.booking;

import com.example.booking_system.booking.dto.BookingRequest;
import com.example.booking_system.booking.dto.BookingResponse;
import com.example.booking_system.booking.dto.BookingShortResponse;
import com.example.booking_system.servicecatalog.ServiceEntity;
import com.example.booking_system.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface  BookingMapper {

    // Create a Booking from a request
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "client", source = "client")
    @Mapping(target = "employee", source = "employee")
    @Mapping(target = "service", source = "service")
    @Mapping(target = "endTime", ignore = true)
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Booking toEntity(BookingRequest request, User client, User employee, ServiceEntity service);

    // Entity -> Response
    @Mapping(target = "clientId", source = "client.id")
    @Mapping(target = "clientFullName", expression = "java(booking.getClient().getFirstName() + \" \" + booking.getClient().getLastName())")
    @Mapping(target = "employeeId", source = "employee.id")
    @Mapping(target = "employeeFullName", expression = "java(booking.getEmployee().getFirstName() + \" \" + booking.getEmployee().getLastName())")
    @Mapping(target = "serviceId", source = "service.id")
    @Mapping(target = "serviceName", source = "service.name")
    @Mapping(target = "serviceDurationMinutes", source = "service.durationMinutes")
    BookingResponse toResponse(Booking booking);

    // Entity -> ShortResponse
    @Mapping(target = "serviceName", source = "service.name")
    BookingShortResponse toShortResponse(Booking booking);

    // Lists
    List<BookingResponse> toResponseList(List<Booking> bookings);
    List<BookingShortResponse> toShortResponseList(List<Booking> bookings);

    // Pages
    default Page<BookingResponse> toResponsePage(Page<Booking> page) {
        if (page == null) {
            return null;
        }
        return page.map(this::toResponse);
    }

    default Page<BookingShortResponse> toShortResponsePage(Page<Booking> page) {
        if (page == null) {
            return null;
        }
        return page.map(this::toShortResponse);
    }
}
