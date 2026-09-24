package com.shecancode.attendence.Attendence.Service;

import com.shecancode.attendence.Attendence.Enum.ProgressColor;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.shecancode.attendence.Attendence.Enum.AttendanceStatus.*;
import static org.junit.jupiter.api.Assertions.*;

class AttendanceScoreTest {

    @Test
    void givenNoRecords_whenScored_thenFullMarksAndGreen() {
        AttendanceScore score = AttendanceScore.of(List.of());

        assertEquals(100.0, score.percentage());
        assertEquals(ProgressColor.GREEN, score.color());
        assertEquals(0, score.consecutiveAbsences());
    }

    @Test
    void givenAbsences_whenScored_thenAbsentCostsOneAndCommunicatedCostsHalf() {
        AttendanceScore score = AttendanceScore.of(List.of(PRESENT, ABSENT, ABSENT_COMMUNICATED, PRESENT));

        assertEquals(1.5, score.pointsLost());
        assertEquals(2.5, score.pointsKept());
        assertEquals(62.5, score.percentage());
        assertEquals(2, score.totalAbsences());
    }

    @Test
    void givenTwoLates_whenScored_thenNoPointsLost() {
        AttendanceScore score = AttendanceScore.of(List.of(LATE_PRESENT, LATE_PRESENT, PRESENT));

        assertEquals(0.0, score.pointsLost());
    }

    @Test
    void givenThreeLates_whenScored_thenOneAbsenceLost() {
        AttendanceScore score = AttendanceScore.of(List.of(LATE_PRESENT, LATE_PRESENT, LATE_PRESENT, PRESENT));

        assertEquals(1.0, score.pointsLost());
        assertEquals(75.0, score.percentage());
    }

    @Test
    void givenSevenLates_whenScored_thenTwoAbsencesLost() {
        AttendanceScore score = AttendanceScore.of(List.of(
                LATE_PRESENT, LATE_PRESENT, LATE_PRESENT, LATE_PRESENT, LATE_PRESENT, LATE_PRESENT, LATE_PRESENT));

        assertEquals(2.0, score.pointsLost());
    }

    @Test
    void givenLatesOnly_whenScored_thenLatesDoNotCountAsAbsencesForAlerts() {
        AttendanceScore score = AttendanceScore.of(List.of(LATE_PRESENT, LATE_PRESENT, LATE_PRESENT));

        assertEquals(0, score.totalAbsences());
        assertEquals(0, score.consecutiveAbsences());
    }

    @Test
    void givenRecentAbsencesOfBothKinds_whenScored_thenStreakCountsBoth() {
        // newest first
        AttendanceScore score = AttendanceScore.of(List.of(ABSENT, ABSENT_COMMUNICATED, ABSENT, PRESENT, ABSENT));

        assertEquals(3, score.consecutiveAbsences());
        assertEquals(4, score.totalAbsences());
    }

    @Test
    void givenMostRecentSessionAttended_whenScored_thenStreakIsZero() {
        AttendanceScore score = AttendanceScore.of(List.of(LATE_PRESENT, ABSENT, ABSENT, ABSENT));

        assertEquals(0, score.consecutiveAbsences());
    }

    @Test
    void givenPercentages_whenColored_thenThresholdsApply() {
        // 17/20 = 85% -> GREEN
        assertEquals(ProgressColor.GREEN, scoreWithAbsences(20, 3).color());
        // 16/20 = 80% -> YELLOW
        assertEquals(ProgressColor.YELLOW, scoreWithAbsences(20, 4).color());
        // 12/20 = 60% -> RED (yellow needs more than 60%)
        assertEquals(ProgressColor.RED, scoreWithAbsences(20, 8).color());
    }

    private static AttendanceScore scoreWithAbsences(int sessions, int absences) {
        return new AttendanceScore(sessions, absences, 0, 0, 0);
    }
}
