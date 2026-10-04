package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import config.DBConnection;
import model.Employee;

/**
 * All SQL for the "employees" table.
 */
public class EmployeeDAO {

    /** Returns the employee if e-mail and password hash match, otherwise null. */
    public Employee login(String email, String passwordHash) throws SQLException {
        String sql = "SELECT employee_id, name, email, department, phone "
                + "FROM employees WHERE email = ? AND password = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, passwordHash);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Employee(rs.getInt("employee_id"), rs.getString("name"),
                            rs.getString("email"), rs.getString("department"), rs.getString("phone"));
                }
            }
        }
        return null;
    }

    /** Returns the employee with the given ID, or null if there is none. */
    public Employee getEmployeeById(int employeeId) throws SQLException {
        String sql = "SELECT employee_id, name, email, department, phone "
                + "FROM employees WHERE employee_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Employee(rs.getInt("employee_id"), rs.getString("name"),
                            rs.getString("email"), rs.getString("department"), rs.getString("phone"));
                }
            }
        }
        return null;
    }

    /**
     * LEFT JOIN + GROUP BY + COUNT: lists every employee together with the number of
     * complaints assigned to them (employees with zero complaints are included).
     */
    public List<Employee> getAllEmployees() throws SQLException {
        String sql = "SELECT e.employee_id, e.name, e.email, e.department, e.phone, "
                + "COUNT(c.complaint_id) AS assigned_total, "
                + "COALESCE(SUM(CASE WHEN c.status = 'IN_PROGRESS' THEN 1 ELSE 0 END), 0) AS active_total "
                + "FROM employees e "
                + "LEFT JOIN complaints c ON e.employee_id = c.employee_id "
                + "GROUP BY e.employee_id, e.name, e.email, e.department, e.phone "
                + "ORDER BY e.employee_id";

        List<Employee> employees = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Employee e = new Employee(rs.getInt("employee_id"), rs.getString("name"),
                        rs.getString("email"), rs.getString("department"), rs.getString("phone"));
                e.setAssignedCount(rs.getInt("assigned_total"));
                e.setActiveCount(rs.getInt("active_total"));
                employees.add(e);
            }
        }
        return employees;
    }
}
