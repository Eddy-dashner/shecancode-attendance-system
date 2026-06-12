-- ==========================================
-- 1. DROP TABLES (children first)
-- ==========================================
DROP TABLE IF EXISTS attendance CASCADE;
DROP TABLE IF EXISTS participant CASCADE;
DROP TABLE IF EXISTS student CASCADE;
DROP TABLE IF EXISTS program CASCADE;
DROP TABLE IF EXISTS cohort CASCADE;
DROP TABLE IF EXISTS app_user CASCADE;

-- ==========================================
-- 2. COHORT (Parent Table)
-- ==========================================
CREATE TABLE cohort (
                        cohort_id UUID PRIMARY KEY,
                        cohort_number VARCHAR(255) NOT NULL UNIQUE,
                        start_date DATE NOT NULL,
                        end_date DATE NOT NULL
);

-- ==========================================
-- 3. PROGRAM
-- ==========================================
CREATE TABLE program (
                         program_id UUID PRIMARY KEY,
                         program_name VARCHAR(255) NOT NULL,
                         program_duration INTEGER,
                         program_start_date DATE NOT NULL,
                         program_end_date DATE NOT NULL,
                         cohort_id UUID NOT NULL,

                         CONSTRAINT fk_program_cohort
                             FOREIGN KEY (cohort_id)
                                 REFERENCES cohort (cohort_id)
);

-- ==========================================
-- 4. STUDENT
-- ==========================================
CREATE TABLE student (
                         student_id UUID PRIMARY KEY,
                         student_first_name VARCHAR(255) NOT NULL,
                         student_last_name VARCHAR(255) NOT NULL,
                         email VARCHAR(255) NOT NULL UNIQUE,
                         phone_number VARCHAR(255),
                         home_address VARCHAR(255),
                         current_occupation VARCHAR(255),
                         student_status VARCHAR(50),
                         cohort_id UUID NOT NULL,
                         program_id UUID NOT NULL,

                         CONSTRAINT fk_student_cohort
                             FOREIGN KEY (cohort_id)
                                 REFERENCES cohort (cohort_id),

                         CONSTRAINT fk_student_program
                             FOREIGN KEY (program_id)
                                 REFERENCES program (program_id)
);

-- ==========================================
-- 5. ATTENDANCE (FIXED)
-- ==========================================
CREATE TABLE attendance (
                            attendance_id UUID PRIMARY KEY,
                            student_id UUID NOT NULL,
                            program_id UUID NOT NULL,
                            cohort_id UUID NOT NULL,

                            check_in_time TIMESTAMP,
                            attendance_status VARCHAR(255),
                            remarks VARCHAR(255),

                            attendance_recorded_date DATE NOT NULL,

                            created_at TIMESTAMP,
                            updated_at TIMESTAMP,

                            recorded_by_id UUID,
                            recorded_by_name VARCHAR(255),

                            CONSTRAINT fk_attendance_student
                                FOREIGN KEY (student_id)
                                    REFERENCES student (student_id),

                            CONSTRAINT fk_attendance_program
                                FOREIGN KEY (program_id)
                                    REFERENCES program (program_id),

                            CONSTRAINT fk_attendance_cohort
                                FOREIGN KEY (cohort_id)
                                    REFERENCES cohort (cohort_id)
);

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
CREATE TABLE app_user (
                          user_id UUID PRIMARY KEY,
                          username VARCHAR(255) NOT NULL UNIQUE,
                          password VARCHAR(255) NOT NULL,
                          full_name VARCHAR(255),
                          role VARCHAR(50) NOT NULL,
                          enabled BOOLEAN NOT NULL DEFAULT TRUE
);
