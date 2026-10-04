# College Complaint & Service Management System

A Java application where students report college problems (Wi-Fi, hostel, electricity, library, transport, ...), admins assign them to employees, and employees update the status. It includes both a JavaFX desktop interface and the original console interface, backed by **Java 17, JDBC and MySQL 8**.

---

## 1. Project overview

| Role | What they do |
|------|--------------|
| **Student** | Registers, logs in, raises complaints, tracks status and history |
| **Admin** | Views/searches/filters complaints, assigns them to employees, sees statistics |
| **Employee** | Sees assigned complaints, updates status, adds comments, reads history |

Complaint life cycle: `OPEN` -> `IN_PROGRESS` (when assigned) -> `RESOLVED` -> `CLOSED`.
Every change is saved in the `complaint_updates` table, so the full history is preserved.

## 2. Features

**Student:** register, login, raise complaint (category + title + description), view all my complaints with status, view complaint details and history, logout.

**Admin:** login, view all complaints, search by ID (with optional student details), filter by status or category, assign a complaint to an employee, view employees (with workload), complaint statistics (totals, per status, per category, per employee), logout.

**Employee:** login, view assigned complaints, view details, update status with a comment, add an update without changing the status, view complaint history, logout.

**Technical features:** `PreparedStatement` everywhere (no SQL string concatenation), try-with-resources, transactions (commit/rollback) when two tables change together, SHA-256 password hashing, input validation that never crashes the program.

## 3. Technologies

- Java 17 (core Java + OOP)
- JavaFX desktop interface (`app.DesktopApp`)
- JDBC (`Connection`, `PreparedStatement`, `ResultSet`, `SQLException`)
- MySQL 8
- MySQL Connector/J (resolved by Maven for JavaFX; downloaded separately for Eclipse)
- Maven for running the JavaFX interface; Eclipse can still be used for the console application

## 4. Database structure

Database name: `college_complaint_db`

```
students ──┐
           ├──< complaints >──┬── categories
employees ─┘         │        
                     └──< complaint_updates
admins  (separate, used only for admin login)
```

| Table | Purpose |
|-------|---------|
| `students` | Registered students |
| `admins` | Admin accounts |
| `employees` | Staff who fix problems |
| `categories` | Complaint categories |
| `complaints` | Complaints (FK to student, category, employee) |
| `complaint_updates` | History of status changes and comments (FK to complaint) |

See `database/README_DATABASE.md` for a simple explanation of every table.

## 5. Folder structure

```
CollegeComplaintServiceManagement/
├── src/
│   ├── app/       Main.java                      (program entry point, main menu)
│   ├── config/    DBConnection.java              (JDBC connection)
│   ├── model/     Student, Admin, Employee, Category, Complaint,
│   │              ComplaintUpdate, ComplaintStatus
│   ├── dao/       StudentDAO, AdminDAO, EmployeeDAO, CategoryDAO, ComplaintDAO
│   │                                              (all SQL lives here)
│   ├── service/   StudentService, AdminService, EmployeeService
│   │                                              (console menus + logic)
│   └── util/      InputUtil, DisplayUtil, PasswordUtil
├── database/
│   ├── schema.sql            (creates DB, tables, categories, admin, employees)
│   ├── sample_data.sql       (optional: sample students, complaints, history)
│   ├── useful_queries.sql    (SQL practice: INSERT/SELECT/JOIN/GROUP BY/HAVING/UPDATE/DELETE)
│   └── README_DATABASE.md
├── lib/           (put the MySQL Connector/J JAR here)
├── pom.xml        (Maven dependencies and JavaFX launcher)
├── README.md
├── TESTING.md
├── .project / .classpath   (Eclipse project files)
└── .gitignore
```

How the layers work together: `Main` -> **Service** (menu, asks the user) -> **DAO** (runs SQL with JDBC) -> **MySQL**. **Model** classes carry data between the layers.

## 6. How to install MySQL (Windows)

1. Go to <https://dev.mysql.com/downloads/installer/> and download **MySQL Installer for Windows**.
2. Run it and choose **Developer Default** (or "Server only" + "Workbench").
3. When asked, set a **root password** and remember it. You will need it in step 8.
4. Finish the installation. MySQL runs as a Windows service on port **3306**.
5. Check it works: open **MySQL 8.x Command Line Client**, enter your root password, and you should see the `mysql>` prompt.

## 7. How to create the database

Option A - MySQL Command Line Client:

```sql
SOURCE C:/Users/YourName/Downloads/CollegeComplaintServiceManagement/database/schema.sql;
SOURCE C:/Users/YourName/Downloads/CollegeComplaintServiceManagement/database/sample_data.sql;
```

(Use forward slashes `/` in the path and change it to where you extracted the project.)

Option B - MySQL Workbench: open `database/schema.sql` (File > Open SQL Script), click the lightning bolt to run it, then do the same with `sample_data.sql`.

Check it worked:

```sql
USE college_complaint_db;
SHOW TABLES;                 -- 6 tables
SELECT * FROM categories;    -- 7 categories
```

> `schema.sql` **drops and recreates** the database, so running it again resets all data. `sample_data.sql` is optional but needed for the ready-made test data used in `TESTING.md`; run it only once (running it twice will fail on duplicate emails).

