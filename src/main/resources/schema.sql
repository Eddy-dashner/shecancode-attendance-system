-- ==========================================
-- 1. DROP TABLES (children first)
-- ==========================================
DROP TABLE IF EXISTS activation_token CASCADE;
DROP TABLE IF EXISTS attendance_alert CASCADE;
DROP TABLE IF EXISTS attendance CASCADE;
DROP TABLE IF EXISTS attendance_session CASCADE;
DROP TABLE IF EXISTS participant CASCADE;
DROP TABLE IF EXISTS student_profile CASCADE;
DROP TABLE IF EXISTS student CASCADE;
DROP TABLE IF EXISTS cohort CASCADE;
DROP TABLE IF EXISTS program CASCADE;
DROP TABLE IF EXISTS app_user CASCADE;

-- ==========================================
-- 2. PROGRAM (Parent Table)
-- ==========================================
CREATE TABLE program (
                         program_id UUID PRIMARY KEY,
                         program_name VARCHAR(255) NOT NULL,
                         program_duration INTEGER,
                         program_start_date DATE NOT NULL,
                         program_end_date DATE NOT NULL,
                         status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
                         deleted_at TIMESTAMP WITH TIME ZONE
);

-- ==========================================
-- 3. COHORT (belongs to a Program)
-- ==========================================
CREATE TABLE cohort (
                        cohort_id UUID PRIMARY KEY,
                        cohort_number VARCHAR(255) NOT NULL,
                        start_date DATE NOT NULL,
                        end_date DATE NOT NULL,
                        program_id UUID NOT NULL,
                        status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
                        deleted_at TIMESTAMP WITH TIME ZONE,

                        CONSTRAINT fk_cohort_program
                            FOREIGN KEY (program_id)
                                REFERENCES program (program_id),
                        CONSTRAINT uk_cohort_program_number UNIQUE (program_id, cohort_number)
);

-- ==========================================
-- 4. STUDENT
-- ==========================================
-- Names are nullable: an invited student has no profile yet (filled at activation).
CREATE TABLE student (
                         student_id UUID PRIMARY KEY,
                         student_first_name VARCHAR(255),
                         student_last_name VARCHAR(255),
                         email VARCHAR(255) NOT NULL UNIQUE,
                         phone_number VARCHAR(255),
                         home_address VARCHAR(255),
                         current_occupation VARCHAR(255),
                         student_status VARCHAR(50),
                         cohort_id UUID NOT NULL,
                         program_id UUID NOT NULL,
                         user_id UUID,
                         deleted_at TIMESTAMP WITH TIME ZONE,

                         CONSTRAINT fk_student_cohort
                             FOREIGN KEY (cohort_id)
                                 REFERENCES cohort (cohort_id),

                         CONSTRAINT fk_student_program
                             FOREIGN KEY (program_id)
                                 REFERENCES program (program_id)
    -- fk_student_user is added after app_user is created (see below)
);

-- ==========================================
-- 4b. STUDENT PROFILE (1:1 with student; filled by the student after activation)
-- ==========================================
CREATE TABLE student_profile (
                         profile_id UUID PRIMARY KEY,
                         student_id UUID NOT NULL UNIQUE,

                         date_of_birth DATE NOT NULL,
                         gender VARCHAR(20) NOT NULL,
                         nationality VARCHAR(255) NOT NULL,
                         refugee BOOLEAN NOT NULL DEFAULT FALSE,

                         has_disability BOOLEAN NOT NULL DEFAULT FALSE,
                         disability_type VARCHAR(50),
                         disability_details VARCHAR(2000),

                         province VARCHAR(255) NOT NULL,
                         district VARCHAR(255) NOT NULL,
                         sector VARCHAR(255) NOT NULL,
                         cell VARCHAR(255) NOT NULL,
                         village VARCHAR(255) NOT NULL,

                         emergency_contact_name VARCHAR(255) NOT NULL,
                         emergency_contact_relationship VARCHAR(255) NOT NULL,
                         emergency_contact_phone VARCHAR(255) NOT NULL,

                         has_young_child BOOLEAN NOT NULL DEFAULT FALSE,
                         has_childcare_support BOOLEAN NOT NULL DEFAULT FALSE,
                         has_laptop BOOLEAN NOT NULL DEFAULT FALSE,

                         occupation VARCHAR(60) NOT NULL,
                         education_level VARCHAR(30) NOT NULL,
                         institution VARCHAR(255),
                         academic_background VARCHAR(2000) NOT NULL,
                         english_proficiency VARCHAR(20) NOT NULL,
                         strongest_english_skill VARCHAR(20) NOT NULL,

                         linkedin_url VARCHAR(500),
                         github_url VARCHAR(500),
                         referral_source VARCHAR(40) NOT NULL,
                         referral_source_details VARCHAR(255),
                         motivation VARCHAR(4000) NOT NULL,
                         additional_feedback VARCHAR(4000),

                         completed_at TIMESTAMP WITH TIME ZONE NOT NULL,
                         updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

                         CONSTRAINT fk_student_profile_student
                             FOREIGN KEY (student_id)
                                 REFERENCES student (student_id)
);

