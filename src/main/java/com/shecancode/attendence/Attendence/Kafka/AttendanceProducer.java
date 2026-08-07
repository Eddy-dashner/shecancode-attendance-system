package com.shecancode.attendence.Attendence.Kafka;

import com.shecancode.attendence.Attendence.Event.AttendanceEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.kafka.support.SendResult;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class AttendanceProducer {

    private final KafkaTemplate<String, AttendanceEvent> kafkaTemplate;

    public CompletableFuture<SendResult<String, AttendanceEvent>>
    sendAttendanceEvent(AttendanceEvent event) {

        String messageKey = event.getStudentId().toString();

        return kafkaTemplate.send(
                KafkaTopicConfig.ATTENDANCE_TOPIC,
                messageKey,
                event
        ).whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Successfully produced event to topic [{}], Partition: {}, Offset: {}",
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("Failed to deliver message to topic [{}]", KafkaTopicConfig.ATTENDANCE_TOPIC, ex);
            }
        });
    }
}
