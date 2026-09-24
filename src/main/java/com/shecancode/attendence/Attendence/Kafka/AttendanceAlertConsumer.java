package com.shecancode.attendence.Attendence.Kafka;

import com.shecancode.attendence.Attendence.Event.AttendanceAlertEvent;
import com.shecancode.attendence.Attendence.Service.AlertNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Turns alert events into emails. Failures (e.g. the email provider is down) are
 * thrown so the Kafka error handler retries; the notification service skips
 * recipients who were already emailed, so retries are safe.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AttendanceAlertConsumer {

    private final AlertNotificationService notificationService;

    @KafkaListener(topics = KafkaTopicConfig.ALERT_TOPIC, groupId = "attendance-alert-notifier")
    public void onAlert(AttendanceAlertEvent event) {
        log.info("Received alert event {} for student {} (alerts {})",
                event.getEventId(), event.getStudentId(), event.getAlertIds());
        notificationService.notifyAlerts(event.getAlertIds());
    }
}
