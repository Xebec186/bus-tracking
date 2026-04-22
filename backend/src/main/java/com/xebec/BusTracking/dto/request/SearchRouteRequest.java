package com.xebec.BusTracking.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchRouteRequest {
    @NotBlank
    private String origin;

    @NotBlank
    private String destination;
}
