package com.dipper.monitor.service.elastic.thread;

import com.dipper.monitor.entity.elastic.thread.check.ThreadPoolCheckItem;
import com.dipper.monitor.entity.elastic.thread.check.ThreadPoolStat;
import com.dipper.monitor.entity.elastic.thread.check.yanshi.ThreadPoolItem;

import java.io.IOException;
import java.util.List;

public interface ThreadPoolService {
    List<ThreadPoolItem> fetchThreadPool() throws IOException;
}
