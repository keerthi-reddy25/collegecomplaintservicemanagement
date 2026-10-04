-- =====================================================================
-- College Complaint & Service Management System
-- Database setup script (MySQL 8)
--
-- WARNING: this script DROPS and re-creates the database
-- "college_complaint_db". Running it again resets everything.
--
-- How to run (MySQL Command Line Client or MySQL Workbench):
--   SOURCE C:/path/to/CollegeComplaintServiceManagement/database/schema.sql;
-- =====================================================================

DROP DATABASE IF EXISTS college_complaint_db;

CREATE DATABASE college_complaint_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE college_complaint_db;

-- ---------------------------------------------------------------------
-- 1. students
-- ---------------------------------------------------------------------
CREATE TABLE students (
    student_id   INT          NOT NULL AUTO_INCREMENT,
    name         VARCHAR(100) NOT NULL,
    email        VARCHAR(100) NOT NULL,
    password     VARCHAR(64)  NOT NULL,
    phone        VARCHAR(15)  NOT NULL,
    department   VARCHAR(100) NOT NULL,
    roll_number  VARCHAR(30)  NOT NULL,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_students PRIMARY KEY (student_id),
    CONSTRAINT uq_students_email UNIQUE (email),
    CONSTRAINT uq_students_roll UNIQUE (roll_number)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 2. admins
-- ---------------------------------------------------------------------
CREATE TABLE admins (
    admin_id   INT          NOT NULL AUTO_INCREMENT,
    name       VARCHAR(100) NOT NULL,
    email      VARCHAR(100) NOT NULL,
    password   VARCHAR(64)  NOT NULL,
    CONSTRAINT pk_admins PRIMARY KEY (admin_id),
    CONSTRAINT uq_admins_email UNIQUE (email)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 3. employees
-- ---------------------------------------------------------------------
CREATE TABLE employees (
    employee_id  INT          NOT NULL AUTO_INCREMENT,
    name         VARCHAR(100) NOT NULL,
    email        VARCHAR(100) NOT NULL,
    password     VARCHAR(64)  NOT NULL,
    department   VARCHAR(100) NOT NULL,
    phone        VARCHAR(15)  NOT NULL,
    CONSTRAINT pk_employees PRIMARY KEY (employee_id),
    CONSTRAINT uq_employees_email UNIQUE (email)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 4. categories
-- ---------------------------------------------------------------------
CREATE TABLE categories (
    category_id    INT          NOT NULL AUTO_INCREMENT,
    category_name  VARCHAR(100) NOT NULL,
    description    VARCHAR(255) NOT NULL,
    CONSTRAINT pk_categories PRIMARY KEY (category_id),
    CONSTRAINT uq_categories_name UNIQUE (category_name)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 5. complaints
--    Student -> Complaint, Category -> Complaint, Employee -> Complaint
-- ---------------------------------------------------------------------
CREATE TABLE complaints (
    complaint_id  INT          NOT NULL AUTO_INCREMENT,
    student_id    INT          NOT NULL,
    category_id   INT          NOT NULL,
    employee_id   INT          NULL,
    title         VARCHAR(150) NOT NULL,
    description   TEXT         NOT NULL,
    status        ENUM('OPEN','IN_PROGRESS','RESOLVED','CLOSED') NOT NULL DEFAULT 'OPEN',
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_complaints PRIMARY KEY (complaint_id),
    CONSTRAINT fk_complaints_student
        FOREIGN KEY (student_id)  REFERENCES students (student_id)    ON DELETE CASCADE,
    CONSTRAINT fk_complaints_category
        FOREIGN KEY (category_id) REFERENCES categories (category_id) ON DELETE RESTRICT,
    CONSTRAINT fk_complaints_employee
        FOREIGN KEY (employee_id) REFERENCES employees (employee_id)  ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE INDEX idx_complaints_status   ON complaints (status);
CREATE INDEX idx_complaints_category ON complaints (category_id);
CREATE INDEX idx_complaints_employee ON complaints (employee_id);

-- ---------------------------------------------------------------------
-- 6. complaint_updates  (history of status changes and comments)
-- ---------------------------------------------------------------------
CREATE TABLE complaint_updates (
    update_id        INT          NOT NULL AUTO_INCREMENT,
    complaint_id     INT          NOT NULL,
    updated_by_role  ENUM('STUDENT','ADMIN','EMPLOYEE') NOT NULL,
    updated_by_name  VARCHAR(100) NOT NULL,
    old_status       ENUM('OPEN','IN_PROGRESS','RESOLVED','CLOSED') NULL,
    new_status       ENUM('OPEN','IN_PROGRESS','RESOLVED','CLOSED') NOT NULL,
    update_comment   TEXT         NOT NULL,
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_complaint_updates PRIMARY KEY (update_id),
    CONSTRAINT fk_updates_complaint
        FOREIGN KEY (complaint_id) REFERENCES complaints (complaint_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_updates_complaint ON complaint_updates (complaint_id);

-- =====================================================================
-- SAMPLE DATA
-- Passwords are stored as SHA-256 hashes. MySQL SHA2(text, 256) produces
-- exactly the same lowercase hex string as the Java PasswordUtil class.
-- =====================================================================

INSERT INTO categories (category_name, description) VALUES
    ('Internet/Wi-Fi',        'Wi-Fi or network not working'),
    ('Classroom Maintenance', 'Broken benches, projectors, fans, boards, etc.'),
    ('Hostel',                'Hostel rooms, mess, water, cleanliness'),
    ('Electricity',           'Power cuts, faulty wiring, lights, sockets'),
    ('Library',               'Books, seating, library services'),
    ('Transport',             'College bus timing, routes, vehicle problems'),
    ('Other',                 'Any other college service');

-- Default admin (email: admin@college.com, password: admin123)
INSERT INTO admins (name, email, password) VALUES
    ('System Admin', 'admin@college.com', SHA2('admin123', 256));

-- Sample employees (password for both: emp123)
INSERT INTO employees (name, email, password, department, phone) VALUES
    ('Employee One', 'employee1@college.com', SHA2('emp123', 256), 'IT Support',  '9000000001'),
    ('Employee Two', 'employee2@college.com', SHA2('emp123', 256), 'Maintenance', '9000000002');
