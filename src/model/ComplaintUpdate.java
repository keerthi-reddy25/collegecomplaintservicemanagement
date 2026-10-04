package model;

import java.time.LocalDateTime;

/**
 * Represents one row of the "complaint_updates" table
 * (one entry in the history of a complaint).
 */
public class ComplaintUpdate {

    private int updateId;
    private int complaintId;
    private String updatedByRole; // STUDENT, ADMIN or EMPLOYEE
    private String updatedByName;
    private String oldStatus;     // null for the very first entry
    private String newStatus;
    private String comment;
    private LocalDateTime createdAt;

    public ComplaintUpdate() {
    }

    public ComplaintUpdate(int updateId, int complaintId, String updatedByRole, String updatedByName,
                           String oldStatus, String newStatus, String comment, LocalDateTime createdAt) {
        this.updateId = updateId;
        this.complaintId = complaintId;
        this.updatedByRole = updatedByRole;
        this.updatedByName = updatedByName;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public int getUpdateId() {
        return updateId;
    }

    public void setUpdateId(int updateId) {
        this.updateId = updateId;
    }

    public int getComplaintId() {
        return complaintId;
    }

    public void setComplaintId(int complaintId) {
        this.complaintId = complaintId;
    }

    public String getUpdatedByRole() {
        return updatedByRole;
    }

    public void setUpdatedByRole(String updatedByRole) {
        this.updatedByRole = updatedByRole;
    }

    public String getUpdatedByName() {
        return updatedByName;
    }

    public void setUpdatedByName(String updatedByName) {
        this.updatedByName = updatedByName;
    }

    public String getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(String oldStatus) {
        this.oldStatus = oldStatus;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
