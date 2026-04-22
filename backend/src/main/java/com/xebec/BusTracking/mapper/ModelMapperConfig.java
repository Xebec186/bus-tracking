package com.xebec.BusTracking.mapper;

import com.xebec.BusTracking.dto.*;
import com.xebec.BusTracking.model.*;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        // ---------------------------
        // RouteStop → RouteStopDto
        // ---------------------------
        modelMapper.typeMap(RouteStop.class, RouteStopDto.class)
                .addMappings(mapper -> {
                    mapper.map(src -> src.getRoute().getId(), RouteStopDto::setRouteId);
                    mapper.map(src -> src.getStop().getId(), RouteStopDto::setStopId);
                });

        // ---------------------------
        // Bus → BusDto
        // ---------------------------
        modelMapper.typeMap(Bus.class, BusDto.class)
                .addMappings(mapper -> {
                    mapper.map(src -> src.getDriver() != null ? src.getDriver().getId() : null, BusDto::setDriverId);
                });

        // ---------------------------
        // BusLocation → BusLocationDto
        // ---------------------------
        modelMapper.typeMap(BusLocation.class, BusLocationDto.class)
                .addMappings(mapper -> {
                    mapper.map(src -> src.getBus().getId(), BusLocationDto::setBusId);
                });

        // ---------------------------
        // Schedule → ScheduleDto
        // ---------------------------
        modelMapper.typeMap(Schedule.class, ScheduleDto.class)
                .addMappings(mapper -> {
                    mapper.map(src -> src.getBus().getId(), ScheduleDto::setBusId);
                    mapper.map(src -> src.getRoute().getId(), ScheduleDto::setRouteId);
                });

        // ---------------------------
        // Ticket → TicketDto
        // ---------------------------
        modelMapper.typeMap(Ticket.class, TicketDto.class)
                .addMappings(mapper -> {
                    mapper.map(src -> src.getSchedule().getId(), TicketDto::setScheduleId);
                    mapper.map(src -> src.getPassenger().getId(), TicketDto::setPassengerId);
                    mapper.map(src -> src.getOriginStop().getId(), TicketDto::setOriginStopId);
                    mapper.map(src -> src.getDestinationStop().getId(), TicketDto::setDestinationStopId);
                });

        // ---------------------------
        // Trip → TripDto
        // ---------------------------
        modelMapper.typeMap(Trip.class, TripDto.class)
                .addMappings(mapper -> {
                    mapper.map(src -> src.getSchedule().getId(), TripDto::setScheduleId);
                    mapper.map(src -> src.getBus().getId(), TripDto::setBusId);
                });


        // ---------------------------
        // ScheduleDay → ScheduleDayDto
        // ---------------------------
        modelMapper.typeMap(ScheduleDay.class, ScheduleDayDto.class)
                .addMappings(mapper -> {
                    mapper.map(src -> src.getSchedule().getId(), ScheduleDayDto::setScheduleId);
                });

        return modelMapper;
    }
}