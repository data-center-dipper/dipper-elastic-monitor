package com.dipper.monitor.utils.plugins;

import com.dipper.client.proxy.config.PluginConfig;

import java.util.Properties;

public class PluginConfigUtils {


    public static PluginConfig getElasticConfig(Properties properties) {
        String clientType = properties.getProperty("elastic.clientType");
        String clientVersion = properties.getProperty("elastic.clientVersion");
        String pluginVersion = properties.getProperty("elastic.pluginVersion");

        PluginConfig pluginConfig = new PluginConfig();
        pluginConfig.setPluginVersion(pluginVersion);
        pluginConfig.setClientType(clientType);
        pluginConfig.setClientVersion(clientVersion);

        return pluginConfig;
    }
}
