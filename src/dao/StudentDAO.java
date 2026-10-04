package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import config.DBConnection;
import model.Student;

/**
 * All SQL for the "students" table.
 */
public class StudentDAO {

    /** INSERT: saves a new student and returns the generated student_id. */
    public int addStudent(Student student) throws SQLException {
        String sql = "INSERT INTO students (name, email, password, phone, department, roll_number) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, student.getName());
            ps.setString(2, student.getEmail());
            ps.setString(3, student.getPassword());
            ps.setString(4, student.getPhone());
            ps.setString(5, student.getDepartment());
            ps.setString(6, student.getRollNumber());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
            throw new SQLException("Student was saved but no ID was returned.");
        }
    }

    /** SELECT with WHERE: checks whether the e-mail is already registered. */
    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM students WHERE email = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /** SELECT with WHERE: checks whether the roll number is already registered. */
    public boolean rollNumberExists(String rollNumber) throws SQLException {
        String sql = "SELECT COUNT(*) FROM students WHERE roll_number = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, rollNumber);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /** Returns the student if e-mail and password hash match, otherwise null. */
    public Student login(String email, String passwordHash) throws SQLException {
        String sql = "SELECT student_id, name, email, password, phone, department, roll_number "
                + "FROM students WHERE email = ? AND password = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, passwordHash);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapStudent(rs);
                }
            }
        }
        return null;
    }

    /** Returns the student with the given ID, or null if there is none. */
    public Student getStudentById(int studentId) throws SQLException {
        String sql = "SELECT student_id, name, email, password, phone, department, roll_number "
                + "FROM students WHERE student_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapStudent(rs);
                }
            }
        }
        return null;
    }

    private Student mapStudent(ResultSet rs) throws SQLException {
        return new Student(
                rs.getInt("student_id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("password"),
                rs.getString("phone"),
                rs.getString("department"),
                rs.getString("roll_number"));
    }
}
