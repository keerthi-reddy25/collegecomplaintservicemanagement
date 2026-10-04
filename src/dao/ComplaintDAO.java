package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import config.DBConnection;
import model.Complaint;
import model.ComplaintStatus;
import model.ComplaintUpdate;

/**
 * All SQL for the "complaints" and "complaint_updates" tables.
 *
 * Methods that change two tables at once (add complaint, assign, update status)
 * use a TRANSACTION: either both changes are saved (commit) or none (rollback).
 */
public class ComplaintDAO {

    // Common SELECT used by every "list complaints" query (JOIN gives us the names).
    private static final String BASE_SELECT =
            "SELECT c.complaint_id, c.student_id, c.category_id, c.employee_id, c.title, c.description, "
            + "c.status, c.created_at, c.updated_at, "
            + "s.name AS student_name, cat.category_name, e.name AS employee_name "
            + "FROM complaints c "
            + "JOIN students s ON c.student_id = s.student_id "
            + "JOIN categories cat ON c.category_id = cat.category_id "
            + "LEFT JOIN employees e ON c.employee_id = e.employee_id ";

    private static final String ORDER_NEWEST_FIRST = " ORDER BY c.created_at DESC, c.complaint_id DESC";

    // ------------------------------------------------------------------
    // INSERT
    // ------------------------------------------------------------------

