package com.dipper.monitor.config.listeners;

import com.dipper.monitor.config.plugins.PluginsConfigUtils;
import com.dipper.monitor.utils.plugins.PluginUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

@Slf4j
public class StartApplicationContextInitializer  implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        log.info("初始化插件配置");
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        String configDirPath = environment.getProperty("plugins.config.dir");
        if (configDirPath == null || configDirPath.isEmpty()) {
            log.error("插件配置目录未指定！");
            return;
        }
        log.info("插件配置目录: {}", configDirPath);

        // 加载目录下所有的配置文件
        PluginsConfigUtils.loadAllProperties(configDirPath);

        String jarDirPath = environment.getProperty("plugins.jar.dir");
        try {
            PluginUtils.loadPlugins(jarDirPath);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}

