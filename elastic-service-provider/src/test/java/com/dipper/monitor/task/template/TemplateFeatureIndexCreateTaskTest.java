package com.dipper.monitor.task.template;

import com.dipper.monitor.BaseMonitorTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;
public class TemplateFeatureIndexCreateTaskTest extends BaseMonitorTest {

    @Autowired
    private TemplateFeatureIndexCreateTask templateFeatureIndexCreateTask;

    @Test
    public void elasticCreateFeatureIndexTask() throws Exception {
        templateFeatureIndexCreateTask.elasticCreateFeatureIndexTask();
    }
}