package com.example.booking_system.schedule;


import com.example.booking_system.booking.Booking;
import com.example.booking_system.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.time.LocalTime;
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

    @JoinColumn(name="id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private User employee;

    @Column(name = "work_day_start", updatable = false)
    private LocalTime workDayStart;

    @Column(name = "work_day_end", updatable = false)
    private LocalTime workDayEnd;

    @Column
    private Integer slotGranularityMinutes = 30;

    @Column
    private Day weekDay;

    @Column
    private boolean isActive;

    @Column
    @CreatedDate
    private LocalDateTime createdAt;

    @Column
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
