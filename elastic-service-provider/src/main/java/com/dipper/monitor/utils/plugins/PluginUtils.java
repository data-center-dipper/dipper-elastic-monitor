package com.dipper.monitor.utils.plugins;

import cn.hutool.core.io.FileUtil;
import com.dipper.common.lib.utils.ApplicationUtils;
import com.dipper.monitor.config.plugins.PluginsClientLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class PluginUtils {

    private static final Logger logger = LoggerFactory.getLogger(PluginUtils.class);


    public static void loadPlugins(String jarDirPath) throws IllegalAccessException {
        logger.info("加载插件相对路径目录: {}", jarDirPath);
        String home = System.getProperty("user.dir");
        logger.info("当前目录:{}", home);
        if(ApplicationUtils.isWindows()){
            if(home.endsWith("elastic-handler-provider")){
                home = home.substring(0, home.lastIndexOf("elastic-handler-provider"));
            }
        }


        String pluginsDirPath = home + "/"+jarDirPath;
        logger.info("插件目录: {}", pluginsDirPath);

        File pluginsDir = new File(pluginsDirPath);
        if(!FileUtil.exist(pluginsDirPath)){
            logger.error("插件目录不存在..");
            throw new IllegalAccessException("插件加载异常");
        }
        if(FileUtil.listFileNames(pluginsDirPath).size() < 1){
            logger.error("插件目录不存在插件包");
            throw new IllegalAccessException("插件加载异常");
        }

        PluginsClientLoader.init(pluginsDir);

        logger.info("plugins load finished.");
    }

    public static void loadExtPluginsConfig() {
        if (ApplicationUtils.isLinux()) {
            logger.info("plugins ext config load starting...");
            String home = System.getenv("EXT_HOME");
            String pluginsExtDirPath = home + "/conf/plugins-ext";
            String pluginsDirPath = home + "/conf/plugins";
            logger.info("ext conf dir path: {}", pluginsExtDirPath);
            File pluginsConfigExtDir = new File(pluginsExtDirPath);
            if (pluginsConfigExtDir.exists() && pluginsConfigExtDir.isDirectory()) {
                File[] files = pluginsConfigExtDir.listFiles();
                if (files != null) {
                    logger.info("plugins ext config dir exists, copy files to conf/plugins/, total: {}", Integer.valueOf(files.length));
                    for (File file : files) {
                        logger.info("copy file: {} to conf/plugins/ with replace", file.getName());
                        FileUtil.copy(file, new File(pluginsDirPath), true);
                    }
                }
            } else {
                logger.info("ext conf dir path not exist.");
            }
            logger.info("plugins ext config load finished.");
        }
    }
}
