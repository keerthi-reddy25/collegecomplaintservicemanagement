package app;

import config.DBConnection;
import service.AdminService;
import service.EmployeeService;
import service.StudentService;
import util.InputUtil;

/**
 * Entry point of the College Complaint & Service Management System.
 */
public class Main {

    public static void main(String[] args) {
        if (!DBConnection.testConnection()) {
            System.out.println();
            System.out.println("The application cannot start without a database connection.");
            return;
        }

        StudentService studentService = new StudentService();
        AdminService adminService = new AdminService();
        EmployeeService employeeService = new EmployeeService();

        boolean running = true;
        while (running) {
            System.out.println();
            System.out.println("========================================");
            System.out.println("COLLEGE COMPLAINT & SERVICE MANAGEMENT");
            System.out.println("========================================");
            System.out.println("1. Student");
            System.out.println("2. Admin");
            System.out.println("3. Employee");
            System.out.println("4. Exit");
            int choice = InputUtil.readIntInRange("Enter your choice: ", 1, 4);

            switch (choice) {
                case 1:
                    studentService.start();
                    break;
                case 2:
                    adminService.start();
                    break;
                case 3:
                    employeeService.start();
                    break;
                default:
                    System.out.println("Thank you for using the system. Goodbye!");
                    running = false;
                    break;
            }
        }
    }
}
