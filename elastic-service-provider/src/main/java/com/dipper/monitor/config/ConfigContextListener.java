package com.dipper.monitor.config;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

public class ConfigContextListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("config_context 初始化");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("java_context 销毁");
    }
}