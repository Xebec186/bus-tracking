package com.xebec.BusTracking.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RouteDto {
    private Long id;

    @NotBlank(message = "Route name is required")
    @Size(max = 100, message = "Route name cannot exceed 100 characters")
    private String name;

    private String number;

    @NotNull(message = "Distance is required")
    @DecimalMin(value = "0.1", message = "Distance must be at least 0.1 km")
    @DecimalMax(value = "9999.99", message = "Distance cannot exceed 9999.99 km")
    @Digits(integer = 4, fraction = 2, message = "Distance must have max 4 digits before and 2 after decimal")
    private BigDecimal distanceKm;

    @NotNull(message = "Estimated duration is required")
    @Min(value = 5, message = "Estimated duration must be at least 5 minutes")
    @Max(value = 300, message = "Estimated duration cannot exceed 300 minutes")
    private Integer estimatedDurationMinutes;

    // --- View-only fields used by Thymeleaf templates ---
    private String startStopName;
    private String endStopName;
    private Integer stopCount;
}
