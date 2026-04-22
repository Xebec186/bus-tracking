package com.xebec.BusTracking.controller.api;

import com.xebec.BusTracking.dto.ScheduleDayDto;
import com.xebec.BusTracking.dto.ScheduleDto;
import com.xebec.BusTracking.service.ScheduleDayService;
import com.xebec.BusTracking.service.ScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/schedules")
public class ScheduleApiController {
    private final ScheduleService scheduleService;
    private final ScheduleDayService scheduleDayService;

    @GetMapping
    public List<ScheduleDto> getAllSchedules() {
        return scheduleService.getAllSchedules();
    }

    @PostMapping
    public ScheduleDto createSchedule(@Valid @RequestBody ScheduleDto scheduleDto) {
        return scheduleService.addSchedule(scheduleDto);
    }

    @GetMapping("/{scheduleId}")
    public ScheduleDto getSchedule(@PathVariable Long scheduleId) {
        return scheduleService.getScheduleById(scheduleId);
    }

    @PutMapping("/{scheduleId}")
    public ScheduleDto updateSchedule(@PathVariable Long scheduleId, @Valid @RequestBody ScheduleDto scheduleDto) {
        return scheduleService.updateSchedule(scheduleId, scheduleDto);
    }

    @DeleteMapping("/{scheduleId}")
    public void deleteSchedule(@PathVariable Long scheduleId) {
        scheduleService.deleteSchedule(scheduleId);
    }

    @GetMapping("/{scheduleId}/days")
    public List<ScheduleDayDto> getScheduleDays(@PathVariable Long scheduleId) {
        return scheduleDayService.getAllScheduleDaysByScheduleId(scheduleId);
    }

    @PostMapping("/{scheduleId}/days")
    public ScheduleDayDto addScheduleDay(@PathVariable Long scheduleId, @Valid @RequestBody ScheduleDayDto scheduleDayDto) {
        scheduleDayDto.setScheduleId(scheduleId);
        return scheduleDayService.addScheduleDay(scheduleDayDto);
    }
}
