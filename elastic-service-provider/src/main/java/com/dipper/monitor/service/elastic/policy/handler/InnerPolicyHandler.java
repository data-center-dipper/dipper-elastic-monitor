package com.dipper.monitor.service.elastic.policy.handler;

import cn.hutool.core.io.FileUtil;
import com.dipper.monitor.config.template.TemplateConfig;
import com.dipper.monitor.entity.elastic.policy.InnerPolicyEntity;
import com.dipper.monitor.entity.elastic.template.unconverted.PrefabricateTemplateEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
public class InnerPolicyHandler {

    private TemplateConfig templateConfig;
    private String innerPolicyPath = null;

    private List<InnerPolicyEntity> esPolicyList = new ArrayList<>();

    public InnerPolicyHandler(TemplateConfig templateConfig) {
        this.templateConfig = templateConfig;
        this.innerPolicyPath = templateConfig.getInnerPolicyPath();
    }

    /**
     * 初始化预制模版
     */
    public void initPolicy() {
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
                        InnerPolicyEntity template = convertFileToEsPolicy(f.toPath());
                        if (template != null) {
                            esPolicyList.add(template);
                        }
                    }
                }
            } else {
                log.error("模板目录不存在：" + templatesDirPath);
            }
        } catch (Exception e) {
            log.error("初始化预制模版失败", e);
        }
        log.info("预制模版初始化完成, size: {}", esPolicyList.size());
        esPolicyList.sort(Comparator.comparing(InnerPolicyEntity::getEnName, Comparator.nullsLast(String::compareTo)));
    }

    /**
     * 根据操作系统获取模板目录路径
     */
    private String getTemplatesDirectoryPath() throws IOException, java.net.URISyntaxException {
        if (innerPolicyPath == null || innerPolicyPath.isEmpty()) {
            log.error("预制策略配置目录未指定！");
            return null;
        }
        log.info("预制策略配置目录: {}", innerPolicyPath);
        return innerPolicyPath;
    }

    /**
     * 将文件内容转换为 EsPolicy 对象
     */
    private InnerPolicyEntity convertFileToEsPolicy(Path path) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
            return objectMapper.readValue(content, InnerPolicyEntity.class);
        } catch (IOException e) {
            log.error("读取或解析模板文件失败：{}", path.toString(), e);
            return null;
        }
    }


    public List<InnerPolicyEntity> getEsPolicyList() {
        return esPolicyList;
    }
}
