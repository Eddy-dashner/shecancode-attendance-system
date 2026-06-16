package com.shecancode.attendence.Attendence.Kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;

public class KafkaTopicConfig {
    public static final String ATTENDANCE_TOPIC = "attendance-events";

    @Bean
    public NewTopic attendanceTopic(){
        return TopicBuilder.name(ATTENDANCE_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
