package com.xebec.BusTracking.exception;

public class TicketValidationException extends RuntimeException {
    public TicketValidationException() {
        super("Ticket is already used or expired");
    }
}
