package com.xebec.BusTracking.model;

public enum UserStatus {
    ACTIVE,
    INACTIVE,
    SUSPENDED;

    public String label() {
        return switch (this) {
            case ACTIVE    -> "Active";
            case INACTIVE  -> "Inactive";
            case SUSPENDED -> "Suspended";
        };
    }
}
