package service;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import dao.AdminDAO;
import dao.CategoryDAO;
import dao.ComplaintDAO;
import dao.EmployeeDAO;
import dao.StudentDAO;
import model.Admin;
import model.Category;
import model.Complaint;
import model.ComplaintStatus;
import model.Employee;
import model.Student;
import util.DisplayUtil;
import util.InputUtil;
import util.PasswordUtil;

/**
 * Console menus and logic for the ADMIN role.
 */
public class AdminService {

    private final AdminDAO adminDAO = new AdminDAO();
    private final ComplaintDAO complaintDAO = new ComplaintDAO();
    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    /** Asks for admin login, then shows the admin menu. */
    public void start() {
        Admin admin = login();
        if (admin != null) {
            adminMenu(admin);
        }
    }

    private Admin login() {
        System.out.println();
        System.out.println("-------------- ADMIN LOGIN --------------");
        String email = InputUtil.readRequired("Email: ", 100).toLowerCase();
        String password = InputUtil.readRequired("Password: ", 100);

        try {
            Admin admin = adminDAO.login(email, PasswordUtil.hash(password));
            if (admin == null) {
                System.out.println("Invalid email or password.");
                return null;
            }
            System.out.println("Login successful. Welcome, " + admin.getName() + "!");
            return admin;
        } catch (SQLException e) {
            System.out.println("Database error while logging in: " + e.getMessage());
            return null;
        }
    }

    private void adminMenu(Admin admin) {
        boolean logout = false;
        while (!logout) {
            System.out.println();
            System.out.println("============== ADMIN MENU ==============");
            System.out.println("1. View All Complaints");
            System.out.println("2. Search Complaint");
            System.out.println("3. Filter Complaints");
            System.out.println("4. Assign Complaint");
            System.out.println("5. View Employees");
            System.out.println("6. Complaint Statistics");
            System.out.println("7. Logout");
            int choice = InputUtil.readIntInRange("Enter your choice: ", 1, 7);

            switch (choice) {
                case 1:
                    viewAllComplaints();
                    break;
                case 2:
                    searchComplaint();
                    break;
                case 3:
                    filterComplaints();
                    break;
                case 4:
                    assignComplaint(admin);
                    break;
                case 5:
                    viewEmployees();
                    break;
                case 6:
                    showStatistics();
                    break;
                default:
                    System.out.println("You have been logged out.");
                    logout = true;
                    break;
            }
        }
    }

    // ------------------------------------------------------------------
    // 1. View all complaints
    // ------------------------------------------------------------------

