package com.example.booking_system.schedule;

import com.example.booking_system.schedule.dto.ScheduleRequest;
import com.example.booking_system.schedule.dto.ScheduleResponse;
import com.example.booking_system.schedule.dto.ScheduleShortResponse;
import com.example.booking_system.user.User;
import com.example.booking_system.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final ScheduleMapper scheduleMapper;
    private final UserRepository userRepository;

    @Transactional
    public List<ScheduleResponse> createSchedule(Map<Day, ScheduleRequest> requestsByDay) {
        if (requestsByDay == null || requestsByDay.isEmpty()) {
            log.warn("Received an empty schedule creation request map.");
            return List.of();
        }

        List<ScheduleRequest> requests = new ArrayList<>(requestsByDay.values());
        Long employeeId = requests.getFirst().getEmployeeId();
        log.info("Creating {} new schedule entries for employee with ID: {}", requests.size(), employeeId);

        User employee = getEmployeeOrThrow(employeeId);

        try {
            String validation = checkIfSchedulesExists(employeeId, requests);
            if (!validation.isEmpty()) {
                throw new IllegalArgumentException(validation);
            }

            List<Schedule> scheduleTemplates = requests.stream()
                    .map(request -> scheduleMapper.toEntity(request, employee))
                    .toList();

            List<Schedule> savedTemplates = scheduleRepository.saveAll(scheduleTemplates);
            return scheduleMapper.toResponseList(savedTemplates);

        } catch (DataIntegrityViolationException ex) {
            log.error("Database constraint violation while saving schedules for employee {}", employeeId, ex);
            throw new IllegalStateException("A schedule conflict occurred. Another process may have modified this schedule simultaneously.", ex);
        }
    }

    @Transactional
    public ScheduleResponse createScheduleForADay(ScheduleRequest request) {
        validateNonNullRequest(request, "create");
        Long employeeId = request.getEmployeeId();
        log.info("Creating schedule on {} for employee with ID: {}", request.getWeekDay(), employeeId);

        User employee = getEmployeeOrThrow(employeeId);

        try {
            if (scheduleExists(employeeId, request)) {
                throw new IllegalArgumentException("Schedule for this day already exists");
            }

            Schedule schedule = scheduleMapper.toEntity(request, employee);
            Schedule savedSchedule = scheduleRepository.save(schedule);
            return scheduleMapper.toResponse(savedSchedule);

        } catch (DataIntegrityViolationException ex) {
            log.error("Database constraint violation while creating a schedule on {} for employee {}", request.getWeekDay(), employeeId, ex);
            throw new IllegalStateException("A schedule conflict occurred. Another process may have modified this schedule simultaneously.", ex);
        }
    }

    @Transactional
    public List<ScheduleResponse> fetchScheduleForEmployee(Long employeeId) {
        getEmployeeOrThrow(employeeId);
        log.info("Getting schedule for employee with ID: {}", employeeId);

        List<Schedule> existingSchedule = scheduleRepository.findByEmployeeIdAndIsActiveTrue(employeeId);
        return scheduleMapper.toResponseList(existingSchedule);
    }

    @Transactional
    public ScheduleShortResponse updateSchedule(ScheduleRequest request) {
        validateNonNullRequest(request, "update");
        Long employeeId = request.getEmployeeId();
        log.info("Updating schedule on {} for employee with ID: {}", request.getWeekDay(), employeeId);

        getEmployeeOrThrow(employeeId);

        try {
            Schedule existingSchedule = scheduleRepository.findByEmployeeIdAndWeekDayAndIsActiveTrue(employeeId, request.getWeekDay())
                    .orElseThrow(() -> new IllegalArgumentException("Schedule for this day does not exist"));

            scheduleMapper.updateEntityFromDto(request, existingSchedule);

            Schedule savedSchedule = scheduleRepository.save(existingSchedule);
            return scheduleMapper.toShortResponse(savedSchedule);

        } catch (DataIntegrityViolationException ex) {
            log.error("Database constraint violation while updating schedule for employee {}", employeeId, ex);
            throw new IllegalStateException("A schedule conflict occurred. Another process may have modified this schedule simultaneously.", ex);
        }
    }

    @Transactional
    public String deleteScheduleForADay(Long employeeId, Day day) {
        log.info("Deleting schedule on {} for employee with ID: {}", day, employeeId);
        getEmployeeOrThrow(employeeId);

        try {
            Schedule existingSchedule = scheduleRepository.findByEmployeeIdAndWeekDayAndIsActiveTrue(employeeId, day)
                    .orElseThrow(() -> new IllegalArgumentException("Schedule for this day does not exist"));

            scheduleRepository.delete(existingSchedule);
            return String.format("Successfully deleted schedule on %s for employee with ID: %s", day, employeeId);
        } catch (DataIntegrityViolationException ex) {
            log.error("Database constraint violation while deleting schedule for employee {}", employeeId, ex);
            throw new IllegalStateException("A schedule conflict occurred. Another process may have modified this schedule simultaneously.", ex);
        }
    }

    private String checkIfSchedulesExists(Long employeeID, List<ScheduleRequest> requests) {
        List<String> conflicts = new ArrayList<>();
        for (ScheduleRequest request : requests) {
            if (scheduleExists(employeeID, request)) {
                conflicts.add(String.format("%s (%s-%s)",
                        request.getWeekDay(), request.getWorkDayStart(), request.getWorkDayEnd()));
            }
        }
        return conflicts.isEmpty() ? "" : "Schedule conflicts detected on existing database entries for: " + String.join(", ", conflicts);
    }

    private boolean scheduleExists(Long employeeId, ScheduleRequest scheduleRequest) {
        return scheduleRepository.existsOverlappingSchedule(
                employeeId,
                scheduleRequest.getWeekDay(),
                scheduleRequest.getWorkDayStart(),
                scheduleRequest.getWorkDayEnd()
        );
    }

    private User getEmployeeOrThrow(Long employeeId) {
        return userRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee with ID: " + employeeId + " does not exist"));
    }

    private void validateNonNullRequest(ScheduleRequest request, String action) {
        if (request == null) {
            log.warn("Received an empty schedule to {}.", action);
            throw new IllegalArgumentException("Received an empty schedule.");
        }
    }
}