package com.dipper.monitor.service.elastic.template.impl;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.beans.SpringUtil;
import com.dipper.monitor.entity.elastic.cluster.CurrentClusterEntity;
import com.dipper.monitor.entity.elastic.template.unconverted.EsUnconvertedTemplate;
import com.dipper.monitor.service.elastic.cluster.ElasticClusterManagerService;
import com.dipper.monitor.service.elastic.template.ElasticStoreTemplateService;
import com.dipper.monitor.service.elastic.template.TemplatePreviewService;
import com.dipper.monitor.service.elastic.template.impl.handlers.preview.Preview7xTemplateHandler;
import com.dipper.monitor.service.elastic.template.impl.handlers.preview.PreviewCanRunTemplateHandler;
import com.dipper.monitor.service.elastic.template.impl.handlers.preview.Preview8xTemplateHandler;
import com.dipper.monitor.utils.elastic.ClusterVersionUtils;
import com.dipper.monitor.utils.elastic.ElasticBeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TemplatePreviewServiceImpl implements TemplatePreviewService {

    @Autowired
    private ElasticStoreTemplateService elasticStoreTemplateService;

    @Override
    public JSONObject previewTemplate(EsUnconvertedTemplate esUnconvertedTemplate) {
        CurrentClusterEntity currentCluster = ElasticBeanUtils.getCurrentCluster();
        String clusterVersion = currentCluster.getClusterVersion();
        if(ClusterVersionUtils .is7xVersion(clusterVersion)){
            Preview7xTemplateHandler preview7xTemplateHandler = new Preview7xTemplateHandler();
            return preview7xTemplateHandler.previewTemplate(esUnconvertedTemplate);
        }
        ClusterVersionUtils .is8xVersion(clusterVersion);
        Preview8xTemplateHandler preview8xTemplateHandler = new Preview8xTemplateHandler();
        return preview8xTemplateHandler.previewTemplate(esUnconvertedTemplate);
    }

    /**
     * 预览模板,可以直接使用的模版信息
     *
     * @param id
     * @return
     */
    @Override
    public JSONObject previewEffectTemplate(Integer id) {
        EsUnconvertedTemplate oneUnconvertedTemplate = elasticStoreTemplateService.getOneUnconvertedTemplate(id);
        if(oneUnconvertedTemplate == null) {
            throw new RuntimeException("模板不存在");
        }
        JSONObject jsonObject = previewTemplate(oneUnconvertedTemplate);
        PreviewCanRunTemplateHandler previewCanRunTemplateHandler = new PreviewCanRunTemplateHandler();
        return previewCanRunTemplateHandler.previewCanRunTemplate(jsonObject);
    }

    @Override
    public JSONObject previewEffectTemplateByDate(Integer id, String date) {
        EsUnconvertedTemplate oneUnconvertedTemplate = elasticStoreTemplateService.getOneUnconvertedTemplate(id);
        if(oneUnconvertedTemplate == null) {
            throw new RuntimeException("模板不存在");
        }
        JSONObject jsonObject = previewTemplate(oneUnconvertedTemplate);
        PreviewCanRunTemplateHandler previewCanRunTemplateHandler = new PreviewCanRunTemplateHandler();
        return previewCanRunTemplateHandler.previewCanRunTemplate(jsonObject,date);
    }

}
