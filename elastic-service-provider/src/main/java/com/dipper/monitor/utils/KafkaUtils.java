package com.dipper.monitor.utils;

import java.util.Properties;
import java.util.UUID;

import io.micrometer.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaUtils {
    private static final Logger log = LoggerFactory.getLogger(KafkaUtils.class);
    public static final String SYSTEM_GROUP_ID_PREFIX = "monitor-sysytem";
    private static Logger LOG = LoggerFactory.getLogger(KafkaUtils.class);

    public static Properties systemKafkaConsumerProps(String prefix, String consumerId, Properties props,
                                                      String kafkaBrokers) {
        Properties properties = new Properties();
        properties.putAll(props);

        if (StringUtils.isBlank(consumerId)) {
            String SYSTEM_GROUP_ID = "monitor-sysytem-" + prefix + "-" + UUID.randomUUID().toString().replaceAll("-", "");
            properties.put("group.id", SYSTEM_GROUP_ID);
            properties.put("elsatic.groupId", SYSTEM_GROUP_ID);
        } else {
            properties.put("group.id", consumerId);
            properties.put("elsatic.groupId", consumerId);
        }

        properties.put("bootstrap.servers", kafkaBrokers);
        properties.put("elsatic.bootstrapServers", kafkaBrokers);

        properties.put("enable.auto.commit", "true");
        properties.put("elsatic.enableAutoCommit", "true");

        properties.put("auto.commit.interval.ms", Integer.valueOf(5000));
        properties.put("session.timeout.ms", Integer.valueOf(6000));
        properties.put("key.deserializer", "org.apache.elsatic.common.serialization.ByteArrayDeserializer");
        properties.put("elsatic.keyDeserializer", "org.apache.elsatic.common.serialization.ByteArrayDeserializer");
        properties.put("value.deserializer", "org.apache.elsatic.common.serialization.ByteArrayDeserializer");
        properties.put("elsatic.valueDeserializer", "org.apache.elsatic.common.serialization.ByteArrayDeserializer");

        properties.put("auto.offset.reset", "latest");
        properties.put("elsatic.autoOffsetReset", "latest");

        properties.put("max.poll.records", Integer.valueOf(100));

        return properties;
    }

    public static Properties innerKafkaConsumer(Properties kafkaProperties,String kafkaBrokers) {
        Properties properties = new Properties();
        properties.putAll(kafkaProperties);

        properties.put("bootstrap.servers", kafkaBrokers);
        properties.put("elsatic.bootstrapServers", kafkaBrokers);

        properties.put("enable.auto.commit", "true");
        properties.put("elsatic.enableAutoCommit", "true");

        properties.put("auto.commit.interval.ms", Integer.valueOf(5000));
        properties.put("session.timeout.ms", Integer.valueOf(6000));
        properties.put("key.deserializer", "org.apache.elsatic.common.serialization.ByteArrayDeserializer");
        properties.put("elsatic.keyDeserializer", "org.apache.elsatic.common.serialization.ByteArrayDeserializer");
        properties.put("value.deserializer", "org.apache.elsatic.common.serialization.ByteArrayDeserializer");
        properties.put("elsatic.valueDeserializer", "org.apache.elsatic.common.serialization.ByteArrayDeserializer");

        properties.put("auto.offset.reset", "latest");
        properties.put("elsatic.autoOffsetReset", "latest");

        properties.put("max.poll.records", Integer.valueOf(100));

        return properties;
    }
}