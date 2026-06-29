package com.example.booking_system.booking;

import com.example.booking_system.booking.dto.BookingRequest;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoField;
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

    //TODO: getOneById, getAllByEmployee, getAllByCustomer, getAllByAdmin, createBooking, deleteBooking
    //long numberOfSlots = bookingMinutes / existingSchedule.getSlotGranularityMinutes();
    /*

    Schedule existingSchedule = scheduleRepository.findByEmployeeIdAndWeekDayAndIsActiveTrue(
                employeeId,
                Day.valueOf(booking.getStartTime().getDayOfWeek().name()))
                .orElseThrow(() -> new EntityNotFoundException("No active schedule found for this employee on this day"));

        long durationOfBooking = Duration.between(booking.getStartTime(), booking.getEndTime()).toMinutes();
        long workdayStart = existingSchedule.getWorkDayStart().get(ChronoField.MINUTE_OF_DAY);
        long workdayEnd = existingSchedule.getWorkDayEnd().get(ChronoField.MILLI_OF_DAY);

        for (int i = 0; i < allBookingDuringTheDay.size(); i++) {

            if(i + 1 <= allBookingDuringTheDay.size()) {

                long gapMinutes = Duration.between(
                        allBookingDuringTheDay.get(i).getEndTime(),
                        allBookingDuringTheDay.get(i+1).getStartTime()).toMinutes();

                if (
                        gapMinutes >= durationOfBooking &&
                        booking.getStartTime().get(ChronoField.MINUTE_OF_DAY) > workdayStart &&
                        gapMinutes < booking.getEndTime().get(ChronoField.MINUTE_OF_DAY)
                )  {



                }


            }



        }
         */


    @Transactional
    public Booking updateBooking(BookingRequest request, Long bookingId) {

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

        if (request.getStartTime() != null || request.getEndTime() != null || request.getEmployeeId() != null) {

            if (!checkAvailability(finalStart, finalEnd, finalEmployee.getId(), existingBooking.getId())) {
                throw new IllegalStateException(
                        String.format("Provided booking time is overlapping. Start %s, End %s", finalStart, finalEnd)
                );
            }
        }

        bookingMapper.updateBooking(request,newCustomer, newEmployee, newService, existingBooking);

        return bookingRepository.save(existingBooking);
    }

    private boolean checkAvailability(LocalDateTime start,LocalDateTime end, Long employeeId, Long bookingId) {

        List<Booking> allBookingDuringTheDay = bookingRepository.findActiveBookingsByEmployeeAndDate(employeeId, start);

        for (Booking currentBooking : allBookingDuringTheDay) {

            if (
                    start.isBefore(currentBooking.getEndTime()) &&
                    end.isAfter(currentBooking.getStartTime()) &&
                            !currentBooking.getId().equals(bookingId)
            ) {
                return false;
            }
        }
        return true;
    }


}
