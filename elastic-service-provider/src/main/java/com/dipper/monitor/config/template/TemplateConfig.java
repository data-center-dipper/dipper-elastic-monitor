package com.dipper.monitor.config.template;

import com.dipper.monitor.utils.elastic.ClusterVersionUtils;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Data
@Configuration
public class TemplateConfig {
    /**
     * 预制模版路径
     */
    @Value("${template.base.path}")
    private String templateBasePath;

    private String separator = File.separator;

    public String getInnerTemplatePath(String clusterVersion) {
        if (ClusterVersionUtils.is7xVersion(clusterVersion)) {
            return templateBasePath + separator + "7x" + separator + "inner";
        }else if (ClusterVersionUtils.is8xVersion(clusterVersion)) {
            return templateBasePath + separator + "8x" + separator + "inner";
        }else {
            return templateBasePath + separator + "inner";
        }
    }

    public String getInnerPolicyPath(String clusterVersion) {
        if (ClusterVersionUtils.is7xVersion(clusterVersion)) {
            return templateBasePath + separator + "7x" + separator + "policy";
        }else if (ClusterVersionUtils.is8xVersion(clusterVersion)) {
            return templateBasePath + separator + "8x" + separator + "policy";
        }else {
            return templateBasePath + separator + "policy";
        }
    }

    public String getPrefabricateTemplatePath(String clusterVersion) {
        if (ClusterVersionUtils.is7xVersion(clusterVersion)) {
            return templateBasePath + separator + "7x" + separator + "prefabricate";
        }else if (ClusterVersionUtils.is8xVersion(clusterVersion)) {
            return templateBasePath + separator + "8x" + separator + "prefabricate";
        }else {
            return templateBasePath + separator + "prefabricate";
        }
    }
}
