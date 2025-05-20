package com.dipper.monitor.controller.elsatic.slowsearch;

import com.dipper.monitor.service.elastic.slowsearch.SlowSearchService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/dipper/monitor/api/v1/elastic/slow_search")
@Tag(name = "慢查询相关", description = "慢查询相关")
public class SlowSearchController {

    @Autowired
    private SlowSearchService slowSearchService;
}