    private void viewAllComplaints() {
        System.out.println();
        System.out.println("---------------------------------- ALL COMPLAINTS ----------------------------------");
        try {
            DisplayUtil.printComplaintTable(complaintDAO.getAllComplaints(), true);
        } catch (SQLException e) {
            System.out.println("Database error while loading complaints: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // 2. Search complaint by ID (+ optional student details)
    // ------------------------------------------------------------------

    private void searchComplaint() {
        int complaintId = InputUtil.readInt("Enter complaint ID to search: ");
        try {
            Complaint complaint = complaintDAO.getComplaintById(complaintId);
            if (complaint == null) {
                System.out.println("No complaint found with ID " + complaintId + ".");
                return;
            }
            DisplayUtil.printComplaintDetails(complaint);
            DisplayUtil.printHistory(complaintDAO.getHistory(complaintId));

            if (InputUtil.readYesNo("View details of the student who raised this complaint?")) {
                Student student = studentDAO.getStudentById(complaint.getStudentId());
                if (student == null) {
                    System.out.println("Student record not found.");
                } else {
                    DisplayUtil.printStudentDetails(student);
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error while searching: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // 3. Filter by status or category
    // ------------------------------------------------------------------

    private void filterComplaints() {
        System.out.println();
        System.out.println("----------- FILTER COMPLAINTS -----------");
        System.out.println("1. Filter by Status");
        System.out.println("2. Filter by Category");
        System.out.println("3. Back");
        int choice = InputUtil.readIntInRange("Enter your choice: ", 1, 3);

        try {
            if (choice == 1) {
                ComplaintStatus[] statuses = ComplaintStatus.values();
                System.out.println("Select a status:");
                for (int i = 0; i < statuses.length; i++) {
                    System.out.println((i + 1) + ". " + statuses[i].name());
                }
                int statusChoice = InputUtil.readIntInRange("Status number: ", 1, statuses.length);
                String status = statuses[statusChoice - 1].name();

                System.out.println("Complaints with status " + status + ":");
                DisplayUtil.printComplaintTable(complaintDAO.getComplaintsByStatus(status), true);
            } else if (choice == 2) {
                List<Category> categories = categoryDAO.getAllCategories();
                if (categories.isEmpty()) {
                    System.out.println("No categories found.");
                    return;
                }
                System.out.println("Select a category:");
                for (int i = 0; i < categories.size(); i++) {
                    System.out.println((i + 1) + ". " + categories.get(i).getCategoryName());
                }
                int categoryChoice = InputUtil.readIntInRange("Category number: ", 1, categories.size());
                Category category = categories.get(categoryChoice - 1);

                System.out.println("Complaints in category " + category.getCategoryName() + ":");
                DisplayUtil.printComplaintTable(complaintDAO.getComplaintsByCategory(category.getCategoryId()), true);
            }
        } catch (SQLException e) {
            System.out.println("Database error while filtering: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // 4. Assign complaint to an employee
    // ------------------------------------------------------------------

    private void assignComplaint(Admin admin) {
        int complaintId = InputUtil.readInt("Enter complaint ID to assign: ");
        try {
            Complaint complaint = complaintDAO.getComplaintById(complaintId);
            if (complaint == null) {
                System.out.println("No complaint found with ID " + complaintId + ".");
                return;
            }
            DisplayUtil.printComplaintDetails(complaint);

            String status = complaint.getStatus();
            if (status.equals(ComplaintStatus.RESOLVED.name()) || status.equals(ComplaintStatus.CLOSED.name())) {
                System.out.println("This complaint is already " + status + " and cannot be assigned.");
                return;
            }

            List<Employee> employees = employeeDAO.getAllEmployees();
            DisplayUtil.printEmployeeTable(employees);
            if (employees.isEmpty()) {
                return;
            }

            int employeeId = InputUtil.readInt("Enter employee ID to assign to: ");
            Employee employee = employeeDAO.getEmployeeById(employeeId);
            if (employee == null) {
                System.out.println("No employee found with ID " + employeeId + ".");
                return;
            }

            boolean assigned = complaintDAO.assignComplaint(complaintId, employee.getEmployeeId(),
                    employee.getName(), status, admin.getName());
            if (assigned) {
                System.out.println("Complaint " + complaintId + " assigned to " + employee.getName()
                        + ". Status is now IN_PROGRESS.");
            } else {
                System.out.println("Could not assign the complaint. Its status may have changed. Please try again.");
            }
        } catch (SQLException e) {
            System.out.println("Database error while assigning: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // 5. View employees
    // ------------------------------------------------------------------

    private void viewEmployees() {
        System.out.println();
        System.out.println("------------------------------------ EMPLOYEES ------------------------------------");
        try {
            DisplayUtil.printEmployeeTable(employeeDAO.getAllEmployees());
        } catch (SQLException e) {
            System.out.println("Database error while loading employees: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // 6. Statistics
    // ------------------------------------------------------------------

    private void showStatistics() {
        try {
            int total = complaintDAO.countAll();
            int unassigned = complaintDAO.countUnassigned();
            Map<String, Integer> byStatus = complaintDAO.countByStatus();
            Map<String, Integer> byCategory = complaintDAO.countByCategory();
            Map<String, Integer> byEmployee = complaintDAO.countByEmployee();

            System.out.println();
            System.out.println("========== COMPLAINT STATISTICS ==========");
            System.out.println("Total complaints       : " + total);
            System.out.println("Open complaints        : " + byStatus.get(ComplaintStatus.OPEN.name()));
            System.out.println("In-progress complaints : " + byStatus.get(ComplaintStatus.IN_PROGRESS.name()));
            System.out.println("Resolved complaints    : " + byStatus.get(ComplaintStatus.RESOLVED.name()));
            System.out.println("Closed complaints      : " + byStatus.get(ComplaintStatus.CLOSED.name()));
            System.out.println("Not assigned to anyone : " + unassigned);

            System.out.println();
            System.out.println("Complaints by category:");
            for (Map.Entry<String, Integer> entry : byCategory.entrySet()) {
                System.out.printf("  %-24s : %d%n", entry.getKey(), entry.getValue());
            }

            System.out.println();
            System.out.println("Complaints by employee:");
            if (byEmployee.isEmpty()) {
                System.out.println("  No complaints have been assigned yet.");
            } else {
                for (Map.Entry<String, Integer> entry : byEmployee.entrySet()) {
                    System.out.printf("  %-24s : %d%n", entry.getKey(), entry.getValue());
                }
            }
            System.out.println("==========================================");
        } catch (SQLException e) {
            System.out.println("Database error while loading statistics: " + e.getMessage());
        }
    }
}
