package com.example.booking_system.schedule;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "schedule")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String employeeId;

    @CreatedDate
    @Column(name = "work_day_start", updatable = false)
    private LocalDateTime workDayStart;

    @CreatedDate
    @Column(name = "work_day_end", updatable = false)
    private LocalDateTime workDayEnd;

    @Column
    private int slotGranularityMinutes;
    //TODO: add when implemented
    //@Column
    //private List<Booking> bookings;

}
