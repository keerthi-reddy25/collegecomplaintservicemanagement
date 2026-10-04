-- =====================================================================
-- Useful SQL queries for learning (MySQL 8)
-- Run schema.sql (and optionally sample_data.sql) first.
-- =====================================================================
USE college_complaint_db;

-- ---------------- INSERT ----------------
-- A student registers (password stored as SHA-256 hash)
INSERT INTO students (name, email, password, phone, department, roll_number)
VALUES ('Test Student', 'test@student.com', SHA2('test1234', 256), '9999999999', 'Mechanical', 'ME2021010');

-- A student raises a complaint, and its first history entry is stored
INSERT INTO complaints (student_id, category_id, title, description)
VALUES (1, 1, 'Wi-Fi slow in library', 'Internet speed is very low in the library.');

INSERT INTO complaint_updates (complaint_id, updated_by_role, updated_by_name, old_status, new_status, update_comment)
VALUES (LAST_INSERT_ID(), 'STUDENT', 'Rahul Sharma', NULL, 'OPEN', 'Complaint raised by student.');

-- ---------------- SELECT / WHERE / ORDER BY ----------------
SELECT * FROM complaints WHERE status = 'OPEN' ORDER BY created_at DESC;

SELECT complaint_id, title, status FROM complaints WHERE student_id = 1 ORDER BY complaint_id;

-- ---------------- JOIN ----------------
-- Every complaint with student, category and assigned employee (LEFT JOIN keeps unassigned ones)
SELECT c.complaint_id, c.title, c.status,
       s.name AS student, cat.category_name AS category, e.name AS assigned_to
FROM complaints c
JOIN students s     ON c.student_id  = s.student_id
JOIN categories cat ON c.category_id = cat.category_id
LEFT JOIN employees e ON c.employee_id = e.employee_id
ORDER BY c.complaint_id;

-- Full history of complaint 2
SELECT u.created_at, u.updated_by_role, u.updated_by_name, u.old_status, u.new_status, u.update_comment
FROM complaint_updates u
WHERE u.complaint_id = 2
ORDER BY u.created_at, u.update_id;

-- ---------------- COUNT / GROUP BY ----------------
SELECT COUNT(*) AS total_complaints FROM complaints;

SELECT status, COUNT(*) AS total FROM complaints GROUP BY status;

-- Complaints per category (categories with no complaints show 0)
SELECT cat.category_name, COUNT(c.complaint_id) AS total
FROM categories cat
LEFT JOIN complaints c ON cat.category_id = c.category_id
GROUP BY cat.category_id, cat.category_name
ORDER BY total DESC;

-- ---------------- HAVING ----------------
-- Students who raised more than one complaint
SELECT s.name, COUNT(c.complaint_id) AS total
FROM students s
JOIN complaints c ON s.student_id = c.student_id
GROUP BY s.student_id, s.name
HAVING COUNT(c.complaint_id) > 1;

-- Employees with at least one complaint
SELECT e.name, COUNT(c.complaint_id) AS total
FROM employees e
JOIN complaints c ON e.employee_id = c.employee_id
GROUP BY e.employee_id, e.name
HAVING COUNT(c.complaint_id) >= 1;

-- ---------------- UPDATE ----------------
-- Admin assigns complaint 1 to employee 1
UPDATE complaints SET employee_id = 1, status = 'IN_PROGRESS' WHERE complaint_id = 1;

-- Employee resolves complaint 1
UPDATE complaints SET status = 'RESOLVED' WHERE complaint_id = 1 AND employee_id = 1;

-- ---------------- DELETE ----------------
-- Remove the test student created above (its complaints would be deleted too: ON DELETE CASCADE)
DELETE FROM students WHERE email = 'test@student.com';

-- Remove old CLOSED complaints (their history rows are deleted automatically: ON DELETE CASCADE)
DELETE FROM complaints WHERE status = 'CLOSED' AND updated_at < NOW() - INTERVAL 1 YEAR;
