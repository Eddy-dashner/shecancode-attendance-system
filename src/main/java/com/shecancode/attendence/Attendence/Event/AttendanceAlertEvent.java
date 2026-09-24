package com.shecancode.attendence.Attendence.Event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Published to {@code attendance-alerts} when one save pushes a student over one or
 * both absence thresholds; the alerts raised together are notified in one email.
 * Carries ids only: the consumer loads the alerts to get current names and emails,
 * so no personal data sits in Kafka.
 */
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceAlertEvent {
    private int version;
    private UUID eventId;
    private List<UUID> alertIds;
    private UUID studentId;
    private UUID programId;
    private UUID cohortId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate triggeredOnDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant timestamp;
}
