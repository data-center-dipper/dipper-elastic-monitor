package com.dipper.monitor.config;

import cn.hutool.core.map.MapUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.util.Map;

/***
 * todo: 九师兄  2023/8/4 11:05
 *
 * 【Spring】Spring的Controller为什么会被CGLB代理
 * https://blog.csdn.net/qq_21383435/article/details/132099921
 */
@Slf4j
@Component
public class TimeCostBeanPostProcessor implements BeanPostProcessor {

    private Map<String, Long> costMap = MapUtil.newHashMap();

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        log.info("准备初始化bean:{} class:{}",beanName,bean.getClass().getCanonicalName());
        costMap.put(beanName, System.currentTimeMillis());
        return bean;
    }
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (costMap.containsKey(beanName)) {
            Long start = costMap.get(beanName);
            long cost  = System.currentTimeMillis() - start;
            if (cost > 0) {
                costMap.put(beanName, cost);
//                log.debug("初始化bean:{} 耗时:{} class:{}",beanName,cost,bean.getClass().getCanonicalName());
            }
        }
        return bean;
    }
}
