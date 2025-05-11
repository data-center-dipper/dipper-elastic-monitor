package com.dipper.monitor.service.elastic.template.impl.handlers.prefabricate;

import cn.hutool.core.io.FileUtil;
import com.dipper.common.lib.utils.ApplicationUtils;
import com.dipper.monitor.config.plugins.PluginsConfigUtils;
import com.dipper.monitor.config.template.TemplateConfig;
import com.dipper.monitor.entity.elastic.template.unconverted.EsUnconvertedTemplate;
import com.dipper.monitor.entity.elastic.template.unconverted.PrefabricateTemplateEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class PrefabricateTemplateInitHandler {

    private TemplateConfig templateConfig;
    private List<PrefabricateTemplateEntity> esUnconvertedTemplateList = new ArrayList<>();

    public PrefabricateTemplateInitHandler(TemplateConfig templateConfig) {
        this.templateConfig = templateConfig;
    }

    /**
     * 初始化预制模版
     */
    public void initTemplate() {
        try {
            // 获取 classpath 下的 prefabricate 目录
            String templatesDirPath = getTemplatesDirectoryPath();
            File file = new File(templatesDirPath);
            if (FileUtil.exist(file)) {
                File[] files = file.listFiles();
                if (files != null) {
                    for (File f : files) {
                        if (f.isDirectory()) {
                            continue; // 跳过子目录
                        }
                        PrefabricateTemplateEntity template = convertFileToEsUnconvertedTemplate(f.toPath());
                        if (template != null) {
                            esUnconvertedTemplateList.add(template);
                        }
                    }
                }
            } else {
                log.error("模板目录不存在：" + templatesDirPath);
            }
        } catch (Exception e) {
            log.error("初始化预制模版失败", e);
        }
        log.info("预制模版初始化完成, size: {}", esUnconvertedTemplateList.size());
    }

    /**
     * 根据操作系统获取模板目录路径
     */
    private String getTemplatesDirectoryPath() throws IOException, java.net.URISyntaxException {
        String configDirPath = templateConfig.getPrefabricateTemplatePath();
        if (configDirPath == null || configDirPath.isEmpty()) {
            log.error("预制模板配置目录未指定！");
            return null;
        }
        log.info("预制模板配置目录: {}", configDirPath);

        String home = System.getProperty("user.dir");
        log.info("当前工作目录: {}", home);
        String pluginConfig = home + "/" + configDirPath;
        log.info("预制模板配置目录: {}", pluginConfig);
        return pluginConfig;
    }

    /**
     * 将文件内容转换为 EsUnconvertedTemplate 对象
     */
    private PrefabricateTemplateEntity convertFileToEsUnconvertedTemplate(Path path) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
            return objectMapper.readValue(content, PrefabricateTemplateEntity.class);
        } catch (IOException e) {
            log.error("读取或解析模板文件失败：{}", path.toString(), e);
            return null;
        }
    }

    // Getter for esUnconvertedTemplateList
    public List<PrefabricateTemplateEntity> getEsUnconvertedTemplateList() {
        return esUnconvertedTemplateList;
    }
}