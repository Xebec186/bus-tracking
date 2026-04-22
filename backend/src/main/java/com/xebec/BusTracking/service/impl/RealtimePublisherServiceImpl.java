package com.xebec.BusTracking.service.impl;

import com.xebec.BusTracking.service.RealtimeFeedService;
import com.xebec.BusTracking.service.RealtimePublisherService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RealtimePublisherServiceImpl implements RealtimePublisherService {
    private final SimpMessagingTemplate messagingTemplate;
    private final RealtimeFeedService realtimeFeedService;

    @Override
    public void publishTrackingUpdate() {
        messagingTemplate.convertAndSend("/topic/tracking/locations", realtimeFeedService.getTrackingPayload());
    }

    @Override
    public void publishDashboardMetrics() {
        messagingTemplate.convertAndSend("/topic/dashboard/metrics", realtimeFeedService.getDashboardPayload());
    }
}
