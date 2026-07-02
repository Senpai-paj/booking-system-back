package com.example.booking_system.booking;

import com.example.booking_system.booking.dto.BookingRequest;
import com.example.booking_system.booking.dto.BookingResponse;
import com.example.booking_system.booking.dto.BookingShortResponse;
import com.example.booking_system.schedule.Day;
import com.example.booking_system.schedule.Schedule;
import com.example.booking_system.schedule.ScheduleRepository;
import com.example.booking_system.servicecatalog.Service;
import com.example.booking_system.servicecatalog.ServiceRepository;
import com.example.booking_system.user.User;
import com.example.booking_system.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;
    private final ScheduleRepository scheduleRepository;
    private final BookingMapper bookingMapper;

    //TODO: changeStatus

    @Transactional(readOnly = true)
    public List<LocalDateTime> getFreeSlotsForDay(Long employeeId, Long serviceId, LocalDate targetDate) {
        log.info("Getting free slots for employee {}, service {} on date {}", employeeId, serviceId, targetDate);
        return findFreeSlotsForPeriod(employeeId, serviceId, targetDate, targetDate);
    }

    @Transactional(readOnly = true)
    public List<LocalDateTime> getFreeSlotsForWeek(Long employeeId, Long serviceId, LocalDate startDate) {
        log.info("Getting free slots for employee {}, service {} for week starting {}", employeeId, serviceId, startDate);
        LocalDate endDate = startDate.plusDays(6);
        return findFreeSlotsForPeriod(employeeId, serviceId, startDate, endDate);
    }

    @Transactional(readOnly = true)
    public List<LocalDateTime> getFreeSlotsForSeveralDays(Long employeeId, Long serviceId, LocalDate startDate, int numberOfDays) {
        log.info("Getting free slots for employee {}, service {} for {} days starting {}",
                employeeId, serviceId, numberOfDays, startDate);
        LocalDate endDate = startDate.plusDays(numberOfDays - 1);
        return findFreeSlotsForPeriod(employeeId, serviceId, startDate, endDate);
    }

