package com.xebec.BusTracking.service;

import com.xebec.BusTracking.dto.DashboardDto;
import com.xebec.BusTracking.dto.TrackingRealtimePayload;

public interface RealtimeFeedService {
    TrackingRealtimePayload getTrackingPayload();

    DashboardDto getDashboardPayload();
}
