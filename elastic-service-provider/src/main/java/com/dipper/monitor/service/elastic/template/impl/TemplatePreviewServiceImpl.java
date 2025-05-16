package com.dipper.monitor.service.elastic.template.impl;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.cluster.CurrentClusterEntity;
import com.dipper.monitor.entity.elastic.template.unconverted.EsUnconvertedTemplate;
import com.dipper.monitor.service.elastic.template.ElasticStoreTemplateService;
import com.dipper.monitor.service.elastic.template.TemplatePreviewService;
import com.dipper.monitor.service.elastic.template.impl.handlers.preview.Preview7xCanRunTemplateHandler;
import com.dipper.monitor.service.elastic.template.impl.handlers.preview.Preview7xTemplateHandler;
import com.dipper.monitor.service.elastic.template.impl.handlers.preview.Preview8xCanRunTemplateHandler;
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
        if(ClusterVersionUtils .is8xVersion(clusterVersion)){
            Preview8xTemplateHandler preview8xTemplateHandler = new Preview8xTemplateHandler();
            return preview8xTemplateHandler.previewTemplate(esUnconvertedTemplate);
        }
        throw new RuntimeException("当前集群版本不支持");
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

        CurrentClusterEntity currentCluster = ElasticBeanUtils.getCurrentCluster();
        String clusterVersion = currentCluster.getClusterVersion();
        if(ClusterVersionUtils .is7xVersion(clusterVersion)){
            Preview7xCanRunTemplateHandler preview7xCanRunTemplateHandler = new Preview7xCanRunTemplateHandler();
            return preview7xCanRunTemplateHandler.previewCanRunTemplate(jsonObject);
        }
        if(ClusterVersionUtils.is8xVersion(clusterVersion)){
            Preview8xCanRunTemplateHandler preview8xCanRunTemplateHandler = new Preview8xCanRunTemplateHandler();
            return preview8xCanRunTemplateHandler.previewCanRunTemplate(jsonObject);
        }
        throw new RuntimeException("当前集群版本不支持");
    }

    @Override
    public JSONObject previewEffectTemplateByDate(Integer id, String date) {
        EsUnconvertedTemplate oneUnconvertedTemplate = elasticStoreTemplateService.getOneUnconvertedTemplate(id);
        if(oneUnconvertedTemplate == null) {
            throw new RuntimeException("模板不存在");
        }
        JSONObject jsonObject = previewTemplate(oneUnconvertedTemplate);

        CurrentClusterEntity currentCluster = ElasticBeanUtils.getCurrentCluster();
        String clusterVersion = currentCluster.getClusterVersion();
        if(ClusterVersionUtils .is7xVersion(clusterVersion)){
            Preview7xCanRunTemplateHandler preview7xCanRunTemplateHandler = new Preview7xCanRunTemplateHandler();
            return preview7xCanRunTemplateHandler.previewCanRunTemplate(jsonObject,date);
        }
        if(ClusterVersionUtils.is8xVersion(clusterVersion)){
            Preview8xCanRunTemplateHandler preview8xCanRunTemplateHandler = new Preview8xCanRunTemplateHandler();
            return preview8xCanRunTemplateHandler.previewCanRunTemplate(jsonObject,date);
        }
        throw new RuntimeException("版本不支持");
    }

}
