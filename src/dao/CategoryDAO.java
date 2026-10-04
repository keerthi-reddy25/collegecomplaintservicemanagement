package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import config.DBConnection;
import model.Category;

/**
 * All SQL for the "categories" table.
 */
public class CategoryDAO {

    /** SELECT ... ORDER BY: returns all categories in ID order. */
    public List<Category> getAllCategories() throws SQLException {
        String sql = "SELECT category_id, category_name, description FROM categories ORDER BY category_id";

        List<Category> categories = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                categories.add(new Category(
                        rs.getInt("category_id"),
                        rs.getString("category_name"),
                        rs.getString("description")));
            }
        }
        return categories;
    }
}
