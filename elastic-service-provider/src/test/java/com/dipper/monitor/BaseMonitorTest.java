package com.dipper.monitor;


import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;


@SpringBootTest(classes = ElasticApplication.class)
@RunWith(SpringRunner.class)
@ActiveProfiles("windows")
public abstract class BaseMonitorTest {
    // 可以在这里定义一些公共的方法或字段供子类使用
}