//  General logic

    private List<LocalDateTime> findFreeSlotsForPeriod(Long employeeId, Long serviceId, LocalDate startDate, LocalDate endDate) {

        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new EntityNotFoundException("Service not found"));

        int serviceDuration = service.getDurationMinutes();
        List<LocalDateTime> freeSlots = new ArrayList<>();

        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {

            Day weekDay = Day.valueOf(currentDate.getDayOfWeek().name());

            Schedule schedule = scheduleRepository
                    .findByEmployeeIdAndWeekDayAndIsActiveTrue(employeeId, weekDay)
                    .orElse(null);

            if (schedule != null) {
                List<LocalDateTime> slotsForDay = findFreeSlotsForDay(currentDate, schedule, serviceDuration);
                freeSlots.addAll(slotsForDay);
            }

            currentDate = currentDate.plusDays(1);
        }
        return freeSlots;
    }

    private List<LocalDateTime> findFreeSlotsForDay(LocalDate date, Schedule schedule, int serviceDuration) {

        List<LocalDateTime> freeSlots = new ArrayList<>();

        LocalTime currentSlotTime = schedule.getWorkDayStart();
        LocalTime workDayEnd = schedule.getWorkDayEnd();
        int stepMinutes = schedule.getSlotGranularityMinutes();

        while (!currentSlotTime.plusMinutes(serviceDuration).isAfter(workDayEnd)) {

            LocalDateTime potentialStart = LocalDateTime.of(date, currentSlotTime);
            LocalDateTime potentialEnd = potentialStart.plusMinutes(serviceDuration);

            if (!slotNotAvailable(potentialStart, potentialEnd, schedule.getEmployee().getId(), null)) {
                freeSlots.add(potentialStart);
            }

            currentSlotTime = currentSlotTime.plusMinutes(stepMinutes);
        }
        return freeSlots;
    }

    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        User customer = userRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));
        User employee = userRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));
        Service service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new EntityNotFoundException("Service not found"));

        log.info("Creating booking of service {} for employee with ID {}", request.getServiceId(), request.getEmployeeId());

        if (slotNotAvailable(request.getStartTime(), request.getEndTime(), employee.getId(), null)) {
            throw new IllegalStateException("The selected time slot has just been taken. Please choose another time.");
        }

        Booking booking = bookingMapper.toEntity(request, customer, employee, service);

        try {
            Booking savedBooking = bookingRepository.saveAndFlush(booking);

            return bookingMapper.toResponse(savedBooking);
        } catch (DataIntegrityViolationException ex) {
            log.error("Database constraint violation while saving booking entity for employee ID: {}", employee.getId(), ex);
            throw new IllegalStateException("A booking conflict occurred. Another process may have modified this booking simultaneously.", ex);
        }
    }

    @Transactional
    public BookingShortResponse changeStatus(Long bookingId, BookingStatus status) {
        log.info("Changing booking's ({}) status with {}", bookingId, status);

        try {
            Booking existingBooking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new EntityNotFoundException("Booking not found"));

            bookingMapper.updateBookingStatus(status, existingBooking);

            Booking updatedBooking = bookingRepository.saveAndFlush(existingBooking);

            return bookingMapper.toShortResponse(updatedBooking);

        } catch (DataIntegrityViolationException ex) {
            log.error("Database constraint violation while changing status on booking with ID {}", bookingId);
            throw new IllegalStateException("A booking conflict occurred. Another process may have modified this booking simultaneously.", ex);
        }


    }

    @Transactional
    public String deleteBooking (Long bookingId) {
        log.info("Deleting booking with ID: {}", bookingId);

        try {
            Booking existingBooking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new EntityNotFoundException("Booking not found"));

            bookingRepository.delete(existingBooking);
            return String.format("Successfully deleted booking with ID: %s", bookingId);
        } catch (DataIntegrityViolationException ex) {
            log.error("Database constraint violation while deleting booking with ID {}", bookingId);
            throw new IllegalStateException("A booking conflict occurred. Another process may have modified this booking simultaneously.", ex);
        }
    }

    @Transactional(readOnly = true)
    public Page<BookingShortResponse> getAllByCustomer (Long customerId, Pageable pageable) {
        log.info("Getting bookings for customer with ID: {}, page: {}, size: {}",
                customerId, pageable.getPageNumber(), pageable.getPageSize());

        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));

        try {
            Page<Booking> allBookings = bookingRepository.findByCustomer(customer, pageable);

            if (allBookings.isEmpty()) {
                throw new EntityNotFoundException("Bookings not found for customer ID: " + customerId);
            }

            return bookingMapper.toShortResponsePage(allBookings);
        } catch (DataIntegrityViolationException ex) {
            log.error("Database constraint violation while getting bookings for customer with ID {}", customerId);
            throw new IllegalStateException("A booking conflict occurred. Another process may have modified this booking simultaneously.", ex);
        }
    }

    @Transactional(readOnly = true)
    public Page<BookingShortResponse> getAllByEmployee (Long employeeId, Pageable pageable) {
        log.info("Getting bookings for employee ID: {}, page: {}, size: {}",
                employeeId, pageable.getPageNumber(), pageable.getPageSize());

        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));

        try {
            Page<Booking> allBookings = bookingRepository.findByEmployee(employee, pageable);

            if (allBookings.isEmpty()) {
                throw new EntityNotFoundException("Bookings not found for employee ID: " + employeeId);
            }

            return bookingMapper.toShortResponsePage(allBookings);
        } catch (DataIntegrityViolationException ex) {
            log.error("Database constraint violation while getting bookings for employee with ID {}", employeeId);
            throw new IllegalStateException("A booking conflict occurred. Another process may have modified this booking simultaneously.", ex);
        }
    }

    @Transactional(readOnly = true)
    public Page<BookingShortResponse> getByEmployeeAndStatus(Long employeeId, BookingStatus status, Pageable pageable) {
        log.info("Getting bookings for employee ID: {}, status: {}, page: {}, size: {}",
                employeeId, status, pageable.getPageNumber(), pageable.getPageSize());

        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));

        Page<Booking> bookings = bookingRepository.findByEmployeeAndStatus(employee, status, pageable);

        if (bookings.isEmpty()) {
            log.info("No bookings found for employee ID: {} with status: {}", employeeId, status);
            return Page.empty(pageable);
        }

        return bookingMapper.toShortResponsePage(bookings);
    }

    @Transactional
    public BookingResponse getOneById(Long bookingId) {
        log.info("Getting booking with ID: {}", bookingId);

        try {
            Booking existingBooking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new EntityNotFoundException("Booking not found"));

            return bookingMapper.toResponse(existingBooking);
        } catch (DataIntegrityViolationException ex) {
            log.error("Database constraint violation while getting booking with ID {}", bookingId);
            throw new IllegalStateException("A booking conflict occurred. Another process may have modified this booking simultaneously.", ex);
        }

    }


    @Transactional
    public BookingResponse updateBooking(BookingRequest request, Long bookingId) {

        Booking existingBooking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found"));

        User newCustomer = (request.getCustomerId() != null)
                ? userRepository.findById(request.getCustomerId()).orElse(null) : null;

        Service newService = (request.getServiceId() != null)
                ? serviceRepository.findById(request.getServiceId()).orElse(null) : null;

        User newEmployee = (request.getEmployeeId() != null)
                ? userRepository.findById(request.getEmployeeId()).orElse(null) : null;

        User finalEmployee = (newEmployee != null) ? newEmployee : existingBooking.getEmployee();

        LocalDateTime finalStart = (request.getStartTime() != null) ? request.getStartTime() : existingBooking.getStartTime();
        LocalDateTime finalEnd = (request.getEndTime() != null) ? request.getEndTime() : existingBooking.getEndTime();

        log.info("Updating booking for date {} for employee with ID {}", request.getStartTime(), request.getEmployeeId());

        if (request.getStartTime() != null || request.getEndTime() != null || request.getEmployeeId() != null) {

            if (slotNotAvailable(finalStart, finalEnd, finalEmployee.getId(), existingBooking.getId())) {
                throw new IllegalStateException(
                        String.format("Provided booking time is overlapping. Start %s, End %s", finalStart, finalEnd)
                );
            }
        }

        bookingMapper.updateBooking(request,newCustomer, newEmployee, newService, existingBooking);

        try {
            Booking savedBooking = bookingRepository.saveAndFlush(existingBooking);

            return bookingMapper.toResponse(savedBooking);
        } catch (DataIntegrityViolationException ex) {
            log.error("Database constraint violation while saving booking entity for employee ID: {}", finalEmployee.getId(), ex);
            throw new IllegalStateException("A booking conflict occurred. Another process may have modified this booking simultaneously.", ex);
        }

    }

    private boolean slotNotAvailable(LocalDateTime start, LocalDateTime end, Long employeeId, Long bookingId) {

        List<Booking> allBookingDuringTheDay = bookingRepository.findActiveBookingsByEmployeeAndDate(employeeId, start);

        for (Booking currentBooking : allBookingDuringTheDay) {

            if (
                    start.isBefore(currentBooking.getEndTime()) &&
                    end.isAfter(currentBooking.getStartTime()) &&
                            !currentBooking.getId().equals(bookingId)
            ) {
                return true;
            }
        }
        return false;
    }
}
