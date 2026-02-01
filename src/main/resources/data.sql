INSERT INTO student (
    student_first_name,
    student_last_name,
    phone_number,
    email,
    home_address,
    cohort_id,
    student_status,
    current_occupation,
    days_to_graduation,
    total_graduation_days
)
VALUES
    (
        'John', 'Doe', '1234567890', 'john.doe@example.com',
        '123 Main St', '550e8400-e29b-41d4-a716-446655440000', 'ACTIVE', 'Software Engineer', 88, 88
    ),
    (
        'Joseph', 'Ineza', '1890234567', 'joseph.ineza@example.com',
        'KG 123 St', '550e8400-e29b-41d4-a716-446655440001', 'ACTIVE', 'System Engineer', 88, 88
    ),
    (
        'Musa', 'Kime', '123789848490', 'kime.musa@example.com',
        'Remera 45', '550e8400-e29b-41d4-a716-446655440002', 'ACTIVE', 'DevOps Engineer', 88, 88
    );

INSERT INTO cohort (
    cohort_id,
    cohort_number,
    cohorts_starting_date,
    cohorts_endDate,
    graduation_date,
    program_id
)
VALUES
    ('550e8400-e29b-41d4-a716-446655440000', 'Cohort-1', '2024-01-01', '2024-06-01', '2024-06-15', '550e8400-e29b-41d4-a716-446655440010'),
    ('550e8400-e29b-41d4-a716-446655440001', 'Cohort-2', '2024-02-01', '2024-07-01', '2024-07-15', '550e8400-e29b-41d4-a716-446655440010'),
    ('550e8400-e29b-41d4-a716-446655440002', 'Cohort-3', '2024-03-01', '2024-08-01', '2024-08-15', '550e8400-e29b-41d4-a716-446655440011');

INSERT INTO PROGRAM (
    program_id,
    program_name,
    program_running_period
)
VALUES
    ('550e8400-e29b-41d4-a716-446655440010', 'Software Engineering', 180),
    ('550e8400-e29b-41d4-a716-446655440011', 'Data Science', 200);