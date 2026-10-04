package model;

/**
 * The four possible statuses of a complaint.
 * The names match the ENUM values in the "complaints.status" column.
 */
public enum ComplaintStatus {
    OPEN,
    IN_PROGRESS,
    RESOLVED,
    CLOSED
}
