package com.dipper.monitor.service.health.impl;

import com.dipper.monitor.service.health.HealthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class HealthServiceImpl implements HealthService {

    @Override
    public Boolean ping() {
        log.info("接收到health请求");
        return true;
    }
}
