-- ==========================================
-- 1. DROP TABLES IN ORDER (Children first)
-- ==========================================
-- We use CASCADE to ensure any remaining constraints are cleared
DROP TABLE IF EXISTS student CASCADE;
DROP TABLE IF EXISTS program CASCADE;
DROP TABLE IF EXISTS cohort CASCADE;

-- ==========================================
-- 2. CREATE COHORT (Parent)
-- ==========================================
CREATE TABLE cohort (
                        cohort_id UUID NOT NULL,
                        cohort_number VARCHAR(255) NOT NULL UNIQUE,
                        start_date DATE NOT NULL,
                        end_date DATE NOT NULL,
                        graduation_date DATE NOT NULL,
                        PRIMARY KEY (cohort_id)
);

-- ==========================================
-- 3. CREATE PROGRAM (Middle Child)
-- ==========================================
CREATE TABLE program (
                         program_id UUID NOT NULL,
                         program_name VARCHAR(255) NOT NULL,
                         program_running_period INTEGER,
                         cohort_id UUID NOT NULL,
                         PRIMARY KEY (program_id),
                         CONSTRAINT fk_program_cohort FOREIGN KEY (cohort_id) REFERENCES cohort (cohort_id)
);

-- ==========================================
-- 4. CREATE STUDENT (Child)
-- ==========================================
CREATE TABLE student (
                         student_id UUID NOT NULL,
                         student_first_name VARCHAR(255) NOT NULL,
                         student_last_name VARCHAR(255) NOT NULL,
                         email VARCHAR(255) NOT NULL UNIQUE,
                         phone_number VARCHAR(255),
                         home_address VARCHAR(255),
                         current_occupation VARCHAR(255),
                         student_status VARCHAR(50), -- Mapped from Enum String
                         days_to_graduation INTEGER,
                         total_graduation_days INTEGER,
                         cohort_id UUID NOT NULL,
                         program_id UUID NOT NULL,
                         PRIMARY KEY (student_id),
                         CONSTRAINT fk_student_cohort FOREIGN KEY (cohort_id) REFERENCES cohort (cohort_id),
                         CONSTRAINT fk_student_program FOREIGN KEY (program_id) REFERENCES program (program_id)
);