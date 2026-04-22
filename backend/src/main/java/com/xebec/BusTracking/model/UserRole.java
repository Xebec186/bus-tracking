package com.xebec.BusTracking.model;

public enum UserRole {
    ADMIN,
    DRIVER,
    PASSENGER;

    /** Display label used in templates */
    public String label() {
        return switch (this) {
            case ADMIN     -> "Administrator";
            case DRIVER    -> "Driver";
            case PASSENGER -> "Passenger";
        };
    }
}
