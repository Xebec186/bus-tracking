package com.xebec.BusTracking.exception;

public class SelfPrivilegeChangeException extends RuntimeException {
    public SelfPrivilegeChangeException(String message) {
        super(message);
    }
}
