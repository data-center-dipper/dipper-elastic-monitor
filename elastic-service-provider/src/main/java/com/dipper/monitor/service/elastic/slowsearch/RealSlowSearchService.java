package com.dipper.monitor.service.elastic.slowsearch;

import com.dipper.monitor.entity.db.elastic.SlowQueryEntity;
import com.dipper.monitor.entity.elastic.slowsearch.task.SlowQueryTaskEntity;

import java.io.IOException;
import java.util.List;

public interface RealSlowSearchService {

    List<SlowQueryEntity> getRelaNodesSlowQuery() throws IOException;

    List<SlowQueryTaskEntity> getRelaSlowQuery() throws IOException;

    List<SlowQueryEntity> transToSlowQueryEntity(List<SlowQueryTaskEntity> taskSlowQueries);
}
