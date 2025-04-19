package com.dipper.monitor.utils;

import com.dipper.monitor.entity.db.config.ConfItemEntity;
import org.springframework.core.io.Resource;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStream;
import java.util.List;

public class YamlConfigReader {

    /**
     * 读取YML文件并返回ConfItemEntity对象列表
     *
     * @param filePath 文件路径
     * @return ConfItemEntity对象列表
     */
    public static List<ConfItemEntity> readYamlFile(Resource resource) {
        try (InputStream inputStream = resource.getInputStream()) {
            if (inputStream == null) {
                throw new IllegalArgumentException("File not found: ");
            }

            // 使用SnakeYAML库解析YAML文件
            Yaml yaml = new Yaml(new Constructor(ConfItemWrapper.class));
            ConfItemWrapper wrapper = yaml.load(inputStream);

            return wrapper.getInnerConfig();
        } catch (Exception e) {
            throw new RuntimeException("Failed to read and parse YAML file: ", e);
        }
    }

    // 定义一个包装类，用于解析YAML文件中的inner_config列表
    private static class ConfItemWrapper {
        public List<ConfItemEntity> inner_config;

        public List<ConfItemEntity> getInnerConfig() {
            return this.inner_config;
        }

        public void setInnerConfig(List<ConfItemEntity> inner_config) {
            this.inner_config = inner_config;
        }
    }


}