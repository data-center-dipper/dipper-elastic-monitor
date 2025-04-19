package com.dipper.monitor.config.listeners;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/***
 * todo: 九师兄  2024/3/29 09:30
 * 经过测试，可以发现这个是Spring boot 所有的bean都初始化后，然后再启动好tomcat之后，
 * 才会发布的事件
 *
 * 2024-03-29 09:30:06,087 [restartedMain] INFO  o.s.b.d.a.OptionalLiveReloadServer.startServer(OptionalLiveReloadServer.java:58) - LiveReload server is running on port 35729
 * 所有bean初始化完毕！
 * 2024-03-29 09:30:06,104 [restartedMain] INFO  o.a.coyote.http11.Http11NioProtocol.log(DirectJDKLog.java:173) - Starting ProtocolHandler ["http-nio-8082"]
 * 2024-03-29 09:30:06,132 [restartedMain] INFO  o.s.b.w.e.tomcat.TomcatWebServer.start(TomcatWebServer.java:220) - Tomcat started on port(s): 8082 (http) with context path ''
 * 接收到ContextRefreshedEvent事件。。。
 *
 * https://mp.weixin.qq.com/s/mr4MAiGOSOvBUScHhLQgzA
 */
@Slf4j
@Component
public class ScheduledAnnotationBeanPostProcessor implements ApplicationListener<ContextRefreshedEvent> {


    public ScheduledAnnotationBeanPostProcessor() {
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

        log.info("接收到ContextRefreshedEvent事件。。。");
    }
}
