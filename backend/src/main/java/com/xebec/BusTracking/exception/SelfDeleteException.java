package com.xebec.BusTracking.exception;

public class SelfDeleteException extends RuntimeException {
    public SelfDeleteException() {
        super("Administrators cannot delete their own account.");
    }
}