-- ==========================================
-- 5. ATTENDANCE SESSION (one register per cohort per date)
-- ==========================================
CREATE TABLE attendance_session (
                            session_id UUID PRIMARY KEY,
                            cohort_id UUID NOT NULL,
                            program_id UUID NOT NULL,
                            session_date DATE NOT NULL,
                            taken_by_id UUID NOT NULL,
                            taken_by_name VARCHAR(255) NOT NULL,
                            created_at TIMESTAMP NOT NULL,
                            updated_at TIMESTAMP,

                            CONSTRAINT fk_session_cohort
                                FOREIGN KEY (cohort_id)
                                    REFERENCES cohort (cohort_id),

                            CONSTRAINT fk_session_program
                                FOREIGN KEY (program_id)
                                    REFERENCES program (program_id),

                            CONSTRAINT uk_session_cohort_date UNIQUE (cohort_id, session_date)
);

-- ==========================================
-- 5b. ATTENDANCE (one row per student per session)
-- ==========================================
CREATE TABLE attendance (
                            attendance_id UUID PRIMARY KEY,
                            session_id UUID NOT NULL,
                            student_id UUID NOT NULL,
                            program_id UUID NOT NULL,
                            cohort_id UUID NOT NULL,

                            -- NULL for absences
                            check_in_time TIME,
                            attendance_status VARCHAR(255) NOT NULL,
                            remarks VARCHAR(255),

                            attendance_recorded_date DATE NOT NULL,

                            created_at TIMESTAMP NOT NULL,
                            updated_at TIMESTAMP,

                            recorded_by_id UUID NOT NULL,
                            recorded_by_name VARCHAR(255) NOT NULL,

                            CONSTRAINT fk_attendance_session
                                FOREIGN KEY (session_id)
                                    REFERENCES attendance_session (session_id),

                            CONSTRAINT fk_attendance_student
                                FOREIGN KEY (student_id)
                                    REFERENCES student (student_id),

                            CONSTRAINT fk_attendance_program
                                FOREIGN KEY (program_id)
                                    REFERENCES program (program_id),

                            CONSTRAINT fk_attendance_cohort
                                FOREIGN KEY (cohort_id)
                                    REFERENCES cohort (cohort_id),

                            CONSTRAINT uk_attendance_session_student UNIQUE (session_id, student_id)
);

CREATE INDEX idx_attendance_date_cohort ON attendance (attendance_recorded_date, cohort_id);
CREATE INDEX idx_attendance_student_program ON attendance (student_id, program_id);

-- ==========================================
-- 5c. ATTENDANCE ALERT (3 absences in a row / in total)
-- ==========================================
CREATE TABLE attendance_alert (
                            alert_id UUID PRIMARY KEY,
                            student_id UUID NOT NULL,
                            program_id UUID NOT NULL,
                            cohort_id UUID NOT NULL,
                            alert_type VARCHAR(50) NOT NULL,
                            status VARCHAR(50) NOT NULL,
                            absence_count INTEGER NOT NULL,
                            triggered_on_date DATE NOT NULL,
                            triggered_by_id UUID NOT NULL,
                            triggered_by_name VARCHAR(255) NOT NULL,
                            created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                            resolved_at TIMESTAMP WITH TIME ZONE,
                            student_notified_at TIMESTAMP WITH TIME ZONE,
                            trainer_notified_at TIMESTAMP WITH TIME ZONE,

                            CONSTRAINT fk_alert_student
                                FOREIGN KEY (student_id)
                                    REFERENCES student (student_id),

                            CONSTRAINT fk_alert_program
                                FOREIGN KEY (program_id)
                                    REFERENCES program (program_id),

                            CONSTRAINT fk_alert_cohort
                                FOREIGN KEY (cohort_id)
                                    REFERENCES cohort (cohort_id)
);

CREATE INDEX idx_alert_student_program_status ON attendance_alert (student_id, program_id, status);

-- ==========================================
-- 6. PARTICIPANT
-- ==========================================
CREATE TABLE participant (
                             id UUID PRIMARY KEY,
                             student_id UUID NOT NULL,
                             program_id UUID NOT NULL,

                             attendance_points DOUBLE PRECISION,
                             attendance_percentage DOUBLE PRECISION,
                             progress_color VARCHAR(255),
                             consecutive_absences INTEGER,

                             last_updated TIMESTAMP
);

-- ==========================================
-- 7. APP USER
-- ==========================================
-- password is nullable: invited users have no password until they activate.
CREATE TABLE app_user (
                          user_id UUID PRIMARY KEY,
                          username VARCHAR(255) NOT NULL UNIQUE,
                          password VARCHAR(255),
                          full_name VARCHAR(255),
                          role VARCHAR(50) NOT NULL,
                          enabled BOOLEAN NOT NULL DEFAULT TRUE,
                          account_status VARCHAR(50)
);

-- Deferred FK: student.user_id -> app_user (app_user is created after student above)
ALTER TABLE student
    ADD CONSTRAINT fk_student_user
        FOREIGN KEY (user_id) REFERENCES app_user (user_id);

-- ==========================================
-- 8. ACTIVATION TOKEN (account invitation / activation)
-- ==========================================
CREATE TABLE activation_token (
                          id UUID PRIMARY KEY,
                          token VARCHAR(255) NOT NULL UNIQUE,
                          user_id UUID NOT NULL,
                          expires_at TIMESTAMP NOT NULL,
                          used_at TIMESTAMP,
                          created_at TIMESTAMP NOT NULL,

                          CONSTRAINT fk_activation_token_user
                              FOREIGN KEY (user_id)
                                  REFERENCES app_user (user_id)
);

CREATE INDEX idx_activation_token_token ON activation_token (token);
CREATE INDEX idx_activation_token_user ON activation_token (user_id);
