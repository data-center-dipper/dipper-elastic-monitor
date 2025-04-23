package com.dipper.monitor.service.elastic.segment;

import com.dipper.monitor.entity.elastic.segments.SegmentMessage;

import java.util.List;
import java.util.Map;

public interface ElasticSegmentService {
    Map<String, List<SegmentMessage>> segmentMap();
}
