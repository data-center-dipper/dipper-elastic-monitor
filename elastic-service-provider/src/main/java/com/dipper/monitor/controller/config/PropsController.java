package com.dipper.monitor.controller.config;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.db.config.ConfItemEntity;
import com.dipper.monitor.service.config.PropsService;
import com.dipper.monitor.utils.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping({"/dipper/api/v1/monitor/props_config"})
public class PropsController {

    @Autowired
    private PropsService propsService;

    @GetMapping({"/config_list"})
    public JSONObject configList() {
        try {
            List<ConfItemEntity> configList = propsService.getConfigList();
            return ResultUtils.onSuccess(configList);
        } catch (Exception e) {
            log.error("查询异常:",e);
            return ResultUtils.onFail();
        }
    }

    @GetMapping({"/config_update"})
    public JSONObject addOrUpdateConfig(@RequestBody ConfItemEntity confItemEntity) {
        try {
            propsService.saveOrUpdate(confItemEntity);
            return ResultUtils.onSuccess();
        } catch (Exception e) {
            log.error("查询异常:",e);
            return ResultUtils.onFail();
        }
    }


}
