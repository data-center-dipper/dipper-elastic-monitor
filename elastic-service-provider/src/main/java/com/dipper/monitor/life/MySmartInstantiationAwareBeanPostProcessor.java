package com.dipper.monitor.life;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.SmartInstantiationAwareBeanPostProcessor;
import org.springframework.lang.Nullable;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class MySmartInstantiationAwareBeanPostProcessor implements SmartInstantiationAwareBeanPostProcessor {

    @Nullable
    @Override
    public Constructor<?>[] determineCandidateConstructors(Class<?> beanClass, String beanName) throws BeansException {
       log.info(beanClass.getName());
        log.info("调用 MySmartInstantiationAwareBeanPostProcessor.determineCandidateConstructors 方法");
        Constructor<?>[] declaredConstructors = beanClass.getDeclaredConstructors();
        if (declaredConstructors != null) {
            //获取有@MyAutowried注解的构造器列表
            List<Constructor<?>> collect = Arrays.stream(declaredConstructors).
                    filter(constructor -> constructor.isAnnotationPresent(MyAutowried.class)).
                    collect(Collectors.toList());
            Constructor[] constructors = collect.toArray(new Constructor[collect.size()]);
            return constructors.length != 0 ? constructors : null;
        } else {
            return null;
        }
    }
}
