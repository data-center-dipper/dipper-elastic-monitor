package com.dipper.monitor.controller.elastic.data;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.data.ProgressInfo;
import com.dipper.monitor.service.elastic.data.DataProcessService;
import com.dipper.monitor.utils.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/dipper/monitor/api/v1/elastic/data_process")
public class DataProcessController {


    @Autowired
    private DataProcessService dataProcessService;

    /**
     * 节点下线
     */
    @GetMapping("/node_offline/{nodeName}")
    public JSONObject nodeOfflineApi(@PathVariable String nodeName) {
        try {
            dataProcessService.nodeOfflineApi(nodeName);
            return ResultUtils.onSuccess();
        } catch (IllegalArgumentException e) {
            log.error("节点下线", e);
            return ResultUtils.onFail(e.getMessage());
        } catch (Exception e) {
            log.error("节点下线", e);
            return ResultUtils.onFail("节点下线: " + e.getMessage());
        }
    }

    /**
     * 获取节点下线任务
     */
    @GetMapping("/node_offline/task_state")
    public JSONObject nodeOfflineState() {
        try {
            String progress = dataProcessService.nodeOfflineState();
            return ResultUtils.onSuccess(progress);
        } catch (IllegalArgumentException e) {
            log.error("节点下线", e);
            return ResultUtils.onFail(e.getMessage());
        } catch (Exception e) {
            log.error("节点下线", e);
            return ResultUtils.onFail("节点下线: " + e.getMessage());
        }
    }
}
