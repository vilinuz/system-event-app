package com.monitor.controller.dto;

import com.monitor.domain.Environment;

public record ServiceRequest(
        String name,
        Environment environment,
        String owner
) {}
