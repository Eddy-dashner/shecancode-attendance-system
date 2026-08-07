package com.shecancode.attendence.Attendence.Event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shecancode.attendence.Attendence.Model.Attendance;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;


@Component
public class OutboxEventFactory {

    private final ObjectMapper objectMapper;

    public OutboxEventFactory(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Creates an OutboxEvent for a newly recorded attendance.
     */
    public OutboxEvent createAttendanceOutboxEvent(Attendance attendance) {
        return buildOutboxEvent(attendance, EventType.ATTENDANCE_RECORDED);
    }

    /**
     * Creates an OutboxEvent when attendance is updated.
     */
    public OutboxEvent createAttendanceUpdatedEvent(Attendance attendance) {
        return buildOutboxEvent(attendance, EventType.ATTENDANCE_UPDATED);
    }

    /**
     * Builds the OutboxEvent.
     */
    private OutboxEvent buildOutboxEvent(
            Attendance attendance,
            EventType eventType) {

        AttendanceEvent event = AttendanceEvent.builder()
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

        return OutboxEvent.builder()
                .id(UUID.randomUUID())
                //.aggregateType(AggregateType.ATTENDANCE)
                .aggregateId(attendance.getAttendanceId())
                .eventType(eventType)
                .payload(convertToJson(event))
                .status(OutboxStatus.PENDING)
                // createdAt is set automatically by @CreationTimestamp
                // processedAt remains null until successfully published
                .build();
    }

    /**
     * Converts AttendanceEvent to JSON.
     */
    private String convertToJson(AttendanceEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert AttendanceEvent to JSON.", e);
        }
    }
}
