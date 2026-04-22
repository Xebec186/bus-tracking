package com.xebec.BusTracking.dto;

import com.xebec.BusTracking.model.BusStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BusDto {
    private Long id;

    private Long driverId;

    @NotBlank(message = "Registration number is required")
    @Pattern(
            regexp = "^[A-Z]{2}-\\d{4}-\\d{2}$",
            message = "Registration number must follow format: AA-1234-12"
    )
    private String registrationNumber;

    @NotNull(message = "Capacity is required")
    @Min(value = 10, message = "Minimum capacity is 10")
    @Max(value = 100, message = "Maximum capacity is 100")
    private Integer capacity;

    @Size(max = 50, message = "Make cannot exceed 50 characters")
    private String make;

    @Size(max = 50, message = "Model cannot exceed 50 characters")
    private String model;

    private BusStatus status;
}
