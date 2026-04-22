package com.xebec.BusTracking.repository;

import com.xebec.BusTracking.model.Schedule;
import com.xebec.BusTracking.model.ScheduleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByStatus(ScheduleStatus status);
    List<Schedule> findByRouteId(Long routeId);

    @Query("SELECT DISTINCT s FROM Schedule s " +
            "JOIN FETCH s.scheduleDays " +
            "WHERE s.route.id = :routeId AND s.status = com.xebec.BusTracking.model.ScheduleStatus.ACTIVE")
    List<Schedule> findActiveSchedulesWithDays(@Param("routeId") Long routeId);

    List<Schedule> findByBusId(Long busId);
    List<Schedule> findByExpiryDateBefore(LocalDate date);
    List<Schedule> findByEffectiveDateAndStatus(LocalDate date, ScheduleStatus status);
}
