package com.example.booking_system.schedule;

import com.example.booking_system.schedule.dto.ScheduleRequest;
import com.example.booking_system.schedule.dto.ScheduleResponse;
import com.example.booking_system.user.User;
import com.example.booking_system.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final ScheduleMapper scheduleMapper;
    private final UserRepository userRepository;

    @Transactional

    public List<ScheduleResponse> createSchedule(List<ScheduleRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            log.warn("Received an empty schedule creation request list.");
            return List.of();
        }

        Long employeeId = requests.getFirst().getEmployeeId();
        log.info("Creating {} new schedule for employee with ID: {}", requests.size(), employeeId);

        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee with ID: " + employeeId + "does not exists"));

        List<Schedule> scheduleTemplates = requests.stream()
                .map(request -> scheduleMapper.toEntity(request,employee))
                .toList();
        List<Schedule> savedTemplates = scheduleRepository.saveAll(scheduleTemplates);

        return scheduleMapper.toResponseList(savedTemplates);
    }


    private ScheduleResponse updateSchedule(ScheduleRequest request) {

    }



}
