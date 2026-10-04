package app;

import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;

import dao.AdminDAO;
import dao.CategoryDAO;
import dao.ComplaintDAO;
import dao.EmployeeDAO;
import dao.StudentDAO;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Admin;
import model.Category;
import model.Complaint;
import model.ComplaintStatus;
import model.ComplaintUpdate;
import model.Employee;
import model.Student;
import util.PasswordUtil;

/**
 * JavaFX desktop interface for the existing MySQL-backed complaint workflows.
 */
public class DesktopApp extends Application {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10}$");

    private final StudentDAO studentDAO = new StudentDAO();
    private final AdminDAO adminDAO = new AdminDAO();
    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final ComplaintDAO complaintDAO = new ComplaintDAO();

    private Stage stage;
    private Student student;
    private Admin admin;
    private Employee employee;
    private TableView<Complaint> complaintTable;
    private Label dashboardMessage;

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        stage.setTitle("College Complaint & Service Management");
        showLogin();
        stage.show();
    }

    private void showLogin() {
        Label brand = new Label("CAMPUS CARE");
        brand.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2563eb;");
        Label heading = new Label("College Complaint\n& Service Management");
        heading.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: #172554;");
        Label subtitle = new Label("Sign in to raise, manage, and track campus service requests.");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b;");

        ComboBox<String> role = new ComboBox<>();
        role.getItems().addAll("Student", "Admin", "Employee");
        role.setValue("Student");
        role.setMaxWidth(Double.MAX_VALUE);

        TextField email = new TextField();
        email.setPromptText("Email address");
        PasswordField password = new PasswordField();
        password.setPromptText("Password");
        Label hint = new Label();
        hint.setStyle("-fx-text-fill: #b91c1c;");

        Button login = primaryButton("Sign in");
        login.setMaxWidth(Double.MAX_VALUE);
        login.setOnAction(event -> {
            String selectedRole = role.getValue();
            String enteredEmail = email.getText().trim().toLowerCase();
            String enteredPassword = password.getText();
            if (!EMAIL_PATTERN.matcher(enteredEmail).matches() || enteredPassword.isEmpty()) {
                hint.setText("Enter a valid email address and password.");
                return;
            }
            hint.setText("");
            login.setDisable(true);
            withDatabase("Signing in...", () -> {
                String hash = PasswordUtil.hash(enteredPassword);
                switch (selectedRole) {
                    case "Admin":
                        return adminDAO.login(enteredEmail, hash);
                    case "Employee":
                        return employeeDAO.login(enteredEmail, hash);
                    default:
                        return studentDAO.login(enteredEmail, hash);
                }
            }, result -> {
                login.setDisable(false);
                if (result == null) {
                    hint.setText("Email or password is incorrect.");
                    return;
                }
                student = null;
                admin = null;
                employee = null;
                if (result instanceof Student loggedInStudent) {
                    student = loggedInStudent;
                } else if (result instanceof Admin loggedInAdmin) {
                    admin = loggedInAdmin;
                } else if (result instanceof Employee loggedInEmployee) {
                    employee = loggedInEmployee;
                }
                showDashboard();
            }, () -> login.setDisable(false));
        });

        Button register = new Button("Create a student account");
        register.setMaxWidth(Double.MAX_VALUE);
        register.setStyle("-fx-background-color: transparent; -fx-text-fill: #2563eb; -fx-font-weight: bold;");
        register.setOnAction(event -> openRegistrationDialog());

        VBox card = new VBox(14, brand, heading, subtitle, role, email, password, login, hint, register);
        card.setPadding(new Insets(34));
        card.setMaxWidth(470);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 18; "
                + "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.12), 28, 0, 0, 8);");
        BorderPane root = new BorderPane(card);
        root.setCenter(card);
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #eff6ff, #f8fafc);");
        BorderPane.setAlignment(card, Pos.CENTER);
        stage.setScene(new Scene(root, 1050, 720));
    }

    private void openRegistrationDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Create student account");
        dialog.setHeaderText("Student registration");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField name = new TextField();
        TextField email = new TextField();
        PasswordField password = new PasswordField();
        TextField phone = new TextField();
        TextField department = new TextField();
        TextField roll = new TextField();
        name.setPromptText("Full name");
        email.setPromptText("name@college.edu");
        password.setPromptText("At least 6 characters");
        phone.setPromptText("10-digit phone");
        department.setPromptText("Department");
        roll.setPromptText("Roll number");

        GridPane fields = new GridPane();
        fields.setHgap(12);
        fields.setVgap(12);
        fields.addRow(0, new Label("Full name"), name);
        fields.addRow(1, new Label("Email"), email);
        fields.addRow(2, new Label("Password"), password);
        fields.addRow(3, new Label("Phone"), phone);
        fields.addRow(4, new Label("Department"), department);
        fields.addRow(5, new Label("Roll number"), roll);
        dialog.getDialogPane().setContent(fields);

        dialog.showAndWait().ifPresent(button -> {
            if (button != ButtonType.OK) {
                return;
            }
            String cleanName = name.getText().trim();
            String cleanEmail = email.getText().trim().toLowerCase();
            String cleanPassword = password.getText();
            String cleanPhone = phone.getText().trim();
            String cleanDepartment = department.getText().trim();
            String cleanRoll = roll.getText().trim();
            if (cleanName.isEmpty() || cleanName.length() > 100
                    || !EMAIL_PATTERN.matcher(cleanEmail).matches() || cleanEmail.length() > 100
                    || cleanPassword.length() < 6 || cleanPassword.length() > 50
                    || !PHONE_PATTERN.matcher(cleanPhone).matches()
                    || cleanDepartment.isEmpty() || cleanDepartment.length() > 100
                    || cleanRoll.isEmpty() || cleanRoll.length() > 30) {
                showAlert(Alert.AlertType.WARNING, "Check registration details",
                        "Fill every field with valid values. Passwords need at least 6 characters "
                                + "and phone numbers must contain 10 digits.");
                return;
            }
            withDatabase("Creating student account...", () -> {
                if (studentDAO.emailExists(cleanEmail)) {
                    throw new IllegalArgumentException("That email is already registered.");
                }
                if (studentDAO.rollNumberExists(cleanRoll)) {
                    throw new IllegalArgumentException("That roll number is already registered.");
                }
                return studentDAO.addStudent(new Student(cleanName, cleanEmail,
                        PasswordUtil.hash(cleanPassword), cleanPhone, cleanDepartment, cleanRoll));
            }, studentId -> showAlert(Alert.AlertType.INFORMATION, "Account created",
                    "Registration successful. Your student ID is " + studentId + ". You can now sign in."),
                    () -> { });
        });
    }

    private void showDashboard() {
        String name = currentName();
        String role = currentRole();
        Label brand = new Label("CAMPUS CARE");
        brand.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #2563eb;");
        Label greeting = new Label("Hello, " + name);
        greeting.setStyle("-fx-font-size: 13px; -fx-text-fill: #475569;");
        Button logout = new Button("Sign out");
        logout.setOnAction(event -> {
            student = null;
            admin = null;
            employee = null;
            showLogin();
        });
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox header = new HBox(18, brand, spacer, new Label(role), greeting, logout);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(18, 26, 18, 26));
        header.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-width: 0 0 1 0;");

        Label title = new Label(role + " dashboard");
        title.setStyle("-fx-font-size: 27px; -fx-font-weight: bold; -fx-text-fill: #172554;");
        Label description = new Label(dashboardDescription());
        description.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b;");

        complaintTable = createComplaintTable();
        dashboardMessage = new Label("Loading complaints...");
        dashboardMessage.setStyle("-fx-text-fill: #64748b;");
        HBox actions = createActions();

        VBox content = new VBox(14, title, description, actions, complaintTable, dashboardMessage);
        content.setPadding(new Insets(28));
        VBox.setVgrow(complaintTable, Priority.ALWAYS);
        BorderPane root = new BorderPane(content);
        root.setTop(header);
        root.setStyle("-fx-background-color: #f8fafc;");
        stage.setScene(new Scene(root, 1180, 760));
        refreshComplaints();
    }

    private HBox createActions() {
        HBox actions = new HBox(10);
        Button refresh = secondaryButton("Refresh");
        refresh.setOnAction(event -> refreshComplaints());
        Button details = secondaryButton("View details");
        details.setOnAction(event -> showSelectedDetails());
        Button history = secondaryButton("View history");
        history.setOnAction(event -> showSelectedHistory());
        actions.getChildren().addAll(refresh, details, history);

        if (student != null) {
            Button raise = primaryButton("Raise a complaint");
            raise.setOnAction(event -> openComplaintDialog());
            actions.getChildren().add(0, raise);
        } else if (admin != null) {
            Button assign = primaryButton("Assign selected");
            assign.setOnAction(event -> assignSelectedComplaint());
            actions.getChildren().add(0, assign);
            Button employees = secondaryButton("View employees");
            employees.setOnAction(event -> showEmployees());
            actions.getChildren().add(employees);
            Button statistics = secondaryButton("Statistics");
            statistics.setOnAction(event -> showStatistics());
            actions.getChildren().add(statistics);
        } else if (employee != null) {
            Button update = primaryButton("Update status");
            update.setOnAction(event -> updateSelectedStatus());
            actions.getChildren().add(0, update);
            Button comment = secondaryButton("Add update");
            comment.setOnAction(event -> addCommentToSelected());
            actions.getChildren().add(comment);
        }
        return actions;
    }

    private TableView<Complaint> createComplaintTable() {
        TableView<Complaint> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getColumns().add(column("ID", complaint -> Integer.toString(complaint.getComplaintId())));
        table.getColumns().add(column("Title", Complaint::getTitle));
        table.getColumns().add(column("Category", Complaint::getCategoryName));
        table.getColumns().add(column("Status", Complaint::getStatus));
        if (admin != null) {
            table.getColumns().add(column("Student", Complaint::getStudentName));
            table.getColumns().add(column("Assigned to", complaint ->
                    complaint.getEmployeeName() == null ? "Not assigned" : complaint.getEmployeeName()));
        } else if (employee != null) {
            table.getColumns().add(column("Student", Complaint::getStudentName));
        } else {
            table.getColumns().add(column("Assigned to", complaint ->
                    complaint.getEmployeeName() == null ? "Not assigned" : complaint.getEmployeeName()));
        }
        table.getColumns().add(column("Created", complaint ->
                complaint.getCreatedAt() == null ? "" : complaint.getCreatedAt().toString().replace('T', ' ')));
        table.setPlaceholder(new Label("No complaints found."));
        table.setRowFactory(view -> {
            javafx.scene.control.TableRow<Complaint> row = new javafx.scene.control.TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    showDetails(row.getItem());
                }
            });
            return row;
        });
        return table;
    }

    private TableColumn<Complaint, String> column(String title, Function<Complaint, String> value) {
        TableColumn<Complaint, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                nullSafe(value.apply(cell.getValue()))));
        return column;
    }

    private void refreshComplaints() {
        withDatabase("Loading complaints...", () -> {
            if (student != null) {
                return complaintDAO.getComplaintsByStudent(student.getStudentId());
            }
            if (employee != null) {
                return complaintDAO.getComplaintsByEmployee(employee.getEmployeeId());
            }
            return complaintDAO.getAllComplaints();
        }, complaints -> {
            complaintTable.getItems().setAll(complaints);
            dashboardMessage.setText(complaints.size() + " complaint(s)");
        }, () -> dashboardMessage.setText("Could not load complaints."));
    }

    private void openComplaintDialog() {
        withDatabase("Loading categories...", categoryDAO::getAllCategories, categories -> {
            if (categories.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "No categories", "No complaint categories are configured.");
                return;
            }
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Raise a complaint");
            dialog.setHeaderText("Tell us what needs attention");
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            ComboBox<Category> category = new ComboBox<>();
            category.getItems().setAll(categories);
            category.setValue(categories.get(0));
            category.setMaxWidth(Double.MAX_VALUE);
            category.setCellFactory(list -> new javafx.scene.control.ListCell<>() {
                @Override
                protected void updateItem(Category item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getCategoryName());
                }
            });
            category.setButtonCell(new javafx.scene.control.ListCell<>() {
                @Override
                protected void updateItem(Category item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getCategoryName());
                }
            });
            TextField title = new TextField();
            title.setPromptText("Short summary");
            TextArea description = new TextArea();
            description.setPromptText("Describe the issue and where it happened");
            description.setWrapText(true);
            description.setPrefRowCount(5);

            GridPane form = new GridPane();
            form.setHgap(12);
            form.setVgap(12);
            form.addRow(0, new Label("Category"), category);
            form.addRow(1, new Label("Title"), title);
            form.addRow(2, new Label("Description"), description);
            dialog.getDialogPane().setContent(form);

            dialog.showAndWait().ifPresent(button -> {
                if (button != ButtonType.OK) {
                    return;
                }
                String cleanTitle = title.getText().trim();
                String cleanDescription = description.getText().trim();
                if (cleanTitle.isEmpty() || cleanTitle.length() > 150
                        || cleanDescription.isEmpty() || cleanDescription.length() > 2000) {
                    showAlert(Alert.AlertType.WARNING, "Check complaint details",
                            "Enter a title (up to 150 characters) and description (up to 2,000 characters).");
                    return;
                }
                Category selectedCategory = category.getValue();
                withDatabase("Submitting complaint...", () -> complaintDAO.addComplaint(
                        new Complaint(student.getStudentId(), selectedCategory.getCategoryId(),
                                cleanTitle, cleanDescription), student.getName()), complaintId -> {
                    showAlert(Alert.AlertType.INFORMATION, "Complaint submitted",
                            "Your complaint was submitted. Reference ID: " + complaintId);
                    refreshComplaints();
                });
            });
        });
    }

    private void assignSelectedComplaint() {
        Complaint selected = selectedComplaint();
        if (selected == null) {
            return;
        }
        if ("RESOLVED".equals(selected.getStatus()) || "CLOSED".equals(selected.getStatus())) {
            showAlert(Alert.AlertType.WARNING, "Cannot assign",
                    "Resolved or closed complaints cannot be assigned.");
            return;
        }
        withDatabase("Loading employees...", employeeDAO::getAllEmployees, employees -> {
            if (employees.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "No employees", "There are no employees to assign.");
                return;
            }
            ComboBox<Employee> choices = new ComboBox<>();
            choices.getItems().setAll(employees);
            choices.setValue(employees.get(0));
            choices.setMaxWidth(Double.MAX_VALUE);
            choices.setCellFactory(list -> employeeListCell());
            choices.setButtonCell(employeeListCell());
            Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
            dialog.setTitle("Assign complaint");
            dialog.setHeaderText("Assign complaint #" + selected.getComplaintId());
            dialog.getDialogPane().setContent(new VBox(10,
                    new Label(selected.getTitle()), new Label("Choose an employee:"), choices));
            dialog.showAndWait().filter(button -> button == ButtonType.OK).ifPresent(button -> {
                Employee assignee = choices.getValue();
                withDatabase("Assigning complaint...", () -> complaintDAO.assignComplaint(
                        selected.getComplaintId(), assignee.getEmployeeId(), assignee.getName(),
                        selected.getStatus(), admin.getName()), assigned -> {
                    if (assigned) {
                        showAlert(Alert.AlertType.INFORMATION, "Complaint assigned",
                                "The complaint is now assigned to " + assignee.getName() + ".");
                    } else {
                        showAlert(Alert.AlertType.WARNING, "Assignment not saved",
                                "The complaint may have changed. Refresh and try again.");
                    }
                    refreshComplaints();
                });
            });
        });
    }

    private javafx.scene.control.ListCell<Employee> employeeListCell() {
        return new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Employee item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null
                        : item.getName() + " - " + item.getDepartment());
            }
        };
    }

    private void updateSelectedStatus() {
        Complaint selected = selectedComplaint();
        if (selected == null) {
            return;
        }
        if ("CLOSED".equals(selected.getStatus())) {
            showAlert(Alert.AlertType.WARNING, "Complaint is closed",
                    "Closed complaints cannot be changed.");
            return;
        }
        ComboBox<String> status = new ComboBox<>();
        status.getItems().addAll(ComplaintStatus.IN_PROGRESS.name(),
                ComplaintStatus.RESOLVED.name(), ComplaintStatus.CLOSED.name());
        status.setValue(selected.getStatus());
        TextArea comment = new TextArea();
        comment.setPromptText("Describe the work or status change");
        comment.setWrapText(true);
        comment.setPrefRowCount(4);
        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setTitle("Update complaint");
        dialog.setHeaderText("Complaint #" + selected.getComplaintId());
        dialog.getDialogPane().setContent(new VBox(10,
                new Label("New status"), status, new Label("Update comment"), comment));
        dialog.showAndWait().filter(button -> button == ButtonType.OK).ifPresent(button -> {
            String newStatus = status.getValue();
            String note = comment.getText().trim();
            if (newStatus.equals(selected.getStatus())) {
                showAlert(Alert.AlertType.INFORMATION, "No changes", "The complaint already has that status.");
                return;
            }
            if (note.isEmpty() || note.length() > 2000) {
                showAlert(Alert.AlertType.WARNING, "Comment required",
                        "Enter a comment of up to 2,000 characters.");
                return;
            }
            withDatabase("Updating complaint...", () -> complaintDAO.updateStatus(
                    selected.getComplaintId(), employee.getEmployeeId(), selected.getStatus(),
                    newStatus, note, employee.getName()), updated -> {
                if (updated) {
                    showAlert(Alert.AlertType.INFORMATION, "Status updated",
                            "The complaint status is now " + newStatus + ".");
                } else {
                    showAlert(Alert.AlertType.WARNING, "Update not saved",
                            "The complaint may have changed. Refresh and try again.");
                }
                refreshComplaints();
            });
        });
    }

    private void addCommentToSelected() {
        Complaint selected = selectedComplaint();
        if (selected == null) {
            return;
        }
        TextArea comment = new TextArea();
        comment.setPromptText("Enter an update");
        comment.setWrapText(true);
        comment.setPrefRowCount(4);
        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setTitle("Add complaint update");
        dialog.setHeaderText("Complaint #" + selected.getComplaintId());
        dialog.getDialogPane().setContent(comment);
        dialog.showAndWait().filter(button -> button == ButtonType.OK).ifPresent(button -> {
            String note = comment.getText().trim();
            if (note.isEmpty() || note.length() > 2000) {
                showAlert(Alert.AlertType.WARNING, "Update required",
                        "Enter an update of up to 2,000 characters.");
                return;
            }
            withDatabase("Saving update...", () -> complaintDAO.addComment(
                    selected.getComplaintId(), employee.getEmployeeId(),
                    selected.getStatus(), note, employee.getName()), saved -> {
                if (saved) {
                    showAlert(Alert.AlertType.INFORMATION, "Update added", "Your update was saved.");
                } else {
                    showAlert(Alert.AlertType.WARNING, "Update not saved",
                            "The complaint may have changed. Refresh and try again.");
                }
                refreshComplaints();
            });
        });
    }

    private void showSelectedDetails() {
        Complaint selected = selectedComplaint();
        if (selected != null) {
            showDetails(selected);
        }
    }

    private void showDetails(Complaint complaint) {
        String assigned = complaint.getEmployeeName() == null ? "Not assigned" : complaint.getEmployeeName();
        String details = "Complaint #" + complaint.getComplaintId()
                + "\n\nTitle: " + complaint.getTitle()
                + "\nCategory: " + complaint.getCategoryName()
                + "\nStatus: " + complaint.getStatus()
                + "\nStudent: " + nullSafe(complaint.getStudentName())
                + "\nAssigned to: " + assigned
                + "\nCreated: " + complaint.getCreatedAt()
                + "\n\nDescription:\n" + complaint.getDescription();
        showAlert(Alert.AlertType.INFORMATION, "Complaint details", details);
    }

    private void showSelectedHistory() {
        Complaint selected = selectedComplaint();
        if (selected == null) {
            return;
        }
        withDatabase("Loading complaint history...", () -> complaintDAO.getHistory(selected.getComplaintId()),
                updates -> {
                    StringBuilder history = new StringBuilder("History for complaint #")
                            .append(selected.getComplaintId()).append("\n\n");
                    if (updates.isEmpty()) {
                        history.append("No history entries.");
                    }
                    for (ComplaintUpdate update : updates) {
                        history.append(update.getCreatedAt()).append("  ")
                                .append(update.getUpdatedByRole()).append(" - ")
                                .append(update.getUpdatedByName()).append("\n")
                                .append(update.getOldStatus() == null ? "NEW" : update.getOldStatus())
                                .append(" -> ").append(update.getNewStatus()).append("\n")
                                .append(update.getComment()).append("\n\n");
                    }
                    showAlert(Alert.AlertType.INFORMATION, "Complaint history", history.toString());
                });
    }

    private void showEmployees() {
        withDatabase("Loading employees...", employeeDAO::getAllEmployees, employees -> {
            StringBuilder text = new StringBuilder();
            for (Employee item : employees) {
                text.append(item.getName()).append(" | ").append(item.getDepartment())
                        .append(" | assigned: ").append(item.getAssignedCount())
                        .append(" | active: ").append(item.getActiveCount()).append('\n');
            }
            showAlert(Alert.AlertType.INFORMATION, "Employees", text.length() == 0
                    ? "No employees found." : text.toString());
        });
    }

    private void showStatistics() {
        withDatabase("Loading statistics...", () -> {
            StringBuilder text = new StringBuilder("Total complaints: ")
                    .append(complaintDAO.countAll())
                    .append("\nUnassigned: ").append(complaintDAO.countUnassigned()).append("\n\nBy status:\n");
            complaintDAO.countByStatus().forEach((status, count) ->
                    text.append(status).append(": ").append(count).append('\n'));
            text.append("\nBy category:\n");
            complaintDAO.countByCategory().forEach((category, count) ->
                    text.append(category).append(": ").append(count).append('\n'));
            return text.toString();
        }, text -> showAlert(Alert.AlertType.INFORMATION, "Complaint statistics", text));
    }

    private Complaint selectedComplaint() {
        Complaint selected = complaintTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Select a complaint", "Choose a complaint from the table first.");
        }
        return selected;
    }

    private <T> void withDatabase(String message, Callable<T> operation, Consumer<T> onSuccess) {
        withDatabase(message, operation, onSuccess, () -> { });
    }

    private <T> void withDatabase(String message, Callable<T> operation, Consumer<T> onSuccess,
                                  Runnable onFailure) {
        if (dashboardMessage != null) {
            dashboardMessage.setText(message);
        }
        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                return operation.call();
            }
        };
        task.setOnSucceeded(event -> onSuccess.accept(task.getValue()));
        task.setOnFailed(event -> {
            onFailure.run();
            Throwable error = task.getException();
            String details = error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage();
            showAlert(Alert.AlertType.ERROR, "Database operation failed", details);
        });
        Thread worker = new Thread(task, "complaint-database-task");
        worker.setDaemon(true);
        worker.start();
    }

    private String currentName() {
        if (student != null) {
            return student.getName();
        }
        if (admin != null) {
            return admin.getName();
        }
        return employee.getName();
    }

    private String currentRole() {
        if (student != null) {
            return "Student";
        }
        if (admin != null) {
            return "Admin";
        }
        return "Employee";
    }

    private String dashboardDescription() {
        if (student != null) {
            return "Raise service requests and follow their progress.";
        }
        if (admin != null) {
            return "Review campus requests, assign staff, and monitor progress.";
        }
        return "Review assigned requests and keep students updated.";
    }

    private Button primaryButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; "
                + "-fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 10 16;");
        return button;
    }

    private Button secondaryButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: white; -fx-text-fill: #334155; "
                + "-fx-border-color: #cbd5e1; -fx-border-radius: 8; -fx-background-radius: 8; "
                + "-fx-padding: 9 14;");
        return button;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
