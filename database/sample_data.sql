-- =====================================================================
-- OPTIONAL extra test data (students + complaints + history).
-- Run this AFTER schema.sql:
--   SOURCE C:/path/to/CollegeComplaintServiceManagement/database/sample_data.sql;
--
-- Student logins (password for both: student123)
--   rahul@student.com
--   priya@student.com
-- =====================================================================

USE college_complaint_db;

INSERT INTO students (name, email, password, phone, department, roll_number) VALUES
    ('Rahul Sharma', 'rahul@student.com', SHA2('student123', 256), '9876543210', 'Computer Science', 'CS2021001'),
    ('Priya Reddy',  'priya@student.com', SHA2('student123', 256), '9876543211', 'Electronics',      'EC2021002');

-- Complaint 1: OPEN, not assigned yet (Rahul, Internet/Wi-Fi)
INSERT INTO complaints (student_id, category_id, employee_id, title, description, status) VALUES
    (1, 1, NULL, 'Wi-Fi not working in Block A',
        'Wi-Fi connection is unavailable in Block A classrooms since Monday.', 'OPEN');

-- Complaint 2: IN_PROGRESS, assigned to Employee Two (Priya, Electricity)
INSERT INTO complaints (student_id, category_id, employee_id, title, description, status) VALUES
    (2, 4, 2, 'Lights flickering in Hostel Block C',
        'Corridor lights on the second floor keep flickering at night.', 'IN_PROGRESS');

-- Complaint 3: RESOLVED, assigned to Employee One (Rahul, Library)
INSERT INTO complaints (student_id, category_id, employee_id, title, description, status) VALUES
    (1, 5, 1, 'Library computers are very slow',
        'The computers in the digital section take a long time to start.', 'RESOLVED');

-- History for complaint 1
INSERT INTO complaint_updates (complaint_id, updated_by_role, updated_by_name, old_status, new_status, update_comment) VALUES
    (1, 'STUDENT', 'Rahul Sharma', NULL, 'OPEN', 'Complaint raised by student.');

-- History for complaint 2
INSERT INTO complaint_updates (complaint_id, updated_by_role, updated_by_name, old_status, new_status, update_comment) VALUES
    (2, 'STUDENT',  'Priya Reddy',  NULL,   'OPEN',        'Complaint raised by student.'),
    (2, 'ADMIN',    'System Admin', 'OPEN', 'IN_PROGRESS', 'Assigned to Employee Two by admin.'),
    (2, 'EMPLOYEE', 'Employee Two', 'IN_PROGRESS', 'IN_PROGRESS', 'Electrician scheduled for tomorrow morning.');

-- History for complaint 3
INSERT INTO complaint_updates (complaint_id, updated_by_role, updated_by_name, old_status, new_status, update_comment) VALUES
    (3, 'STUDENT',  'Rahul Sharma', NULL,   'OPEN',        'Complaint raised by student.'),
    (3, 'ADMIN',    'System Admin', 'OPEN', 'IN_PROGRESS', 'Assigned to Employee One by admin.'),
    (3, 'EMPLOYEE', 'Employee One', 'IN_PROGRESS', 'RESOLVED', 'Reinstalled the operating system and cleaned startup programs.');
