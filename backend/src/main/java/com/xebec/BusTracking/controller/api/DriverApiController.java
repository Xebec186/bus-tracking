package com.xebec.BusTracking.controller.api;

import com.xebec.BusTracking.dto.TicketDto;
import com.xebec.BusTracking.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/driver")
public class DriverApiController {
    private final TicketService ticketService;

    @PostMapping("/tickets/{code}/validate")
    public TicketDto validateTicket(@PathVariable String code) {
        return ticketService.validateTicket(code);
    }
}

