package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import config.DBConnection;
import model.Admin;

/**
 * All SQL for the "admins" table.
 */
public class AdminDAO {

    /** Returns the admin if e-mail and password hash match, otherwise null. */
    public Admin login(String email, String passwordHash) throws SQLException {
        String sql = "SELECT admin_id, name, email FROM admins WHERE email = ? AND password = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, passwordHash);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Admin(rs.getInt("admin_id"), rs.getString("name"), rs.getString("email"));
                }
            }
        }
        return null;
    }
}
