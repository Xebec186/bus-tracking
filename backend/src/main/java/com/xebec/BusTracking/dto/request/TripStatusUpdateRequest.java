package com.xebec.BusTracking.dto.request;

import com.xebec.BusTracking.model.TripStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TripStatusUpdateRequest {
    @NotNull
    private TripStatus status;
}
