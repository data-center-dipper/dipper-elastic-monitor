package com.dipper.monitor.service.elastic.segment;

import com.dipper.monitor.entity.elastic.segments.SegmentMessage;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ElasticSegmentService {
    Map<String, List<SegmentMessage>> segmentMap();

    List<SegmentMessage> listSegmentByPrefix(String indexPatternPrefix, String indexXing) throws IOException;
}
