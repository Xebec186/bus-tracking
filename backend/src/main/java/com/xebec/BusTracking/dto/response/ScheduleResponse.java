package com.xebec.BusTracking.dto.response;

import com.xebec.BusTracking.model.Schedule;
import com.xebec.BusTracking.model.ScheduleDay;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Builder
@Getter
@Setter
public class ScheduleResponse {
    private Long id;
    private Long routeId;
    private Long busId;
    private String scheduleDay;
    private LocalTime departureTime;
    private LocalTime arrivalTime;
    private LocalDate expiryDate;
    private LocalDate effectiveDate;
    private String status;

    public static ScheduleResponse from(Schedule schedule, ScheduleDay scheduleDay) {
        return ScheduleResponse.builder()
                .id(schedule.getId())
                .routeId(schedule.getRoute().getId())
                .busId(schedule.getBus().getId())
                .scheduleDay(scheduleDay.getDay().toString())
                .departureTime(scheduleDay.getDepartureTime())
                .arrivalTime(scheduleDay.getArrivalTime())
                .expiryDate(schedule.getExpiryDate())
                .effectiveDate(schedule.getEffectiveDate())
                .status(schedule.getStatus().toString())
                .build();
    }
}
