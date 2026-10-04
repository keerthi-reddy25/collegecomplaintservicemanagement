package model;

/**
 * Represents one row of the "admins" table.
 * The password is never kept in this object after login.
 */
public class Admin {

    private int adminId;
    private String name;
    private String email;

    public Admin() {
    }

    public Admin(int adminId, String name, String email) {
        this.adminId = adminId;
        this.name = name;
        this.email = email;
    }

    public int getAdminId() {
        return adminId;
    }

    public void setAdminId(int adminId) {
        this.adminId = adminId;
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
}
