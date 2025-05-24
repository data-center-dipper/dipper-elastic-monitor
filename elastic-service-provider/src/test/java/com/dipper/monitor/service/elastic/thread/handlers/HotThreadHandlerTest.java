package com.dipper.monitor.service.elastic.thread.handlers;

import org.junit.Test;

import static org.junit.Assert.*;

public class HotThreadHandlerTest {


    @Test
    public void parseHotThreadsResponse() {
        HotThreadHandler handler = new HotThreadHandler(null);

        String data = "::: {1.es3}{tEq2FE4oTwmBptZ5FYQm8w}{eEgDpFa4SOiyYuyJKuTbvg}{78.118.1.34}{78.118.1.34:9300}{cdfhimrstw}{xpack.installed=true, transform.node=true}\n" +
                "    Hot threads at 2025-05-24T06:28:59.029Z, interval=500ms, busiestThreads=3, ignoreIdleThreads=true:\n" +
                "\n" +
                "::: {1.es1}{UyTW8SeuQSeLWC8eZ9ISDg}{vQXMQcvOSp6uBxLVpqW82g}{78.118.1.36}{78.118.1.36:9300}{cdfhimrstw}{xpack.installed=true, transform.node=true}\n" +
                "    Hot threads at 2025-05-24T06:28:59.029Z, interval=500ms, busiestThreads=3, ignoreIdleThreads=true:";
        handler.parseHotThreadsResponse(data);
    }
}