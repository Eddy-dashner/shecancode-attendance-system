package com.shecancode.attendence.Attendence.Kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shecancode.attendence.Attendence.Event.AttendanceAlertEvent;
import com.shecancode.attendence.Attendence.Event.AttendanceEvent;
import com.shecancode.attendence.Attendence.Event.OutboxEvent;
import com.shecancode.attendence.Attendence.Event.OutboxRepository;
import com.shecancode.attendence.Attendence.Event.OutboxStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class AttendanceEventPublisher {

    private static final int MAX_RETRIES = 5;

    private final OutboxRepository outboxRepository;
    private final AttendanceProducer producer;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishPendingEvents() {

        List<OutboxEvent> eventList = outboxRepository.findByStatusInAndRetryCountLessThan(
                List.of(OutboxStatus.PENDING, OutboxStatus.FAILED), MAX_RETRIES);
        if (eventList.isEmpty()) {
            return;
        }

        log.info("Found {} outbox event(s) to (re)publish.", eventList.size());
        for (OutboxEvent outbox : eventList) {

            try {
                publish(outbox);
                outbox.setStatus(OutboxStatus.SENT);
                outbox.setProcessedAt(Instant.now());

                outboxRepository.save(outbox);
            } catch (JsonProcessingException e) {
                log.error("Failed to deserialize event payload for outbox id: {}", outbox.getId(), e);
                failOutboxEvent(outbox);

            } catch (Exception e) {
                log.error("Failed to publish event for outbox id: {}", outbox.getId(), e);
                failOutboxEvent(outbox);
            }
        }
    }

    private void publish(OutboxEvent outbox) throws Exception {
        switch (outbox.getEventType()) {
            case ATTENDANCE_RECORDED, ATTENDANCE_UPDATED -> producer.sendAttendanceEvent(
                    objectMapper.readValue(outbox.getPayload(), AttendanceEvent.class)).get();
            case ATTENDANCE_ALERT_TRIGGERED -> producer.sendAlertEvent(
                    objectMapper.readValue(outbox.getPayload(), AttendanceAlertEvent.class)).get();
        }
    }

    private void failOutboxEvent(OutboxEvent outbox) {
        outbox.setStatus(OutboxStatus.FAILED);
        outbox.setRetryCount(outbox.getRetryCount() + 1);
        if (outbox.getRetryCount() >= MAX_RETRIES) {
            log.error("Outbox event {} exhausted {} retries; giving up.", outbox.getId(), MAX_RETRIES);
        }
        outboxRepository.save(outbox);
    }
}

