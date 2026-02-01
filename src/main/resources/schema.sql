CREATE TABLE student (
                         student_id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
                         student_first_name VARCHAR(100) NOT NULL,
                         student_last_name VARCHAR(100) NOT NULL,
                         phone_number VARCHAR(20),
                         email VARCHAR(100) NOT NULL UNIQUE,
                         home_address VARCHAR(255),
                         cohort_id UUID NOT NULL,
                         student_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                         current_occupation VARCHAR(100),
                         days_to_graduation INT NOT NULL,
                         total_graduation_days INT NOT NULL

);

CREATE TABLE cohort(
  cohort_id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
  cohort_number VARCHAR(100) NOT NULL,
  cohorts_starting_date DATE NOT NULL,
  cohorts_endDate DATE NOT NULL,
  graduation_date DATE NOT NULL,
  program_id UUID NOT NULL
);

CREATE TABLE PROGRAM(
    program_id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    program_name VARCHAR(100) NOT NULL,
    program_running_period INT NOT NULL
);