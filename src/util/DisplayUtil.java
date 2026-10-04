package util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import model.Complaint;
import model.ComplaintUpdate;
import model.Employee;
import model.Student;

/**
 * Prints complaints, history, employees and students in a readable format.
 */
public class DisplayUtil {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");

    private DisplayUtil() {
    }

    public static String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "-";
        }
        return dateTime.format(DATE_FORMAT);
    }

    private static String truncate(String text, int maxLength) {
        if (text == null) {
            return "-";
        }
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }

    /** Prints complaints as a table. The Student column is shown only if showStudent is true. */
    public static void printComplaintTable(List<Complaint> complaints, boolean showStudent) {
        if (complaints.isEmpty()) {
            System.out.println("No complaints found.");
            return;
        }

        String line = "-".repeat(showStudent ? 135 : 115);
        System.out.println(line);
        if (showStudent) {
            System.out.printf("%-5s | %-30s | %-22s | %-12s | %-16s | %-16s | %-17s%n",
                    "ID", "Title", "Category", "Status", "Student", "Assigned To", "Created");
        } else {
            System.out.printf("%-5s | %-30s | %-22s | %-12s | %-16s | %-17s%n",
                    "ID", "Title", "Category", "Status", "Assigned To", "Created");
        }
        System.out.println(line);

        for (Complaint c : complaints) {
            String assigned = c.getEmployeeName() == null ? "Not assigned" : c.getEmployeeName();
            if (showStudent) {
                System.out.printf("%-5d | %-30s | %-22s | %-12s | %-16s | %-16s | %-17s%n",
                        c.getComplaintId(), truncate(c.getTitle(), 30), truncate(c.getCategoryName(), 22),
                        c.getStatus(), truncate(c.getStudentName(), 16), truncate(assigned, 16),
                        formatDate(c.getCreatedAt()));
            } else {
                System.out.printf("%-5d | %-30s | %-22s | %-12s | %-16s | %-17s%n",
                        c.getComplaintId(), truncate(c.getTitle(), 30), truncate(c.getCategoryName(), 22),
                        c.getStatus(), truncate(assigned, 16), formatDate(c.getCreatedAt()));
            }
        }
        System.out.println(line);
        System.out.println("Total: " + complaints.size() + " complaint(s)");
    }

    public static void printComplaintDetails(Complaint c) {
        System.out.println();
        System.out.println("============ COMPLAINT DETAILS ============");
        System.out.println("Complaint ID : " + c.getComplaintId());
        System.out.println("Title        : " + c.getTitle());
        System.out.println("Category     : " + c.getCategoryName());
        System.out.println("Status       : " + c.getStatus());
        System.out.println("Raised By    : " + c.getStudentName());
        System.out.println("Assigned To  : " + (c.getEmployeeName() == null ? "Not assigned yet" : c.getEmployeeName()));
        System.out.println("Created      : " + formatDate(c.getCreatedAt()));
        System.out.println("Last Updated : " + formatDate(c.getUpdatedAt()));
        System.out.println("Description  : " + c.getDescription());
        System.out.println("===========================================");
    }

    public static void printHistory(List<ComplaintUpdate> updates) {
        System.out.println();
        System.out.println("------------- COMPLAINT HISTORY -------------");
        if (updates.isEmpty()) {
            System.out.println("No history available.");
            return;
        }
        int number = 1;
        for (ComplaintUpdate u : updates) {
            String statusText;
            if (u.getOldStatus() == null) {
                statusText = u.getNewStatus();
            } else if (u.getOldStatus().equals(u.getNewStatus())) {
                statusText = u.getNewStatus() + " (no status change)";
            } else {
                statusText = u.getOldStatus() + " -> " + u.getNewStatus();
            }
            System.out.println(number + ". [" + formatDate(u.getCreatedAt()) + "] "
                    + u.getUpdatedByRole() + " - " + u.getUpdatedByName());
            System.out.println("   Status : " + statusText);
            System.out.println("   Note   : " + u.getComment());
            number++;
        }
        System.out.println("---------------------------------------------");
    }

    public static void printEmployeeTable(List<Employee> employees) {
        if (employees.isEmpty()) {
            System.out.println("No employees found.");
            return;
        }
        String line = "-".repeat(112);
        System.out.println(line);
        System.out.printf("%-5s | %-18s | %-26s | %-14s | %-11s | %-8s | %-6s%n",
                "ID", "Name", "Email", "Department", "Phone", "Assigned", "Active");
        System.out.println(line);
        for (Employee e : employees) {
            System.out.printf("%-5d | %-18s | %-26s | %-14s | %-11s | %-8d | %-6d%n",
                    e.getEmployeeId(), truncate(e.getName(), 18), truncate(e.getEmail(), 26),
                    truncate(e.getDepartment(), 14), e.getPhone(), e.getAssignedCount(), e.getActiveCount());
        }
        System.out.println(line);
    }

    public static void printStudentDetails(Student s) {
        System.out.println();
        System.out.println("============= STUDENT DETAILS =============");
        System.out.println("Student ID  : " + s.getStudentId());
        System.out.println("Name        : " + s.getName());
        System.out.println("Roll Number : " + s.getRollNumber());
        System.out.println("Email       : " + s.getEmail());
        System.out.println("Phone       : " + s.getPhone());
        System.out.println("Department  : " + s.getDepartment());
        System.out.println("===========================================");
    }
}
