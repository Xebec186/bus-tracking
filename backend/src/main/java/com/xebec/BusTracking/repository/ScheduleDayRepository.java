package com.xebec.BusTracking.repository;

import com.xebec.BusTracking.model.ScheduleDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleDayRepository extends JpaRepository<ScheduleDay, Long> {
    Optional<ScheduleDay> findByScheduleIdAndDay(Long scheduleId, DayOfWeek dayOfWeek);
    List<ScheduleDay> findByScheduleId(Long scheduleId);

    List<ScheduleDay> findByScheduleIdIn(Collection<Long> scheduleIds);
}
