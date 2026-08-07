package com.shecancode.attendence.Attendence.Kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final OutboxRepository outboxRepository;
    private final AttendanceProducer producer;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishPendingEvents() {

        List<OutboxEvent> eventList = outboxRepository.findByStatus(OutboxStatus.PENDING);
        if (eventList.isEmpty()) {
            return;
        }

        log.info("Found {} pending outbox event(s).", eventList.size());
        for (OutboxEvent outbox : eventList) {

            try {
                AttendanceEvent event = objectMapper.readValue(
                        outbox.getPayload(), AttendanceEvent.class);
                producer.sendAttendanceEvent(event).get();
                outbox.setStatus(OutboxStatus.SENT);
                outbox.setProcessedAt(Instant.now());

                outboxRepository.save(outbox);
            } catch (JsonProcessingException e) {
                log.error("Failed to deserialize event payload for outbox id: {}", outbox.getId(), e);
                outbox.setStatus(OutboxStatus.FAILED);
                outboxRepository.save(outbox);

            } catch (Exception e) {
                log.error("Failed to publish event for outbox id: {}", outbox.getId(), e);
                outbox.setStatus(OutboxStatus.FAILED);
                outboxRepository.save(outbox);
            }
        }
    }
}

