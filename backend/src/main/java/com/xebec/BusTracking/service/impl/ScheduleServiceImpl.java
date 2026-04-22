package com.xebec.BusTracking.service.impl;

import com.xebec.BusTracking.dto.ScheduleDto;
import com.xebec.BusTracking.dto.response.ScheduleResponse;
import com.xebec.BusTracking.exception.ResourceNotFoundException;
import com.xebec.BusTracking.model.Bus;
import com.xebec.BusTracking.model.Route;
import com.xebec.BusTracking.model.Schedule;
import com.xebec.BusTracking.model.ScheduleStatus;
import com.xebec.BusTracking.repository.BusRepository;
import com.xebec.BusTracking.repository.RouteRepository;
import com.xebec.BusTracking.repository.ScheduleRepository;
import com.xebec.BusTracking.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final BusRepository busRepository;
    private final RouteRepository routeRepository;
    private final ModelMapper modelMapper;

    @Override
    public ScheduleDto addSchedule(ScheduleDto scheduleDto) {
        Schedule schedule = modelMapper.map(scheduleDto, Schedule.class);

        Long routeId = scheduleDto.getRouteId();
        Long busId = scheduleDto.getBusId();
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found with given id: " + routeId));
        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new ResourceNotFoundException("Bus not found with given id: " + busId));

        schedule.setRoute(route);
        schedule.setBus(bus);

        Schedule addedSchedule = scheduleRepository.save(schedule);

        return modelMapper.map(addedSchedule, ScheduleDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public ScheduleDto getScheduleById(Long scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with given id: " + scheduleId));
        return modelMapper.map(schedule, ScheduleDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScheduleDto> getAllSchedules() {
        return scheduleRepository.findAll().stream()
                .map((schedule) -> modelMapper.map(schedule, ScheduleDto.class))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScheduleResponse> getSchedulesByRouteId(Long routeId) {
        List<Schedule> schedules = scheduleRepository.findActiveSchedulesWithDays(routeId);
        List<ScheduleResponse> scheduleResponses = new ArrayList<>();
        schedules.forEach(schedule -> {
            schedule.getScheduleDays().forEach(scheduleDay -> {
                ScheduleResponse scheduleResponse = ScheduleResponse.from(schedule, scheduleDay);
                scheduleResponses.add(scheduleResponse);
            });
        });
        return scheduleResponses;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScheduleDto> getSchedulesByBusId(Long busId) {
        return scheduleRepository.findByBusId(busId).stream()
                .map((schedule) -> modelMapper.map(schedule, ScheduleDto.class))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScheduleDto> getActiveSchedules() {
        return scheduleRepository.findByStatus(ScheduleStatus.ACTIVE).stream()
                .map((schedule) -> modelMapper.map(schedule, ScheduleDto.class))
                .toList();
    }

    @Override
    public ScheduleDto updateSchedule(Long scheduleId, ScheduleDto scheduleDto) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with given id: " + scheduleId));

        Long busId = scheduleDto.getBusId();
        Long routeId = scheduleDto.getRouteId();

        if(!busId.equals(schedule.getBus().getId())) {
            Bus bus = busRepository.findById(busId)
                    .orElseThrow(() -> new ResourceNotFoundException("Bus not found with given id: " + busId));
            schedule.setBus(bus);
        }

        if(!routeId.equals(schedule.getRoute().getId())) {
            Route route = routeRepository.findById(routeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Route not found with given id: " + routeId));
            schedule.setRoute(route);
        }

        schedule.setEffectiveDate(scheduleDto.getEffectiveDate());
        schedule.setExpiryDate(scheduleDto.getExpiryDate());
        schedule.setStatus(scheduleDto.getStatus());

        Schedule updatedSchedule = scheduleRepository.save(schedule);

        return modelMapper.map(updatedSchedule, ScheduleDto.class);
    }

    @Override
    public void deleteSchedule(Long scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with given id: " + scheduleId));
        scheduleRepository.delete(schedule);
    }
}
