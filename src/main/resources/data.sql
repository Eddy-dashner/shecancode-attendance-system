INSERT INTO program (program_id, program_name, program_Duration, program_start_date, program_end_date)
VALUES ('550e8400-e29b-41d4-a716-446655440010', 'Backend', 6, '2026-03-01', '2026-09-01');

INSERT INTO cohort (cohort_id, cohort_number, start_date, end_date, program_id)
VALUES ('550e8400-e29b-41d4-a716-446655440000', 'Cohort-10', '2026-03-01', '2026-09-01', '550e8400-e29b-41d4-a716-446655440010');

INSERT INTO program (program_id, program_name, program_Duration, program_start_date, program_end_date)
VALUES ('550e8400-e29b-41d4-a716-446655440020', 'ADV_Backend', 6, '2026-03-01', '2026-09-01');

INSERT INTO cohort (cohort_id, cohort_number, start_date, end_date, program_id)
VALUES ('550e8400-e29b-41d4-a716-446655440001', 'Cohort-1', '2026-03-01', '2026-09-01', '550e8400-e29b-41d4-a716-446655440020');

INSERT INTO student (student_id, student_first_name, student_last_name, phone_number, email, home_address, student_status, current_occupation, cohort_id, program_id)
VALUES ('550e8400-e29b-41d4-a716-446655440011', 'Existing', 'User', '0780000000', 'existing@example.com', 'Kigali', 'ACTIVE', 'Student', '550e8400-e29b-41d4-a716-446655440000', '550e8400-e29b-41d4-a716-446655440010');

INSERT INTO attendance_session (session_id, cohort_id, program_id, session_date, taken_by_id, taken_by_name, created_at, updated_at)
VALUES ('550e8400-e29b-41d4-a716-446655440030', '550e8400-e29b-41d4-a716-446655440000', '550e8400-e29b-41d4-a716-446655440010', '2026-03-02', '550e8400-e29b-41d4-a716-446655440010', 'Default Trainer', '2026-03-02 12:00:00', '2026-03-02 12:00:00');

INSERT INTO attendance (attendance_id, session_id, student_id, program_id, cohort_id, check_in_time, attendance_status, remarks, attendance_recorded_date, created_at, updated_at, recorded_by_id, recorded_by_name)
VALUES ('550e8400-e29b-41d4-a716-446655440010', '550e8400-e29b-41d4-a716-446655440030', '550e8400-e29b-41d4-a716-446655440011', '550e8400-e29b-41d4-a716-446655440010', '550e8400-e29b-41d4-a716-446655440000', '09:00:00', 'PRESENT', 'Good', '2026-03-02', '2026-03-02 12:00:00', '2026-03-02 12:00:00', '550e8400-e29b-41d4-a716-446655440010', 'Default Trainer');

INSERT INTO participant (id, student_id, program_id, attendance_points, attendance_percentage, progress_color, consecutive_absences, last_updated)
VALUES ('550e8400-e29b-41d4-a716-446655440011', '550e8400-e29b-41d4-a716-446655440011', '550e8400-e29b-41d4-a716-446655440010', 1, 100, 'GREEN', 0, CURRENT_TIMESTAMP);

INSERT INTO app_user (user_id, username, password, full_name, role, enabled, account_status)
VALUES
    ('93c745fb-63d5-4a70-9a0e-a71f97cdd50e', 'admin@shecancode.org', '$2a$10$88FN2rob70SNlIdnOjGXa.QP8b2rPtyUFhx3ickUE62kSDP.x0.ha', 'System Administrator', 'ADMIN', TRUE, 'ACTIVE'),
    ('550e8400-e29b-41d4-a716-446655440010', 'trainer1@shecancode.org', '$2a$10$88FN2rob70SNlIdnOjGXa.QP8b2rPtyUFhx3ickUE62kSDP.x0.ha', 'Default Trainer', 'TRAINER', TRUE, 'ACTIVE');