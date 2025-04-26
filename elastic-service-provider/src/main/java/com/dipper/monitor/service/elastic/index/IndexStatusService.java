package com.dipper.monitor.service.elastic.index;

import java.io.IOException;

public interface IndexStatusService {
    boolean isIndexFreeze(String index);

    boolean isIndexOpen(String index) throws IOException;

    boolean isIndexClose(String index) throws IOException;
}
