package service;

import java.sql.SQLException;
import java.util.List;

import dao.ComplaintDAO;
import dao.EmployeeDAO;
import model.Complaint;
import model.ComplaintStatus;
import model.Employee;
import util.DisplayUtil;
import util.InputUtil;
import util.PasswordUtil;

/**
 * Console menus and logic for the EMPLOYEE role.
 */
public class EmployeeService {

    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private final ComplaintDAO complaintDAO = new ComplaintDAO();

    /** Asks for employee login, then shows the employee menu. */
    public void start() {
        Employee employee = login();
        if (employee != null) {
            employeeMenu(employee);
        }
    }

    private Employee login() {
        System.out.println();
        System.out.println("------------- EMPLOYEE LOGIN -------------");
        String email = InputUtil.readRequired("Email: ", 100).toLowerCase();
        String password = InputUtil.readRequired("Password: ", 100);

        try {
            Employee employee = employeeDAO.login(email, PasswordUtil.hash(password));
            if (employee == null) {
                System.out.println("Invalid email or password.");
                return null;
            }
            System.out.println("Login successful. Welcome, " + employee.getName() + "!");
            return employee;
        } catch (SQLException e) {
            System.out.println("Database error while logging in: " + e.getMessage());
            return null;
        }
    }

    private void employeeMenu(Employee employee) {
        boolean logout = false;
        while (!logout) {
            System.out.println();
            System.out.println("========= EMPLOYEE MENU (" + employee.getName() + ") =========");
            System.out.println("1. View Assigned Complaints");
            System.out.println("2. View Complaint Details");
            System.out.println("3. Update Complaint Status");
            System.out.println("4. Add Complaint Update");
            System.out.println("5. View Complaint History");
            System.out.println("6. Logout");
            int choice = InputUtil.readIntInRange("Enter your choice: ", 1, 6);

            switch (choice) {
                case 1:
                    viewAssignedComplaints(employee);
                    break;
                case 2:
                    viewComplaintDetails(employee);
                    break;
                case 3:
                    updateStatus(employee);
                    break;
                case 4:
                    addUpdate(employee);
                    break;
                case 5:
                    viewHistory(employee);
                    break;
                default:
                    System.out.println("You have been logged out.");
                    logout = true;
                    break;
            }
        }
    }

    // ------------------------------------------------------------------
    // 1. Assigned complaints
    // ------------------------------------------------------------------

    private void viewAssignedComplaints(Employee employee) {
        System.out.println();
        System.out.println("------------ COMPLAINTS ASSIGNED TO YOU ------------");
        try {
            List<Complaint> complaints = complaintDAO.getComplaintsByEmployee(employee.getEmployeeId());
            DisplayUtil.printComplaintTable(complaints, true);
        } catch (SQLException e) {
            System.out.println("Database error while loading complaints: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // 2. Complaint details
    // ------------------------------------------------------------------

    private void viewComplaintDetails(Employee employee) {
        int complaintId = InputUtil.readInt("Enter complaint ID: ");
        try {
            Complaint complaint = complaintDAO.getComplaintForEmployee(complaintId, employee.getEmployeeId());
            if (complaint == null) {
                System.out.println("Complaint not found among your assigned complaints.");
                return;
            }
            DisplayUtil.printComplaintDetails(complaint);
        } catch (SQLException e) {
            System.out.println("Database error while loading the complaint: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // 3. Update status (+ comment saved in history)
    // ------------------------------------------------------------------

    private void updateStatus(Employee employee) {
        int complaintId = InputUtil.readInt("Enter complaint ID to update: ");
        try {
            Complaint complaint = complaintDAO.getComplaintForEmployee(complaintId, employee.getEmployeeId());
            if (complaint == null) {
                System.out.println("Complaint not found among your assigned complaints.");
                return;
            }

            String currentStatus = complaint.getStatus();
            System.out.println("Current status: " + currentStatus);
            if (currentStatus.equals(ComplaintStatus.CLOSED.name())) {
                System.out.println("This complaint is CLOSED and can no longer be changed.");
                return;
            }

            System.out.println("Select the new status:");
            System.out.println("1. IN_PROGRESS");
            System.out.println("2. RESOLVED");
            System.out.println("3. CLOSED");
            int choice = InputUtil.readIntInRange("Status number: ", 1, 3);

            String newStatus;
            if (choice == 1) {
                newStatus = ComplaintStatus.IN_PROGRESS.name();
            } else if (choice == 2) {
                newStatus = ComplaintStatus.RESOLVED.name();
            } else {
                newStatus = ComplaintStatus.CLOSED.name();
            }

            if (newStatus.equals(currentStatus)) {
                System.out.println("The complaint already has status " + currentStatus + ". Nothing changed.");
                return;
            }

            String comment = InputUtil.readRequired("Comment about this change: ", 2000);

            boolean updated = complaintDAO.updateStatus(complaintId, employee.getEmployeeId(),
                    currentStatus, newStatus, comment, employee.getName());
            if (updated) {
                System.out.println("Status changed from " + currentStatus + " to " + newStatus + ".");
            } else {
                System.out.println("Could not update the complaint.");
            }
        } catch (SQLException e) {
            System.out.println("Database error while updating the status: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // 4. Add a comment without changing the status
    // ------------------------------------------------------------------

    private void addUpdate(Employee employee) {
        int complaintId = InputUtil.readInt("Enter complaint ID: ");
        try {
            Complaint complaint = complaintDAO.getComplaintForEmployee(complaintId, employee.getEmployeeId());
            if (complaint == null) {
                System.out.println("Complaint not found among your assigned complaints.");
                return;
            }

            String comment = InputUtil.readRequired("Enter your update/comment: ", 2000);
            boolean added = complaintDAO.addComment(complaintId, employee.getEmployeeId(),
                    complaint.getStatus(), comment, employee.getName());
            if (added) {
                System.out.println("Update added to complaint " + complaintId + ".");
            } else {
                System.out.println("Could not add the update.");
            }
        } catch (SQLException e) {
            System.out.println("Database error while adding the update: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // 5. History
    // ------------------------------------------------------------------

    private void viewHistory(Employee employee) {
        int complaintId = InputUtil.readInt("Enter complaint ID: ");
        try {
            Complaint complaint = complaintDAO.getComplaintForEmployee(complaintId, employee.getEmployeeId());
            if (complaint == null) {
                System.out.println("Complaint not found among your assigned complaints.");
                return;
            }
            System.out.println("Complaint " + complaintId + ": " + complaint.getTitle()
                    + " (current status: " + complaint.getStatus() + ")");
            DisplayUtil.printHistory(complaintDAO.getHistory(complaintId));
        } catch (SQLException e) {
            System.out.println("Database error while loading the history: " + e.getMessage());
        }
    }
}
