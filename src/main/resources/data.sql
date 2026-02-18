-- ==========================================
-- 1. INSERT COHORTS
-- ==========================================
INSERT INTO cohort (
    cohort_id,
    cohort_number,
    start_date,
    end_date
--     graduation_date
) VALUES (
             '550e8400-e29b-41d4-a716-446655440000',
             'Cohort-10', -- Matches your Postman test
             '2026-03-01',
             '2026-09-01'
--              '2026-09-15'
         );

-- ==========================================
-- 2. INSERT PROGRAMS
-- ==========================================
INSERT INTO program (
    program_id,
    program_name,
    program_Duration,
    program_start_date,
    program_end_date,
    cohort_id
) VALUES (
             '550e8400-e29b-41d4-a716-446655440010',
             'Backend', -- Matches your Postman test ("Backend" vs "Backend Development")
             6,
             '2026-03-01',
             '2026-09-01',
--               50,
             '550e8400-e29b-41d4-a716-446655440000'
         );

-- ==========================================
-- 3. INSERT INITIAL STUDENTS (Optional)
-- ==========================================
-- You can keep these or leave the table empty to test your POST request
INSERT INTO student (
    student_id,
    student_first_name,
    student_last_name,
    phone_number,
    email,
    home_address,
    student_status,
    current_occupation,
--     days_to_graduation,
--     total_graduation_days,
    cohort_id,
    program_id
) VALUES (
             RANDOM_UUID(),
             'Existing', 'User', '0780000000', 'existing@example.com',
             'Kigali', 'ACTIVE', 'Student',
             '550e8400-e29b-41d4-a716-446655440000',
             '550e8400-e29b-41d4-a716-446655440010'
         );