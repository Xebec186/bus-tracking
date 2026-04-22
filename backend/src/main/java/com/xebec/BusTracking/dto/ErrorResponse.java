package com.xebec.BusTracking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@AllArgsConstructor
@Builder
public class ErrorResponse {
    private String message;
    private int status;
    private Map<String, String> errors;
    private LocalDateTime timestamp;
}
