package com.xebec.BusTracking.repository;

import com.xebec.BusTracking.dto.RecentBookingDto;
import com.xebec.BusTracking.model.Ticket;
import com.xebec.BusTracking.model.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    Optional<Ticket> findByCode(String code);

    @Query("""
    SELECT t FROM Ticket t
    JOIN FETCH t.schedule s
    JOIN FETCH s.route
    JOIN FETCH t.originStop
    JOIN FETCH t.destinationStop
    WHERE t.passenger.id = :passengerId
    """)
    List<Ticket> findByPassengerId(Long passengerId);

    List<Ticket> findByScheduleId(Long scheduleId);

    List<Ticket> findByPassengerIdAndStatusIn(Long passengerId, List<TicketStatus> statuses);

    List<Ticket> findByStatusAndValidityDateBefore(TicketStatus status, LocalDate date);

    @Query("""
    SELECT COUNT(t)
    FROM Ticket t
    WHERE DATE(t.createdAt) = CURRENT_DATE
    """)
    int countTicketsToday();

    @Query("""
    SELECT COUNT(t)
    FROM Ticket t
    WHERE t.createdAt >= :startDate
    GROUP BY FUNCTION('DAYOFWEEK', t.createdAt)
    ORDER BY FUNCTION('DAYOFWEEK', t.createdAt)
    """)
    List<Integer> getWeeklyTicketCounts(@Param("startDate") LocalDateTime startDate);

    @Query("""
    SELECT SUM(t.price)
    FROM Ticket t
    WHERE t.createdAt >= :startDate
    GROUP BY FUNCTION('DAYOFWEEK', t.createdAt)
    ORDER BY FUNCTION('DAYOFWEEK', t.createdAt)
    """)
    List<Double> getWeeklyRevenue(@Param("startDate") LocalDateTime startDate);

    @Query("""
    SELECT new com.xebec.BusTracking.dto.RecentBookingDto(
        CONCAT(t.passenger.firstName, ' ', t.passenger.lastName),
        t.schedule.route.name,
        t.status,
        t.createdAt
    )
    FROM Ticket t
    ORDER BY t.createdAt DESC
    """)
    List<RecentBookingDto> getRecentBookings();

    @Query("""
        SELECT t
        FROM Ticket t
        JOIN FETCH t.schedule s
        JOIN FETCH s.route r
        JOIN FETCH s.bus b
        WHERE t.date >= :startDate
          AND t.date <= :endDate
          AND (:routeId IS NULL OR r.id = :routeId)
          AND (:busId IS NULL OR b.id = :busId)
        """)
    List<Ticket> findTicketsForReport(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("routeId") Long routeId,
            @Param("busId") Long busId
    );
}
