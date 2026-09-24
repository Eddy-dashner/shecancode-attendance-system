package com.shecancode.attendence.Attendence.Service;

import com.shecancode.attendence.Attendence.Enum.AttendanceStatus;
import com.shecancode.attendence.Attendence.Enum.ProgressColor;

import java.util.List;

/**
 * The attendance scoring rules, in one place:
 * <ul>
 *   <li>ABSENT costs 1 point, ABSENT_COMMUNICATED costs 0.5 points.</li>
 *   <li>Every 3 LATE_PRESENT cost 1 point (2 lates cost nothing).</li>
 *   <li>Percentage = points kept / sessions recorded for the student.</li>
 *   <li>GREEN at 85% or more, YELLOW above 60%, RED otherwise.</li>
 *   <li>Both absence types count toward the absence totals and streak.</li>
 * </ul>
 */
public record AttendanceScore(
        int sessionsRecorded,
        int absent,
        int absentCommunicated,
        int late,
        int consecutiveAbsences
) {

    public static final int LATES_PER_ABSENCE = 3;

    /**
     * @param statusesNewestFirst the student's statuses ordered by session date, newest first
     */
    public static AttendanceScore of(List<AttendanceStatus> statusesNewestFirst) {
        int absent = 0, communicated = 0, late = 0, streak = 0;
        boolean streakOpen = true;

        for (AttendanceStatus status : statusesNewestFirst) {
            switch (status) {
                case ABSENT -> absent++;
                case ABSENT_COMMUNICATED -> communicated++;
                case LATE_PRESENT -> late++;
                case PRESENT -> { }
            }
            if (streakOpen && status.isAbsence()) {
                streak++;
            } else {
                streakOpen = false;
            }
        }
        return new AttendanceScore(statusesNewestFirst.size(), absent, communicated, late, streak);
    }

    /** On-time attendance (not late, not absent). */
    public int present() {
        return sessionsRecorded - absent - absentCommunicated - late;
    }

    public int totalAbsences() {
        return absent + absentCommunicated;
    }

    public double pointsLost() {
        return absent + absentCommunicated * 0.5 + (late / LATES_PER_ABSENCE);
    }

    public double pointsKept() {
        return sessionsRecorded - pointsLost();
    }

    public double percentage() {
        if (sessionsRecorded == 0) return 100.0;
        return pointsKept() * 100.0 / sessionsRecorded;
    }

    public ProgressColor color() {
        double percentage = percentage();
        if (percentage >= 85) return ProgressColor.GREEN;
        if (percentage > 60) return ProgressColor.YELLOW;
        return ProgressColor.RED;
    }
}
