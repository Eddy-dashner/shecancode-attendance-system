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

    public OutboxEvent createAttendanceOutboxEvent(Attendance attendance){

        AttendanceEvent event = AttendanceEvent.builder()
                .eventId(UUID.randomUUID())
                .attendanceId(attendance.getAttendanceId())
                .studentId(attendance.getStudent().getId())
                .programId(attendance.getProgram().getId())
                .cohortId(attendance.getCohort().getId())
                .attendanceStatus(attendance.getAttendanceStatus())
                .attendanceDate(attendance.getAttendanceRecordedDate())
                .checkInTime(attendance.getCheckInTime())
                .build();

        return OutboxEvent.builder()
                .id(UUID.randomUUID())
                .aggregateType(AggregateType.ATTENDANCE)
                .aggregateId(attendance.getAttendanceId())
                .eventType(EventType.ATTENDANCE_RECORDED)
                .payload(convertToJson(event))
                .status(OutboxStatus.PENDING)
                .createdAt(Instant.now())
                .build();
    }

    private String convertToJson(AttendanceEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert event to JSON", e);
        }
    }
}
