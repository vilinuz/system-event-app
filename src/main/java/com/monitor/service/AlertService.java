package com.monitor.service;

import com.monitor.domain.Event;

public interface AlertService {
    void sendAlert(Event event);
}
