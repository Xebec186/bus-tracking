package com.xebec.BusTracking.service;

public interface RealtimePublisherService {
    void publishTrackingUpdate();

    void publishDashboardMetrics();
}
