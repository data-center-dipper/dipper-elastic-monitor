package com.dipper.monitor.service.elastic.segment.impl;

import com.dipper.monitor.entity.elastic.segments.SegmentMessage;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.segment.ElasticSegmentService;
import com.dipper.monitor.service.elastic.segment.impl.handlers.SegmentMapHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ElasticSegmentServiceImpl implements ElasticSegmentService {

    @Autowired
    private ElasticClientService elasticClientService;

    @Override
    public Map<String, List<SegmentMessage>> segmentMap() {
        SegmentMapHandler segmentMapHandler = new SegmentMapHandler(elasticClientService);
        return segmentMapHandler.segmentMap();
    }
}
