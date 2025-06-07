package com.dipper.monitor.task.template;


import com.dipper.monitor.annotation.quartz.QuartzJob;
import com.dipper.monitor.entity.elastic.policy.response.LifePolicyResponse;
import com.dipper.monitor.service.elastic.policy.LifePolicyRealService;
import com.dipper.monitor.service.elastic.policy.LifePolicyStoreService;
import com.dipper.monitor.task.AbstractITask;
import com.dipper.monitor.task.ITask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
public class PolicyRefreshTask  extends AbstractITask  {

    @Autowired
    private LifePolicyStoreService lifePolicyStoreService;
    @Autowired
    private LifePolicyRealService lifePolicyRealService;

    // 每10分钟执行一次
    public void policyRefresh() {
        try {
            lifePolicyRealService.policyAllRefresh();
        }catch (Exception e){
            log.error("生命周期执行异常",e);
        }
    }

    private void doWork() {
        List<LifePolicyResponse> allPolicies = lifePolicyStoreService.getAllPolicies();
        if(allPolicies == null || allPolicies.isEmpty()){
            return;
        }
        for (LifePolicyResponse lifePolicyResponse : allPolicies) {
            Integer id = lifePolicyResponse.getId();
            try {
                lifePolicyRealService.policyEffective(id);
            } catch (IOException e) {
                log.error("策略生效失败",e);
            }
        }
    }

    @Override
    public String getCron() {
        return "0 0/30 * * * ?";
    }

    @Override
    public void setCron(String cron) {

    }

    @Override
    public String getAuthor() {
        return "lcc";
    }

    @Override
    public String getJobDesc() {
        return "生命周期策略刷新";
    }

    @Override
    public boolean isEditable() {
        return false;
    }

    @Override
    public void execute() {
        policyRefresh();
    }

    @Override
    public String getTaskName() {
        return "policyRefresh";
    }
}
