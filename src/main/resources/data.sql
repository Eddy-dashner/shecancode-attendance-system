INSERT INTO cohort (cohort_id, cohort_number, start_date, end_date)
VALUES ('550e8400-e29b-41d4-a716-446655440000', 'Cohort-10', '2026-03-01', '2026-09-01');

INSERT INTO program (program_id, program_name, program_Duration, program_start_date, program_end_date, cohort_id)
VALUES ('550e8400-e29b-41d4-a716-446655440010', 'Backend', 6, '2026-03-01', '2026-09-01', '550e8400-e29b-41d4-a716-446655440000');

INSERT INTO student (student_id, student_first_name, student_last_name, phone_number, email, home_address, student_status, current_occupation, cohort_id, program_id)
VALUES ('550e8400-e29b-41d4-a716-446655440011', 'Existing', 'User', '0780000000', 'existing@example.com', 'Kigali', 'ACTIVE', 'Student', '550e8400-e29b-41d4-a716-446655440000', '550e8400-e29b-41d4-a716-446655440010');

INSERT INTO attendance (attendance_id, student_id, program_id, cohort_id, check_in_time, attendance_status, remarks, attendance_recorded_date, created_at, updated_at, recorded_by_id, recorded_by_name)
VALUES (RANDOM_UUID(), '550e8400-e29b-41d4-a716-446655440011', '550e8400-e29b-41d4-a716-446655440010', '550e8400-e29b-41d4-a716-446655440000', '2026-03-01 12:00:00', 'PRESENT', 'Good', '2026-03-01 00:00:00', '2026-03-01 12:00:00', '2026-03-01 12:00:00', '550e8400-e29b-41d4-a716-446655440010', 'Existing User');

INSERT INTO participant (id, student_id, program_id, attendance_points, attendance_percentage, progress_color, consecutive_absences, last_updated)
VALUES (RANDOM_UUID(), '550e8400-e29b-41d4-a716-446655440011', '550e8400-e29b-41d4-a716-446655440010', 10, 10, 'green', '0', CURRENT_TIMESTAMP);

INSERT INTO app_user (user_id, username, password, full_name, role, enabled)
VALUES
    (RANDOM_UUID(), 'admin', '$2a$12$nLCkF1FmpAiN95U3q8gBge1NmFOSLWJSWjCBU9gfp9vfOFSN5qmMa', 'System Administrator', 'ADMIN', TRUE),
    (RANDOM_UUID(), 'trainer1', '$2a$12$Y1U6hXVhGmWBqLBFXBJLlOTMPRR/OjNR5sC4kqKSigjGFSmH/tlOu', 'Default Trainer', 'TRAINER', TRUE);