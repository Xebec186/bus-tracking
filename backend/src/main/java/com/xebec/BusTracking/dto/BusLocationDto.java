package com.xebec.BusTracking.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BusLocationDto {

    private Long id;

    private Long busId;

    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    @Digits(integer = 2, fraction = 8, message = "Latitude precision: 2 digits before, 8 after decimal")
    private BigDecimal latitude;

    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    @Digits(integer = 3, fraction = 8, message = "Longitude precision: 3 digits before, 8 after decimal")
    private BigDecimal longitude;

    @DecimalMin(value = "0.0", message = "Speed cannot be negative")
    @DecimalMax(value = "120.0", message = "Speed cannot exceed 120 km/h")
    @Digits(integer = 3, fraction = 2, message = "Speed precision: 3 digits before, 2 after decimal")
    private BigDecimal speed;

    @DecimalMin(value = "0.0", message = "Heading must be between 0 and 360")
    @DecimalMax(value = "360.0", message = "Heading must be between 0 and 360")
    @Digits(integer = 3, fraction = 2, message = "Heading precision: 3 digits before, 2 after decimal")
    private BigDecimal heading;

    @NotNull(message = "Timestamp is required")
    private LocalDateTime timestamp;
}
