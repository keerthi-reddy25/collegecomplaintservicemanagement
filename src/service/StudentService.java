package service;

import java.sql.SQLException;
import java.util.List;

import dao.CategoryDAO;
import dao.ComplaintDAO;
import dao.StudentDAO;
import model.Category;
import model.Complaint;
import model.Student;
import util.DisplayUtil;
import util.InputUtil;
import util.PasswordUtil;

/**
 * Console menus and logic for the STUDENT role.
 */
public class StudentService {

    private final StudentDAO studentDAO = new StudentDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final ComplaintDAO complaintDAO = new ComplaintDAO();

    /** Student menu: Register / Login / Back. */
    public void start() {
        boolean back = false;
        while (!back) {
            System.out.println();
            System.out.println("-------------- STUDENT --------------");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Back");
            int choice = InputUtil.readIntInRange("Enter your choice: ", 1, 3);

            switch (choice) {
                case 1:
                    register();
                    break;
                case 2:
                    Student student = login();
                    if (student != null) {
                        studentMenu(student);
                    }
                    break;
                default:
                    back = true;
                    break;
            }
        }
    }

    // ------------------------------------------------------------------
    // Register / Login
    // ------------------------------------------------------------------

    private void register() {
        System.out.println();
        System.out.println("---------- STUDENT REGISTRATION ----------");
        String name = InputUtil.readRequired("Full name: ", 100);
        String email = InputUtil.readEmail("Email: ");

        try {
            if (studentDAO.emailExists(email)) {
                System.out.println("This email is already registered. Please login instead.");
                return;
            }

            String password = InputUtil.readNewPassword("Password (minimum 6 characters): ");
            String phone = InputUtil.readPhone("Phone (10 digits): ");
            String department = InputUtil.readRequired("Department: ", 100);
            String rollNumber = InputUtil.readRequired("Roll number: ", 30);

            if (studentDAO.rollNumberExists(rollNumber)) {
                System.out.println("This roll number is already registered.");
                return;
            }

            Student student = new Student(name, email, PasswordUtil.hash(password), phone, department, rollNumber);
            int newId = studentDAO.addStudent(student);
            System.out.println("Registration successful! Your student ID is " + newId + ". You can now login.");
        } catch (SQLException e) {
            System.out.println("Database error while registering: " + e.getMessage());
        }
    }

    private Student login() {
        System.out.println();
        System.out.println("------------- STUDENT LOGIN -------------");
        String email = InputUtil.readRequired("Email: ", 100).toLowerCase();
        String password = InputUtil.readRequired("Password: ", 100);

        try {
            Student student = studentDAO.login(email, PasswordUtil.hash(password));
            if (student == null) {
                System.out.println("Invalid email or password.");
                return null;
            }
            System.out.println("Login successful. Welcome, " + student.getName() + "!");
            return student;
        } catch (SQLException e) {
            System.out.println("Database error while logging in: " + e.getMessage());
            return null;
        }
    }

    // ------------------------------------------------------------------
    // Menu after login
    // ------------------------------------------------------------------

    private void studentMenu(Student student) {
        boolean logout = false;
        while (!logout) {
            System.out.println();
            System.out.println("======== STUDENT MENU (" + student.getName() + ") ========");
            System.out.println("1. Raise Complaint");
            System.out.println("2. View My Complaints");
            System.out.println("3. View Complaint Details");
            System.out.println("4. Logout");
            int choice = InputUtil.readIntInRange("Enter your choice: ", 1, 4);

            switch (choice) {
                case 1:
                    raiseComplaint(student);
                    break;
                case 2:
                    viewMyComplaints(student);
                    break;
                case 3:
                    viewComplaintDetails(student);
                    break;
                default:
                    System.out.println("You have been logged out.");
                    logout = true;
                    break;
            }
        }
    }

    private void raiseComplaint(Student student) {
        System.out.println();
        System.out.println("------------- RAISE COMPLAINT -------------");
        try {
            List<Category> categories = categoryDAO.getAllCategories();
            if (categories.isEmpty()) {
                System.out.println("No categories found in the database. Please run database/schema.sql.");
                return;
            }

            System.out.println("Select a category:");
            for (int i = 0; i < categories.size(); i++) {
                System.out.println((i + 1) + ". " + categories.get(i).getCategoryName());
            }
            int choice = InputUtil.readIntInRange("Category number: ", 1, categories.size());
            Category category = categories.get(choice - 1);

            String title = InputUtil.readRequired("Complaint title: ", 150);
            String description = InputUtil.readRequired("Detailed description: ", 2000);

            Complaint complaint = new Complaint(student.getStudentId(), category.getCategoryId(), title, description);
            int newId = complaintDAO.addComplaint(complaint, student.getName());

            System.out.println("Complaint raised successfully!");
            System.out.println("Complaint ID : " + newId);
            System.out.println("Category     : " + category.getCategoryName());
            System.out.println("Status       : OPEN");
        } catch (SQLException e) {
            System.out.println("Database error while raising the complaint: " + e.getMessage());
        }
    }

    private void viewMyComplaints(Student student) {
        System.out.println();
        System.out.println("---------------- MY COMPLAINTS ----------------");
        try {
            List<Complaint> complaints = complaintDAO.getComplaintsByStudent(student.getStudentId());
            DisplayUtil.printComplaintTable(complaints, false);
        } catch (SQLException e) {
            System.out.println("Database error while loading complaints: " + e.getMessage());
        }
    }

    private void viewComplaintDetails(Student student) {
        int complaintId = InputUtil.readInt("Enter complaint ID: ");
        try {
            // Only complaints that belong to this student can be opened.
            Complaint complaint = complaintDAO.getComplaintForStudent(complaintId, student.getStudentId());
            if (complaint == null) {
                System.out.println("Complaint not found. Check the ID in 'View My Complaints'.");
                return;
            }
            DisplayUtil.printComplaintDetails(complaint);
            DisplayUtil.printHistory(complaintDAO.getHistory(complaintId));
        } catch (SQLException e) {
            System.out.println("Database error while loading the complaint: " + e.getMessage());
        }
    }
}
