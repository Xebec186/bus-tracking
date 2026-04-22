package com.xebec.BusTracking.service;

import com.xebec.BusTracking.dto.ScheduleDayDto;

import java.util.List;

public interface ScheduleDayService {
    ScheduleDayDto addScheduleDay(ScheduleDayDto scheduleDayDto);

    ScheduleDayDto getScheduleDayById(Long scheduleDayId);

    List<ScheduleDayDto> getAllScheduleDays();

    List<ScheduleDayDto> getAllScheduleDaysByScheduleId(Long scheduleId);

    ScheduleDayDto updateScheduleDay(Long scheduleDayId, ScheduleDayDto scheduleDayDto);

    void deleteScheduleDay(Long scheduleDayId);
}
