package com.xebec.BusTracking.service;

import com.xebec.BusTracking.dto.ScheduleDto;
import com.xebec.BusTracking.dto.response.ScheduleResponse;

import java.util.List;

public interface ScheduleService {
    ScheduleDto addSchedule(ScheduleDto scheduleDto);

    ScheduleDto getScheduleById(Long scheduleId);

    List<ScheduleDto> getAllSchedules();

    List<ScheduleResponse> getSchedulesByRouteId(Long routeId);

    List<ScheduleDto> getSchedulesByBusId(Long busId);

    List<ScheduleDto> getActiveSchedules();

    ScheduleDto updateSchedule(Long scheduleId, ScheduleDto scheduleDto);

    void deleteSchedule(Long scheduleId);
}