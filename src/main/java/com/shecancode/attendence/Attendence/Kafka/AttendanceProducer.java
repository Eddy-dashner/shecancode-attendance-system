package com.shecancode.attendence.Attendence.Kafka;

import com.shecancode.attendence.Attendence.Event.AttendanceAlertEvent;
import com.shecancode.attendence.Attendence.Event.AttendanceEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class AttendanceProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public CompletableFuture<SendResult<String, Object>> sendAttendanceEvent(AttendanceEvent event) {
        return send(KafkaTopicConfig.ATTENDANCE_TOPIC, event.getStudentId().toString(), event);
    }

    public CompletableFuture<SendResult<String, Object>> sendAlertEvent(AttendanceAlertEvent event) {
        return send(KafkaTopicConfig.ALERT_TOPIC, event.getStudentId().toString(), event);
    }

    // Keyed by student id so all events for one student land on the same partition, in order.
    private CompletableFuture<SendResult<String, Object>> send(String topic, String key, Object event) {
        return kafkaTemplate.send(topic, key, event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Successfully produced event to topic [{}], Partition: {}, Offset: {}",
                                result.getRecordMetadata().topic(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    } else {
                        log.error("Failed to deliver message to topic [{}]", topic, ex);
                    }
                });
    }
}
