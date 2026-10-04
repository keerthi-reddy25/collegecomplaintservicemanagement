# Database Guide - `college_complaint_db`

This explains every table in simple language. Create everything by running `schema.sql`.

## Big picture

```
students (1) ────< (many) complaints (many) >──── (1) categories
                             │      │
employees (1) ──────────────┘      └────< (many) complaint_updates
```

- One **student** can raise **many complaints**.
- One **category** (for example Hostel) can have **many complaints**.
- One **employee** can be assigned **many complaints**; a complaint has **zero or one** employee (zero until the admin assigns it).
- One **complaint** has **many history entries** in `complaint_updates`.
- `admins` is a stand-alone table used only for admin login.

## Tables

### `students`
People who raise complaints.

| Column | Meaning |
|--------|---------|
| `student_id` | Primary key (auto number) |
| `name`, `phone`, `department` | Basic details |
| `email` | **UNIQUE** - used to log in |
| `password` | SHA-256 hash (64 characters), never the real password |
| `roll_number` | **UNIQUE** college roll number |
| `created_at` | When the student registered |

### `admins`
Administrators. Columns: `admin_id` (PK), `name`, `email` (UNIQUE), `password` (hash).

### `employees`
Staff who solve the complaints. Columns: `employee_id` (PK), `name`, `email` (UNIQUE), `password` (hash), `department` (e.g. IT Support), `phone`.

### `categories`
The list shown when raising a complaint. Columns: `category_id` (PK), `category_name` (UNIQUE), `description`.
Sample values: Internet/Wi-Fi, Classroom Maintenance, Hostel, Electricity, Library, Transport, Other.

### `complaints`
The most important table.

| Column | Meaning |
|--------|---------|
| `complaint_id` | Primary key |
| `student_id` | **Foreign key** -> `students.student_id` (who raised it). `ON DELETE CASCADE`: if a student is deleted, their complaints go too |
| `category_id` | **Foreign key** -> `categories.category_id`. `ON DELETE RESTRICT`: a category in use cannot be deleted |
| `employee_id` | **Foreign key** -> `employees.employee_id`, **nullable**. `ON DELETE SET NULL`: if an employee is deleted the complaint becomes unassigned |
| `title`, `description` | What the problem is |
| `status` | `OPEN`, `IN_PROGRESS`, `RESOLVED` or `CLOSED` (default `OPEN`) |
| `created_at` | Set automatically when inserted |
| `updated_at` | Updated automatically by MySQL whenever the row changes |

### `complaint_updates`
The history (audit log) of a complaint. One row for every event: complaint raised, assigned, status changed, comment added.

| Column | Meaning |
|--------|---------|
| `update_id` | Primary key |
| `complaint_id` | **Foreign key** -> `complaints.complaint_id`, `ON DELETE CASCADE` (history is deleted with its complaint) |
| `updated_by_role` | `STUDENT`, `ADMIN` or `EMPLOYEE` |
| `updated_by_name` | Name of the person (kept as text so the history stays readable) |
| `old_status` | Status before the change (NULL for the first entry) |
| `new_status` | Status after the change (same as old for a comment-only update) |
| `update_comment` | The note, e.g. "Router checked. Network issue identified." |
| `created_at` | When it happened |

## Relationships in one table

| From (child) | To (parent) | Column | On delete |
|--------------|-------------|--------|-----------|
| complaints | students | `student_id` | CASCADE |
| complaints | categories | `category_id` | RESTRICT |
| complaints | employees | `employee_id` | SET NULL |
| complaint_updates | complaints | `complaint_id` | CASCADE |

## Example: the full story of one complaint

| complaints row | complaint_updates rows |
|----------------|------------------------|
| status `OPEN`, `employee_id` NULL | STUDENT: NULL -> OPEN "Complaint raised by student." |
| status `IN_PROGRESS`, `employee_id` = 1 | ADMIN: OPEN -> IN_PROGRESS "Assigned to Employee One by admin." |
| (still `IN_PROGRESS`) | EMPLOYEE: IN_PROGRESS -> IN_PROGRESS "Router checked. Network issue identified." |
| status `RESOLVED` | EMPLOYEE: IN_PROGRESS -> RESOLVED "Router replaced. Wi-Fi working." |

## Passwords

Passwords are saved as SHA-256 hashes. In SQL: `SHA2('admin123', 256)`. In Java: `PasswordUtil.hash("admin123")`. Both give the same text, which is how login compares them.

## Other files

- `sample_data.sql` - 2 students, 3 complaints and their history for testing.
- `useful_queries.sql` - practice queries: INSERT, SELECT, WHERE, ORDER BY, JOIN, GROUP BY, COUNT, HAVING, UPDATE, DELETE.
