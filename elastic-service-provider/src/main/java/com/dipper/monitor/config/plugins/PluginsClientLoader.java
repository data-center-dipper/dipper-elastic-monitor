package com.dipper.monitor.config.plugins;

import com.dipper.client.proxy.ClientServiceLoader;
import com.dipper.client.proxy.api.CommonClientService;
import com.dipper.client.proxy.config.BaseProxyConfig;
import com.dipper.client.proxy.config.PluginConfig;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class PluginsClientLoader {

    private static final Logger logger = LoggerFactory.getLogger(PluginsClientLoader.class);

    private static ClientServiceLoader clientServiceLoader;
    private static final Lock LOCK = new ReentrantLock();

    private PluginsClientLoader() {
        logger.info("准备初始化PluginsClientLoader");
    }

    public static void init(String pluginsDir) {
        if(clientServiceLoader == null){
            synchronized (PluginsClientLoader.class){
                if(clientServiceLoader == null){
                    logger.info("准备初始化插件目录:{}", pluginsDir);
                    if(StringUtils.isBlank(pluginsDir)){
                        throw new RuntimeException("插件目录为空");
                    }
                    initFromFileDir(new File(pluginsDir));
                }
            }
        }
    }

    public static void init(File pluginsDir) {
        if (clientServiceLoader == null) {
            synchronized (PluginsClientLoader.class) {
                if (clientServiceLoader == null) {
                    initFromFileDir(pluginsDir);
                }
            }
        }
    }

    private static void initFromFileDir(File pluginsDir) {
        List<Path> pluginPaths = new ArrayList<>();
        if (pluginsDir.exists() && pluginsDir.isDirectory()) {
            File[] files = pluginsDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".jar")) {
                        pluginPaths.add(file.toPath());
                    }
                }
            }
        }
        if(pluginPaths == null || pluginPaths.isEmpty()){
            logger.info("no plugin found in {}", pluginsDir.getAbsolutePath());
            throw new RuntimeException("没有发现插件信息");
        }
        logger.info("plugin paths: {}", pluginPaths.stream().map(Path::toString).collect(Collectors.joining(",")));
        clientServiceLoader = ClientServiceLoader.getInstance(pluginPaths);
    }


    public static synchronized <T extends CommonClientService> T loadComponentClient(PluginConfig pluginConfig, Class<T> serviceClass, BaseProxyConfig config) {
        Preconditions.checkNotNull(serviceClass, "Service class cannot be null");
        Preconditions.checkNotNull(config, "Config cannot be null");

        logger.info("准备创建服务:{}",serviceClass);
        T t = clientServiceLoader.loadServiceOfPlugin(pluginConfig,config, serviceClass);

        return t;
    }


    public static PluginsClientLoader getInstance() {
        return new PluginsClientLoader();
    }
}