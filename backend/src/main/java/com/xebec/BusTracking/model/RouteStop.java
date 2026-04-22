package com.xebec.BusTracking.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA Entity for Route_Stops junction table.
 * Represents the many-to-many relationship between Routes and Stops
 * with additional ordering and timing information
 */
@Entity
@Table(name = "route_stops"
//    uniqueConstraints = {
//        @UniqueConstraint(name = "unique_route_stop",
//                         columnNames = {"route_id", "stop_id"}),
//        @UniqueConstraint(name = "unique_route_sequence",
//                         columnNames = {"route_id", "stop_sequence"})
//    }
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RouteStop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stop_id", nullable = false)
    private Stop stop;

    @Column(nullable = false)
    private Integer stopSequence;

    @Column(nullable = false)
    private Integer estimatedArrivalMinutes;
}