package model;

/**
 * Represents one row of the "employees" table.
 * assignedCount and activeCount are filled only by the "view employees" query.
 */
public class Employee {

    private int employeeId;
    private String name;
    private String email;
    private String department;
    private String phone;
    private int assignedCount; // total complaints ever assigned
    private int activeCount;   // complaints currently IN_PROGRESS

    public Employee() {
    }

    public Employee(int employeeId, String name, String email, String department, String phone) {
        this.employeeId = employeeId;
        this.name = name;
        this.email = email;
        this.department = department;
        this.phone = phone;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getAssignedCount() {
        return assignedCount;
    }

    public void setAssignedCount(int assignedCount) {
        this.assignedCount = assignedCount;
    }

    public int getActiveCount() {
        return activeCount;
    }

    public void setActiveCount(int activeCount) {
        this.activeCount = activeCount;
    }
}
