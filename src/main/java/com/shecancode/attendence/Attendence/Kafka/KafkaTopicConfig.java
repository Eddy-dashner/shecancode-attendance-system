package com.shecancode.attendence.Attendence.Kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaTopicConfig {

    public static final String ATTENDANCE_TOPIC = "attendance-events";
    public static final String ALERT_TOPIC = "attendance-alerts";

    @Bean
    public NewTopic attendanceTopic(){
        return TopicBuilder.name(ATTENDANCE_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic alertTopic() {
        return TopicBuilder.name(ALERT_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    // Retry a failed listener (e.g. email provider down) 3 more times, 10s apart,
    // then log and skip the record. Picked up by Boot's default listener factory.
    @Bean
    public CommonErrorHandler kafkaErrorHandler() {
        return new DefaultErrorHandler(new FixedBackOff(10_000L, 3));
    }
}