## 8. How to configure DBConnection.java

Open `src/config/DBConnection.java` and change:

```java
private static final String USERNAME = "root";                  // your MySQL user
private static final String PASSWORD = "YOUR_MYSQL_PASSWORD";   // <-- your real MySQL password
```

Only edit it on your own computer. Do not upload your real password to GitHub.

## 9. How to add MySQL Connector/J (JDBC driver)

The driver JAR is not included in this project; download it yourself:

1. Open <https://dev.mysql.com/downloads/connector/j/>.
2. In "Select Operating System" choose **Platform Independent**.
3. Download the **ZIP Archive**, extract it, and find `mysql-connector-j-x.x.x.jar` (any 8.x or newer version works with MySQL 8).
4. Copy the JAR into the project's `lib/` folder (create it if needed).
5. In Eclipse: right-click the project > **Build Path** > **Configure Build Path...** > **Libraries** tab > select **Classpath** > **Add JARs...** > choose `CollegeComplaintServiceManagement/lib/mysql-connector-j-x.x.x.jar` > **Apply and Close**.

   (If the JAR is somewhere else, use **Add External JARs...** instead.)

## 10. Run the JavaFX desktop interface in VS Code

1. Open the inner `CollegeComplaintServiceManagement` project folder (the folder containing `pom.xml`) in VS Code.
2. Confirm MySQL is running, the schema has been loaded, and the username/password in `src/config/DBConnection.java` match your local MySQL account.
3. Open **Terminal > New Terminal** and run:

   ```powershell
   mvn javafx:run
   ```

   Maven downloads JavaFX and MySQL Connector/J automatically. You do not need to add the Connector/J JAR to a separate VS Code classpath for this command.
4. Sign in using a student, admin, or employee account. Students can also create an account from the sign-in screen.

The command-line version remains available with its existing entry point, `app.Main`.

## 11. How to run the console project in Eclipse

1. Make sure **JDK 17** is installed. In Eclipse: *Window > Preferences > Java > Installed JREs* should list a JDK 17.
2. Extract the ZIP.
3. *File > Import... > General > Existing Projects into Workspace > Next*.
4. **Select root directory:** browse to the extracted `CollegeComplaintServiceManagement` folder > make sure the project is ticked > **Finish**.
5. If you see a JRE error: right-click project > *Build Path > Configure Build Path > Libraries*, remove the unbound "JavaSE-17" entry, then *Add Library... > JRE System Library > Alternate JRE* and pick your JDK 17.
6. Edit the password in `DBConnection.java` (section 8) and add the Connector/J JAR (section 9).
7. Open `src/app/Main.java`, right-click > **Run As > Java Application**.
8. Use the **Console** tab at the bottom of Eclipse to type your choices.

Troubleshooting:

| Message | Fix |
|---------|-----|
| `MySQL Connector/J JAR was not found` | Add the JAR to the Build Path (section 9) |
| `Access denied for user 'root'` | Wrong password in `DBConnection.java` |
| `Unknown database 'college_complaint_db'` | Run `database/schema.sql` |
| `Communications link failure` | MySQL service is not running (Windows: Services > MySQL80 > Start) |
| Passwords are visible while typing | Normal in the Eclipse console (it has no hidden-input support) |

## 12. Default login credentials

| Role | Email | Password |
|------|-------|----------|
| Admin | admin@college.com | admin123 |
| Employee 1 (IT Support) | employee1@college.com | emp123 |
| Employee 2 (Maintenance) | employee2@college.com | emp123 |
| Student (from `sample_data.sql`) | rahul@student.com | student123 |
| Student (from `sample_data.sql`) | priya@student.com | student123 |

Passwords are stored as SHA-256 hashes in the database.

## 12. Example usage

```
========================================
COLLEGE COMPLAINT & SERVICE MANAGEMENT
========================================
1. Student   2. Admin   3. Employee   4. Exit
```

1. **Student** -> Login (`rahul@student.com`) -> *Raise Complaint* -> category `Internet/Wi-Fi`, title `Wi-Fi not working in Block A`, description `Wi-Fi connection is unavailable in Block A classroom.` -> Complaint ID 4, status `OPEN`.
2. **Admin** -> Login -> *Assign Complaint* -> ID `4` -> employee `1` -> status becomes `IN_PROGRESS`.
3. **Employee** (`employee1@college.com`) -> *Add Complaint Update* -> `Router checked. Network issue identified.`
4. **Employee** -> *Update Complaint Status* -> ID `4` -> `RESOLVED` -> comment `Router replaced. Wi-Fi working.`
5. **Student** -> *View Complaint Details* -> ID `4` -> sees the status `RESOLVED` and the full history.

## 13. Future enhancements

- Hidden password input and password reset
- Salted password hashing (bcrypt/PBKDF2)
- Complaint priority and due dates
- Student can reopen or rate a resolved complaint
- Admin can add/remove employees and categories
- Email/SMS notifications
- Connection pooling instead of one connection per query
- Unit tests with JUnit
- A GUI (Swing/JavaFX) on top of the same DAO classes
