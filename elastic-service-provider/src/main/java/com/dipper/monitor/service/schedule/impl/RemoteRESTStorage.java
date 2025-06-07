package com.dipper.monitor.service.schedule.impl;

import com.dipper.monitor.entity.task.TaskMetadataEntity;
import com.dipper.monitor.service.schedule.MetadataStorage;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

/**
 * 远程REST API存储实现
 */
@Slf4j
@Service("remoteRESTStorage")
public class RemoteRESTStorage implements MetadataStorage {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Value("${annotation.metadata.remote.url:}")
    private String remoteUrl;

    @Override
    public int saveMetadata(List<TaskMetadataEntity> metadataList) {
        if (metadataList == null || metadataList.isEmpty()) {
            log.warn("没有元数据需要保存");
            return 0;
        }
        
        if (remoteUrl == null || remoteUrl.trim().isEmpty()) {
            log.error("远程URL未配置，无法保存元数据");
            return 0;
        }
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<List<TaskMetadataEntity>> request = new HttpEntity<>(metadataList, headers);
            ResponseEntity<Integer> response = restTemplate.postForEntity(remoteUrl + "/api/metadata/save", request, Integer.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                int count = response.getBody();
                log.info("成功保存 {} 条注解元数据到远程服务器", count);
                return count;
            } else {
                log.error("保存元数据到远程服务器失败: {}", response.getStatusCode());
                return 0;
            }
        } catch (Exception e) {
            log.error("保存注解元数据到远程服务器时发生错误: {}", e.getMessage(), e);
            return 0;
        }
    }

    @Override
    public List<TaskMetadataEntity> findByAnnotationType(String annotationType) {
        if (remoteUrl == null || remoteUrl.trim().isEmpty()) {
            log.error("远程URL未配置，无法查询元数据");
            return Collections.emptyList();
        }
        
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(
                    remoteUrl + "/api/metadata/type/{type}", 
                    String.class, 
                    annotationType);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return objectMapper.readValue(response.getBody(), 
                        new TypeReference<List<TaskMetadataEntity>>() {});
            } else {
                log.error("从远程服务器查询元数据失败: {}", response.getStatusCode());
                return Collections.emptyList();
            }
        } catch (Exception e) {
            log.error("从远程服务器查询注解元数据时发生错误: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<TaskMetadataEntity> findAll() {
        if (remoteUrl == null || remoteUrl.trim().isEmpty()) {
            log.error("远程URL未配置，无法查询元数据");
            return Collections.emptyList();
        }
        
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(
                    remoteUrl + "/api/metadata/all", 
                    String.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return objectMapper.readValue(response.getBody(), 
                        new TypeReference<List<TaskMetadataEntity>>() {});
            } else {
                log.error("从远程服务器查询所有元数据失败: {}", response.getStatusCode());
                return Collections.emptyList();
            }
        } catch (Exception e) {
            log.error("从远程服务器查询所有注解元数据时发生错误: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}