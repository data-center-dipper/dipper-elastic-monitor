package com.dipper.monitor.controller.elsatic.manager_policy;

import com.dipper.monitor.service.elastic.policy.LifePolicyStoreService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/dipper/monitor/api/v1/elastic/policy")
@Tag(name = "ES模板管理", description = "管理和维护Elasticsearch模板")
public class LifePolicyStoreController {

    @Autowired
    private LifePolicyStoreService lifePolicyStoreService;
}
