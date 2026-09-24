package com.shecancode.attendence.Attendence.Event;

import com.shecancode.attendence.Attendence.Model.Attendance;
import com.shecancode.attendence.Attendence.Model.AttendanceAlert;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class OutboxEventFactory {

    private final EventSerializer eventSerializer;

    public OutboxEventFactory(EventSerializer eventSerializer) {
        this.eventSerializer = eventSerializer;
    }

    public OutboxEvent createAttendanceOutboxEvent(Attendance attendance) {
        return buildAttendanceEvent(attendance, EventType.ATTENDANCE_RECORDED);
    }

    public OutboxEvent createAttendanceUpdatedEvent(Attendance attendance) {
        return buildAttendanceEvent(attendance, EventType.ATTENDANCE_UPDATED);
    }

    /** One event for all alerts raised for one student in one save. */
    public OutboxEvent createAlertTriggeredEvent(List<AttendanceAlert> alerts) {
        AttendanceAlert first = alerts.get(0);
        AttendanceAlertEvent event = AttendanceAlertEvent.builder()
                .version(1)
                .eventId(UUID.randomUUID())
                .alertIds(alerts.stream().map(AttendanceAlert::getId).toList())
                .studentId(first.getStudent().getId())
                .programId(first.getProgram().getId())
                .cohortId(first.getCohort().getId())
                .triggeredOnDate(first.getTriggeredOnDate())
                .timestamp(Instant.now())
                .build();

        return buildOutboxEvent(first.getId(), EventType.ATTENDANCE_ALERT_TRIGGERED, event);
    }

    private OutboxEvent buildAttendanceEvent(Attendance attendance, EventType eventType) {
        AttendanceEvent event = AttendanceEvent.builder()
                .version(1)
                .eventId(UUID.randomUUID())
                .attendanceId(attendance.getAttendanceId())
                .studentId(attendance.getStudent().getId())
                .programId(attendance.getProgram().getId())
                .cohortId(attendance.getCohort().getId())
                .attendanceStatus(attendance.getAttendanceStatus())
                .attendanceDate(attendance.getAttendanceRecordedDate())
                .checkInTime(attendance.getCheckInTime())
                .timestamp(Instant.now())
                .build();

        return buildOutboxEvent(attendance.getAttendanceId(), eventType, event);
    }

    private OutboxEvent buildOutboxEvent(UUID aggregateId, EventType eventType, Object event) {
        return OutboxEvent.builder()
                .id(UUID.randomUUID())
                .aggregateId(aggregateId)
                .eventType(eventType)
                .payload(eventSerializer.serialize(event))
                .status(OutboxStatus.PENDING)
                // createdAt is set automatically by @CreationTimestamp
                // processedAt remains null until successfully published
                .build();
    }
}
