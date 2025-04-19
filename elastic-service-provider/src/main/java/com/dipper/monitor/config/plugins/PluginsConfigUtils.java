package com.dipper.monitor.config.plugins;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class PluginsConfigUtils {

    private static final Logger log = LoggerFactory.getLogger(PluginsConfigUtils.class);
    private static final Map<String, Properties> cache = new ConcurrentHashMap<>();

    /**
     * 使用类加载器加载指定目录下的所有属性文件并缓存结果。
     */
    public static void loadAllProperties(String configDirPath) {
        String home = System.getProperty("user.dir");
        log.info("当前工作目录: {}", home);
        String pluginConfig = home + "/" + configDirPath;
        log.info("插件配置目录: {}", pluginConfig);
        File dir = new File(pluginConfig);
        if (dir.isDirectory()) {
            for (File file : dir.listFiles((d, name) -> name.endsWith(".properties"))) {
                try (InputStream input = new FileInputStream(file)) {
                    Properties properties = new Properties();
                    properties.load(input);

                    String name = file.getName().replace(".properties", "");
                    cache.put(name, properties);
                    log.info("已加载并缓存配置文件: {}", file.getName());
                } catch (IOException e) {
                    log.error("加载配置文件时出错: {}", file.getName(), e);
                }
            }
        } else {
            log.error("指定的配置文件目录不是有效的目录: {}", configDirPath);
        }
    }

    /**
     * 根据插件名称获取相应的属性配置。
     *
     * @param pluginName 插件名称（即配置文件名，不包括扩展名）。
     * @return 对应插件的Properties配置。
     */
    public static Properties getPluginConfig(String pluginName) {
        Properties properties = cache.get(pluginName);
        if (properties == null) {
            log.error("插件{}配置为空，返回默认值", pluginName);
            properties = cache.get(pluginName);
        }
        return properties;
    }

    // 示例方法：加载配置后打印某个插件的特定属性值
    public static String getProperty(String pluginName, String key) {
        Properties props = getPluginConfig(pluginName);
        return props == null ? null : props.getProperty(key);
    }
}