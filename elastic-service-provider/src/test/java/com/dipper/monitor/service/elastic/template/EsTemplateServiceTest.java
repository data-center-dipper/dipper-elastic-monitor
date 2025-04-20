package com.dipper.monitor.service.elastic.template;

import com.dipper.monitor.BaseMonitorTest;
import com.dipper.monitor.entity.elastic.template.EsTemplate;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

public class EsTemplateServiceTest extends BaseMonitorTest {

    @Autowired
    private EsTemplateService esTemplateService;

    @Test
    public void addTemplate() {
        EsTemplate esTemplate = new EsTemplate();
        esTemplate.setEnName("ailpha-log");
        esTemplate.setZhName("原始日志");
        esTemplate.setTemplateContent("{}");
        esTemplateService.addTemplate(esTemplate);
    }

    @Test
    public void getTemplate() {
    }

    @Test
    public void updateTemplate() {
    }
}