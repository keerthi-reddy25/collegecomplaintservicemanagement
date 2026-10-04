# Testing Guide

Manual test cases for the console application.

## Before you start

1. Run `database/schema.sql`, then `database/sample_data.sql` (a fresh database gives predictable IDs).
2. Set your MySQL password in `DBConnection.java`, add Connector/J to the Build Path, run `app.Main`.
3. Sample data gives: students `rahul@student.com` / `priya@student.com` (password `student123`), complaints **1** (OPEN, unassigned), **2** (IN_PROGRESS, Employee Two), **3** (RESOLVED, Employee One).
4. Run the tests in order. The first complaint you create becomes ID **4**.

To reset at any time, run `schema.sql` and `sample_data.sql` again.

| # | Test case | Steps | Expected result |
|---|-----------|-------|-----------------|
| 1 | Application starts | Run `Main` with MySQL running | Main menu with options 1-4 is shown |
| 2 | Database not reachable | Put a wrong password in `DBConnection.java`, run | Message "Could not connect to the database" and the program ends without a crash stack trace (restore the password afterwards) |
| 3 | Invalid menu input | At the main menu type `abc`, then `9`, then empty Enter | "Invalid number" / "Please enter a number between 1 and 4" and the menu asks again |
| 4 | Student registration (valid) | 1 > 1 > name `Anita Rao`, email `anita@student.com`, password `anita123`, phone `9123456780`, department `Civil`, roll `CV2021003` | "Registration successful" with a student ID (3) |
| 5 | Registration: duplicate email | Register again with `anita@student.com` | "This email is already registered" and nothing is saved |
| 6 | Registration: duplicate roll number | Register new email `anita2@student.com` with roll `CV2021003` | "This roll number is already registered" |
| 7 | Registration: invalid input | Email `anita.com`; password `123`; phone `12345` | Each field is rejected with a message and asked again |
| 8 | Student login (valid) | 1 > 2 > `anita@student.com` / `anita123` | "Login successful. Welcome, Anita Rao!" and the student menu appears |
| 9 | Invalid login | 1 > 2 > `anita@student.com` / `wrongpass` | "Invalid email or password." and you return to the student menu |
| 10 | Raise complaint | Login as `rahul@student.com`. Raise Complaint > category `1` (Internet/Wi-Fi), title `Wi-Fi not working in Block A`, description `Wi-Fi connection is unavailable in Block A classroom.` | "Complaint raised successfully", Complaint ID 4, Status OPEN |
| 11 | Raise complaint: empty / invalid input | Raise Complaint > category `0`, then `99`, then valid `3`; leave title empty | Invalid category and empty title are rejected and asked again |
| 12 | View my complaints | As Rahul: View My Complaints | Table shows complaints 1, 3 and 4 with statuses OPEN, RESOLVED, OPEN. Priya's complaint 2 is NOT listed |
| 13 | View complaint details + history | As Rahul: View Complaint Details > `4` | Details (category, status OPEN, "Not assigned yet") and history with one entry "Complaint raised by student." |
| 14 | Student cannot see another student's complaint | As Rahul: View Complaint Details > `2` | "Complaint not found" |
| 15 | Student logout | Student menu > 4 | "You have been logged out." and the student login menu appears |
| 16 | Admin login (valid and invalid) | 2 > `admin@college.com` / `wrong`, then again with `admin123` | First attempt: "Invalid email or password."; second: "Login successful" and the admin menu |
| 17 | Admin: view all complaints | Admin menu > 1 | Table of 4 complaints with student names and assigned employee ("Not assigned" for 1 and 4) |
| 18 | Admin: search by ID | Admin menu > 2 > `4`, then `y` for student details | Complaint 4 details and history, then Rahul Sharma's details (roll `CS2021001`, email, phone) |
| 19 | Admin: search unknown ID | Admin menu > 2 > `999` | "No complaint found with ID 999." |
| 20 | Admin: filter by status | Admin menu > 3 > 1 > status `1` (OPEN) | Only complaints 1 and 4 |
| 21 | Admin: filter by category | Admin menu > 3 > 2 > category `1` (Internet/Wi-Fi) | Only complaints 1 and 4 |
| 22 | Admin: view employees | Admin menu > 5 | Employees 1 and 2 with Assigned and Active counts (Employee One: 1 assigned, 0 active; Employee Two: 1 assigned, 1 active) |
| 23 | Admin: assign complaint | Admin menu > 4 > complaint `4` > employee `1` | "assigned to Employee One. Status is now IN_PROGRESS." |
| 24 | Admin: assign invalid / not allowed | Assign complaint `3` (RESOLVED); then complaint `4` with employee `99` | Complaint 3: "already RESOLVED and cannot be assigned". Employee 99: "No employee found" |
| 25 | Admin: statistics | Admin menu > 6 | Total 4, Open 1, In-progress 2, Resolved 1, Closed 0, category counts (Internet/Wi-Fi 2, Electricity 1, Library 1, others 0), employee counts (Employee One 2, Employee Two 1) |
| 26 | Employee login | 3 > `employee1@college.com` / `emp123` | Login successful, employee menu appears |
| 27 | Employee: view assigned complaints | Employee menu > 1 | Complaints 3 and 4 only |
| 28 | Employee: complaint not assigned to me | Employee menu > 2 > `2` | "Complaint not found among your assigned complaints." |
| 29 | Employee: add update | Employee menu > 4 > `4` > `Router checked. Network issue identified.` | "Update added to complaint 4." Status stays IN_PROGRESS |
| 30 | Employee: update status | Employee menu > 3 > `4` > status `2` (RESOLVED) > comment `Router replaced. Wi-Fi working.` | "Status changed from IN_PROGRESS to RESOLVED." |
| 31 | Employee: same status rejected | Employee menu > 3 > `4` > status `2` again | "already has status RESOLVED. Nothing changed." |
| 32 | Employee: view history | Employee menu > 5 > `4` | 4 entries in order: raised (STUDENT), assigned (ADMIN), comment (EMPLOYEE), OPEN/IN_PROGRESS -> RESOLVED (EMPLOYEE) |
| 33 | Closed complaint is locked | Employee menu > 3 > `4` > status `3` (CLOSED) > comment `Verified`; then try Update Status on `4` again | First: status changes to CLOSED. Second: "This complaint is CLOSED and can no longer be changed." |
| 34 | Student sees the result | Login as Rahul > View Complaint Details > `4` | Status CLOSED, assigned to Employee One, full history with all comments |
| 35 | Employee wrong password | 3 > `employee1@college.com` / `bad` | "Invalid email or password." |
| 36 | Exit | Main menu > 4 | "Thank you for using the system. Goodbye!" and the program stops |

## Checking the database directly (optional)

After test 30, in MySQL:

```sql
USE college_complaint_db;
SELECT complaint_id, status, employee_id FROM complaints WHERE complaint_id = 4;
SELECT updated_by_role, old_status, new_status, update_comment
FROM complaint_updates WHERE complaint_id = 4 ORDER BY update_id;
```

You should see the same history that the application shows.

## Test result sheet

Copy this table and fill it in:

| # | Pass / Fail | Notes |
|---|-------------|-------|
| 1 | | |
| 2 | | |
| ... | | |