    /**
     * Saves a new OPEN complaint and its first history entry in one transaction.
     *
     * @return the generated complaint_id
     */
    public int addComplaint(Complaint complaint, String studentName) throws SQLException {
        String insertComplaint = "INSERT INTO complaints (student_id, category_id, title, description, status) "
                + "VALUES (?, ?, ?, ?, 'OPEN')";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int newId;
                try (PreparedStatement ps = conn.prepareStatement(insertComplaint, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, complaint.getStudentId());
                    ps.setInt(2, complaint.getCategoryId());
                    ps.setString(3, complaint.getTitle());
                    ps.setString(4, complaint.getDescription());
                    ps.executeUpdate();

                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("Complaint was saved but no ID was returned.");
                        }
                        newId = keys.getInt(1);
                    }
                }

                insertHistory(conn, newId, "STUDENT", studentName, null,
                        ComplaintStatus.OPEN.name(), "Complaint raised by student.");

                conn.commit();
                return newId;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    // ------------------------------------------------------------------
    // SELECT (lists and single complaints)
    // ------------------------------------------------------------------

    /** All complaints of one student. */
    public List<Complaint> getComplaintsByStudent(int studentId) throws SQLException {
        return queryComplaints(BASE_SELECT + "WHERE c.student_id = ?" + ORDER_NEWEST_FIRST, studentId);
    }

    /** All complaints in the system (admin). */
    public List<Complaint> getAllComplaints() throws SQLException {
        return queryComplaints(BASE_SELECT + ORDER_NEWEST_FIRST);
    }

    /** Complaints with one status (admin filter). */
    public List<Complaint> getComplaintsByStatus(String status) throws SQLException {
        return queryComplaints(BASE_SELECT + "WHERE c.status = ?" + ORDER_NEWEST_FIRST, status);
    }

    /** Complaints of one category (admin filter). */
    public List<Complaint> getComplaintsByCategory(int categoryId) throws SQLException {
        return queryComplaints(BASE_SELECT + "WHERE c.category_id = ?" + ORDER_NEWEST_FIRST, categoryId);
    }

    /** Complaints assigned to one employee. */
    public List<Complaint> getComplaintsByEmployee(int employeeId) throws SQLException {
        return queryComplaints(BASE_SELECT + "WHERE c.employee_id = ?" + ORDER_NEWEST_FIRST, employeeId);
    }

    /** One complaint by ID (admin may see any complaint). Returns null if not found. */
    public Complaint getComplaintById(int complaintId) throws SQLException {
        List<Complaint> list = queryComplaints(BASE_SELECT + "WHERE c.complaint_id = ?", complaintId);
        return list.isEmpty() ? null : list.get(0);
    }

    /** One complaint, but only if it belongs to the given student. Returns null otherwise. */
    public Complaint getComplaintForStudent(int complaintId, int studentId) throws SQLException {
        List<Complaint> list = queryComplaints(
                BASE_SELECT + "WHERE c.complaint_id = ? AND c.student_id = ?", complaintId, studentId);
        return list.isEmpty() ? null : list.get(0);
    }

    /** One complaint, but only if it is assigned to the given employee. Returns null otherwise. */
    public Complaint getComplaintForEmployee(int complaintId, int employeeId) throws SQLException {
        List<Complaint> list = queryComplaints(
                BASE_SELECT + "WHERE c.complaint_id = ? AND c.employee_id = ?", complaintId, employeeId);
        return list.isEmpty() ? null : list.get(0);
    }

    // ------------------------------------------------------------------
    // UPDATE (with history, inside transactions)
    // ------------------------------------------------------------------

    /**
     * Admin assigns a complaint to an employee: sets employee_id, changes the status to
     * IN_PROGRESS and writes a history entry.
     * Only OPEN or IN_PROGRESS complaints can be (re)assigned.
     *
     * @return true if the complaint was assigned, false if it was not allowed
     */
    public boolean assignComplaint(int complaintId, int employeeId, String employeeName,
                                   String oldStatus, String adminName) throws SQLException {
        String update = "UPDATE complaints SET employee_id = ?, status = 'IN_PROGRESS' "
                + "WHERE complaint_id = ? AND status IN ('OPEN', 'IN_PROGRESS')";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int rows;
                try (PreparedStatement ps = conn.prepareStatement(update)) {
                    ps.setInt(1, employeeId);
                    ps.setInt(2, complaintId);
                    rows = ps.executeUpdate();
                }

                if (rows == 0) {
                    conn.rollback();
                    return false;
                }

                insertHistory(conn, complaintId, "ADMIN", adminName, oldStatus,
                        ComplaintStatus.IN_PROGRESS.name(), "Assigned to " + employeeName + " by admin.");

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /**
     * Employee changes the status of one of THEIR complaints and writes a history entry.
     *
     * @return true if the status was changed, false if the complaint is not assigned to this employee
     */
    public boolean updateStatus(int complaintId, int employeeId, String oldStatus, String newStatus,
                                String comment, String employeeName) throws SQLException {
        String update = "UPDATE complaints SET status = ? WHERE complaint_id = ? AND employee_id = ?";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int rows;
                try (PreparedStatement ps = conn.prepareStatement(update)) {
                    ps.setString(1, newStatus);
                    ps.setInt(2, complaintId);
                    ps.setInt(3, employeeId);
                    rows = ps.executeUpdate();
                }

                if (rows == 0) {
                    conn.rollback();
                    return false;
                }

                insertHistory(conn, complaintId, "EMPLOYEE", employeeName, oldStatus, newStatus, comment);

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /**
     * Employee adds a comment without changing the status.
     * Also touches complaints.updated_at so the "last updated" time is correct.
     */
    public boolean addComment(int complaintId, int employeeId, String currentStatus,
                              String comment, String employeeName) throws SQLException {
        String touch = "UPDATE complaints SET updated_at = CURRENT_TIMESTAMP "
                + "WHERE complaint_id = ? AND employee_id = ?";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int rows;
                try (PreparedStatement ps = conn.prepareStatement(touch)) {
                    ps.setInt(1, complaintId);
                    ps.setInt(2, employeeId);
                    rows = ps.executeUpdate();
                }

                if (rows == 0) {
                    conn.rollback();
                    return false;
                }

                insertHistory(conn, complaintId, "EMPLOYEE", employeeName, currentStatus, currentStatus, comment);

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    // ------------------------------------------------------------------
    // HISTORY
    // ------------------------------------------------------------------

    /** All history entries of a complaint, oldest first. */
    public List<ComplaintUpdate> getHistory(int complaintId) throws SQLException {
        String sql = "SELECT update_id, complaint_id, updated_by_role, updated_by_name, "
                + "old_status, new_status, update_comment, created_at "
                + "FROM complaint_updates WHERE complaint_id = ? ORDER BY created_at ASC, update_id ASC";

        List<ComplaintUpdate> updates = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, complaintId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    updates.add(new ComplaintUpdate(
                            rs.getInt("update_id"),
                            rs.getInt("complaint_id"),
                            rs.getString("updated_by_role"),
                            rs.getString("updated_by_name"),
                            rs.getString("old_status"),
                            rs.getString("new_status"),
                            rs.getString("update_comment"),
                            toLocalDateTime(rs.getTimestamp("created_at"))));
                }
            }
        }
        return updates;
    }

    // ------------------------------------------------------------------
    // STATISTICS (COUNT, GROUP BY, JOIN, HAVING)
    // ------------------------------------------------------------------

    /** Total number of complaints. */
    public int countAll() throws SQLException {
        return countSingleValue("SELECT COUNT(*) FROM complaints");
    }

    /** Complaints that have no employee yet. */
    public int countUnassigned() throws SQLException {
        return countSingleValue("SELECT COUNT(*) FROM complaints WHERE employee_id IS NULL");
    }

    /** Number of complaints per status. Statuses with no complaints are shown as 0. */
    public Map<String, Integer> countByStatus() throws SQLException {
        Map<String, Integer> result = new LinkedHashMap<>();
        for (ComplaintStatus status : ComplaintStatus.values()) {
            result.put(status.name(), 0);
        }

        String sql = "SELECT status, COUNT(*) AS total FROM complaints GROUP BY status";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                result.put(rs.getString("status"), rs.getInt("total"));
            }
        }
        return result;
    }

    /** Number of complaints per category (LEFT JOIN so empty categories show 0). */
    public Map<String, Integer> countByCategory() throws SQLException {
        String sql = "SELECT cat.category_name, COUNT(c.complaint_id) AS total "
                + "FROM categories cat "
                + "LEFT JOIN complaints c ON cat.category_id = c.category_id "
                + "GROUP BY cat.category_id, cat.category_name "
                + "ORDER BY total DESC, cat.category_name";

        Map<String, Integer> result = new LinkedHashMap<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                result.put(rs.getString("category_name"), rs.getInt("total"));
            }
        }
        return result;
    }

    /** Number of complaints per employee, only employees that have at least one (HAVING). */
    public Map<String, Integer> countByEmployee() throws SQLException {
        String sql = "SELECT e.name, COUNT(c.complaint_id) AS total "
                + "FROM employees e "
                + "JOIN complaints c ON e.employee_id = c.employee_id "
                + "GROUP BY e.employee_id, e.name "
                + "HAVING COUNT(c.complaint_id) > 0 "
                + "ORDER BY total DESC, e.name";

        Map<String, Integer> result = new LinkedHashMap<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                result.put(rs.getString("name"), rs.getInt("total"));
            }
        }
        return result;
    }

    // ------------------------------------------------------------------
    // private helpers
    // ------------------------------------------------------------------

    /** Runs one of the BASE_SELECT queries. Values are bound with ? placeholders (no concatenation). */
    private List<Complaint> queryComplaints(String sql, Object... params) throws SQLException {
        List<Complaint> complaints = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    complaints.add(mapComplaint(rs));
                }
            }
        }
        return complaints;
    }

    private Complaint mapComplaint(ResultSet rs) throws SQLException {
        Complaint c = new Complaint();
        c.setComplaintId(rs.getInt("complaint_id"));
        c.setStudentId(rs.getInt("student_id"));
        c.setCategoryId(rs.getInt("category_id"));

        int employeeId = rs.getInt("employee_id");
        c.setEmployeeId(rs.wasNull() ? null : employeeId); // employee_id can be NULL

        c.setTitle(rs.getString("title"));
        c.setDescription(rs.getString("description"));
        c.setStatus(rs.getString("status"));
        c.setCreatedAt(toLocalDateTime(rs.getTimestamp("created_at")));
        c.setUpdatedAt(toLocalDateTime(rs.getTimestamp("updated_at")));
        c.setStudentName(rs.getString("student_name"));
        c.setCategoryName(rs.getString("category_name"));
        c.setEmployeeName(rs.getString("employee_name")); // null when not assigned
        return c;
    }

    /** INSERT into complaint_updates using the SAME connection (so it is part of the transaction). */
    private void insertHistory(Connection conn, int complaintId, String role, String name,
                               String oldStatus, String newStatus, String comment) throws SQLException {
        String sql = "INSERT INTO complaint_updates "
                + "(complaint_id, updated_by_role, updated_by_name, old_status, new_status, update_comment) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, complaintId);
            ps.setString(2, role);
            ps.setString(3, name);
            ps.setString(4, oldStatus); // null is stored as SQL NULL
            ps.setString(5, newStatus);
            ps.setString(6, comment);
            ps.executeUpdate();
        }
    }

    private int countSingleValue(String sql) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
