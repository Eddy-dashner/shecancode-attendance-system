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

    public void sendAttendanceEvent(AttendanceEvent event) {
        log.info("preparing to publish attendance event for student ID", event.getStudentId());

        // Use the studentId as the message key to guarantee order retention per student across partitions
        String messageKey = String.valueOf(event.getStudentId());

        CompletableFuture<SendResult<String, AttendanceEvent>> future =
                kafkaTemplate.send(KafkaTopicConfig.ATTENDANCE_TOPIC, messageKey, event);

        // Handle the broker confirmation asynchronously
        future.whenComplete((result, exception) -> {
            if (exception == null) {
                log.info("Successfully produced event to topic [{}] | Partition: {} | Offset: {}",
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("Failed to deliver message to Kafka broker due to: {}", exception.getMessage(), exception);
            }
        });

    }
}
