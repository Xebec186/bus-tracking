package com.xebec.BusTracking.service.impl;

import com.xebec.BusTracking.dto.ScheduleDayDto;
import com.xebec.BusTracking.exception.ResourceNotFoundException;
import com.xebec.BusTracking.model.Schedule;
import com.xebec.BusTracking.model.ScheduleDay;
import com.xebec.BusTracking.repository.ScheduleDayRepository;
import com.xebec.BusTracking.repository.ScheduleRepository;
import com.xebec.BusTracking.service.ScheduleDayService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ScheduleDayServiceImpl implements ScheduleDayService {

    private final ScheduleDayRepository scheduleDayRepository;
    private final ScheduleRepository scheduleRepository;
    private final ModelMapper modelMapper;

    @Override
    public ScheduleDayDto addScheduleDay(ScheduleDayDto scheduleDayDto) {
        ScheduleDay scheduleDay = modelMapper.map(scheduleDayDto, ScheduleDay.class);

        Long scheduleId = scheduleDayDto.getScheduleId();
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with given id: " + scheduleId));
        scheduleDay.setSchedule(schedule);

        ScheduleDay addedScheduleDay = scheduleDayRepository.save(scheduleDay);
        return modelMapper.map(addedScheduleDay, ScheduleDayDto.class);
    }

    @Override
    public ScheduleDayDto getScheduleDayById(Long scheduleDayId) {
        ScheduleDay scheduleDay = scheduleDayRepository.findById(scheduleDayId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not on day: " + scheduleDayId));
        return modelMapper.map(scheduleDay, ScheduleDayDto.class);
    }

    @Override
    public List<ScheduleDayDto> getAllScheduleDays() {
        return scheduleDayRepository.findAll().stream()
                .map(scheduleDay -> modelMapper.map(scheduleDay, ScheduleDayDto.class))
                .toList();
    }

    @Override
    public List<ScheduleDayDto> getAllScheduleDaysByScheduleId(Long scheduleId) {
        return scheduleDayRepository.findByScheduleId(scheduleId).stream()
                .map(scheduleDay -> modelMapper.map(scheduleDay, ScheduleDayDto.class))
                .toList();
    }

    @Override
    public ScheduleDayDto updateScheduleDay(Long scheduleDayId, ScheduleDayDto scheduleDayDto) {
        ScheduleDay scheduleDay = scheduleDayRepository.findById(scheduleDayId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not on day: " + scheduleDayId));

        Long scheduleId = scheduleDayDto.getScheduleId();
        if(!Objects.equals(scheduleDay.getSchedule().getId(), scheduleId)) {
            Schedule schedule = scheduleRepository.findById(scheduleId)
                    .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with given id: " + scheduleId));
            scheduleDay.setSchedule(schedule);
        }

        scheduleDay.setDay(scheduleDayDto.getDayOfWeek());
        scheduleDay.setDepartureTime(scheduleDayDto.getDepartureTime());
        scheduleDay.setArrivalTime(scheduleDay.getArrivalTime());

        ScheduleDay updatedScheduleDay = scheduleDayRepository.save(scheduleDay);
        return modelMapper.map(updatedScheduleDay, ScheduleDayDto.class);
    }

    @Override
    public void deleteScheduleDay(Long scheduleDayId) {
        ScheduleDay scheduleDay = scheduleDayRepository.findById(scheduleDayId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not on day: " + scheduleDayId));
        scheduleDayRepository.delete(scheduleDay);
    }
}
