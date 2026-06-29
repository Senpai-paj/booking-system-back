package com.example.booking_system.schedule;

import com.example.booking_system.schedule.dto.ScheduleRequest;
import com.example.booking_system.schedule.dto.ScheduleResponse;
import com.example.booking_system.schedule.dto.ScheduleShortResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping("/employee/{id}")
    public ResponseEntity<List<ScheduleResponse>> getScheduleById(@PathVariable("id") Long employeeId) {
        List<ScheduleResponse> response = scheduleService.fetchScheduleForEmployee(employeeId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/create/full")
    public ResponseEntity<List<ScheduleResponse>> createFullSchedule(@RequestBody Map<Day, ScheduleRequest> requestMap) {
        List<ScheduleResponse> responses = scheduleService.createSchedule(requestMap);
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    @PostMapping("/create/one")
    public ResponseEntity<ScheduleResponse> createScheduleForOneDay(@RequestBody ScheduleRequest request) {
        ScheduleResponse response = scheduleService.createScheduleForADay(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/update")
    public ResponseEntity<ScheduleShortResponse> updateSchedule(@RequestBody ScheduleRequest request) {
        ScheduleShortResponse response = scheduleService.updateSchedule(request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete/{employeeId}/{day}")
    public ResponseEntity<String> deleteSchedule(@PathVariable Long employeeId, @PathVariable Day day) {
        String response = scheduleService.deleteScheduleForADay(employeeId, day);
        return ResponseEntity.ok(response);
    }


}
