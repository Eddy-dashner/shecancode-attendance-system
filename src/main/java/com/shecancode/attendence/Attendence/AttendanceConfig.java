package com.shecancode.attendence.Attendence;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class AttendanceConfig {

    // "Today" for attendance (future-date checks, the trainer same-day edit rule)
    // is judged in the programme's local timezone, not the server's.
    @Bean
    public Clock attendanceClock(@Value("${app.attendance.timezone:Africa/Kigali}") String timezone) {
        return Clock.system(ZoneId.of(timezone));
    }
}
