package com.dipper.monitor.support;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class SpringApplicationContextHolder implements ApplicationListener<ContextRefreshedEvent> {

    private static Optional<ApplicationContext> contextHolder;
    private static List<ContextStartListener> onStartListeners = new ArrayList<>();

    public static void setContext(ApplicationContext context) {
        contextHolder = Optional.ofNullable(context);
    }

    public static ApplicationContext getContext() {
        if(!contextHolder.isPresent()) return null;

        return contextHolder.get();
    }

    public static Object getBean(String name) {
        if(!contextHolder.isPresent()) return null;

        return contextHolder.get().getBean(name);
    }

    public static <T> T getBean(String name, Class<T> clazz) {
        if(!contextHolder.isPresent()) return null;

        return contextHolder.get().getBean(name, clazz);
    }

    public static <T> T getBean(Class<T> clazz) {
        if(!contextHolder.isPresent()) return null;

        return contextHolder.get().getBean(clazz);
    }

    public static <T> T getBeanAllowNull(Class<T> clazz) {
        if(!contextHolder.isPresent()) return null;

        String[] beanNames = contextHolder.get().getBeanNamesForType(clazz);
        return null == beanNames || beanNames.length <= 0 ? null : contextHolder.get().getBean(clazz);
    }

    public static void addOnStartListener(ContextStartListener listener) {
        onStartListeners.add(listener);
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ApplicationContext ac = event.getApplicationContext();
        if (null == ac.getParent()) {
            contextHolder = Optional.ofNullable(event.getApplicationContext());

            for (ContextStartListener listener : onStartListeners) {
                listener.onStart(ac);
            }
        }
    }
}
