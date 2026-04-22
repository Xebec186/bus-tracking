package com.xebec.BusTracking.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FareEstimateRequest {
    @NotNull
    private Long scheduleId;

    @NotNull
    private Long originStopId;

    @NotNull
    private Long destinationStopId;
}
