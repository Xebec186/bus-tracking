package com.xebec.BusTracking.model;

public enum TicketStatus {
    PENDING,   // Created but not paid
    PAID,      // Payment confirmed
    USED,      // Ticket has been validated/used
    EXPIRED,   // Validity date passed
    CANCELLED  // Cancelled by user or admin
}
