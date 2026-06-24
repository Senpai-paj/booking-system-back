package com.example.booking_system.servicecatalog;

import com.example.booking_system.servicecatalog.dto.ServiceRequest;
import com.example.booking_system.servicecatalog.dto.ServiceResponse;
import com.example.booking_system.servicecatalog.dto.ServiceShortResponse;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ServiceMapper {

    // Request → Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isActive", source = "isActive")
    ServiceEntity toEntity(ServiceRequest request);

    // Entity -> Response
    @Mapping(target = "isActive", source = "isActive")
    ServiceResponse toResponse(ServiceEntity service);

    // Entity -> ShortResponse
    @Mapping(target = "isActive", source = "isActive")
    ServiceShortResponse toShortResponse(ServiceEntity service);

    // Lists
    List<ServiceResponse> toResponseList(List<ServiceEntity> services);
    List<ServiceShortResponse> toShortResponseList(List<ServiceEntity> services);

    // Update
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget ServiceEntity service, ServiceRequest request);

    // Pages
    default Page<ServiceResponse> toResponsePage(Page<ServiceEntity> page) {
        if (page == null) {
            return null;
        }
        return page.map(this::toResponse);
    }

    default Page<ServiceShortResponse> toShortResponsePage(Page<ServiceEntity> page) {
        if (page == null) {
            return null;
        }
        return page.map(this::toShortResponse);
    }
}
