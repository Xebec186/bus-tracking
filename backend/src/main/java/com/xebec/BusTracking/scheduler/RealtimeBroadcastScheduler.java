package com.xebec.BusTracking.scheduler;

import com.xebec.BusTracking.service.RealtimePublisherService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RealtimeBroadcastScheduler {
    private final RealtimePublisherService realtimePublisherService;

    @Scheduled(fixedDelay = 10000)
    public void pushTrackingFeed() {
        realtimePublisherService.publishTrackingUpdate();
    }

    @Scheduled(fixedDelay = 15000)
    public void pushDashboardMetrics() {
        realtimePublisherService.publishDashboardMetrics();
    }
}
