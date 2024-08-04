import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;

public class EmployeeController {

    @FXML
    private TableView<Employee> employeeTable;
    @FXML
    private TableColumn<Employee, Integer> employeeIdColumn;
    @FXML
    private TableColumn<Employee, String> nameColumn;
    @FXML
    private TableColumn<Employee, Integer> roleColumn;
    @FXML
    private TableColumn<Employee, String> keyColumn;
    @FXML
    private TableColumn<Employee, String> cinColumn;
    @FXML
    private TableColumn<Employee, Void> actionColumn;
    @FXML
    private Button saveButton;
    @FXML
    private TextField nameTextField;
    @FXML
    private ComboBox<String> roleComboBox;
    @FXML
    private TextField keyTextField;
    @FXML
    private TextField cinTextField;
    @FXML
    private Button addButton;

    private ObservableList<Employee> employeeData = FXCollections.observableArrayList();
    private ObservableList<String> roles = FXCollections.observableArrayList("Guide", "Co-Guide");

    @FXML
    private void initialize() {
        employeeIdColumn.setCellValueFactory(new PropertyValueFactory<>("employeeId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        keyColumn.setCellValueFactory(new PropertyValueFactory<>("key"));
        cinColumn.setCellValueFactory(new PropertyValueFactory<>("cin"));

        employeeTable.setEditable(true);
        employeeTable.setItems(employeeData); // Ensure the table is bound to the data list

        nameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        roleColumn.setCellFactory(column -> new TableCell<Employee, Integer>() {
            private final ComboBox<String> comboBox = new ComboBox<>(roles);

            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    comboBox.setValue(roles.get(item));
                    comboBox.valueProperty().addListener((obs, oldValue, newValue) -> {
                        if (newValue != null) {
                            Employee employee = getTableView().getItems().get(getIndex());
                            employee.setRole(roles.indexOf(newValue));
                            updateEmployeeRoleInDatabase(employee);
                        }
                    });
                    setGraphic(comboBox);
                }
            }
        });

        nameColumn.setOnEditCommit(event -> {
            Employee employee = event.getRowValue();
            employee.setName(event.getNewValue());
            updateEmployeeNameInDatabase(employee);
        });

        addButtonToTable();

        loadEmployeeData();

        roleComboBox.setItems(roles);
    }

    private void loadEmployeeData() {
        employeeData.clear();
        String query = "SELECT EmployeID, name, role, `key`, cin FROM employees";
    
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
    
            while (rs.next()) {
                employeeData.add(new Employee(
                    rs.getInt("EmployeID"),
                    rs.getString("name"),
                    rs.getInt("role"),
                    rs.getString("key"),
                    rs.getString("cin")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    
        employeeTable.setItems(employeeData);
    }

    private void addButtonToTable() {
        Callback<TableColumn<Employee, Void>, TableCell<Employee, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Employee, Void> call(final TableColumn<Employee, Void> param) {
                final TableCell<Employee, Void> cell = new TableCell<>() {

                    private final Button btn = new Button("Delete");

                    {
                        btn.setOnAction(event -> {
                            Employee employee = getTableView().getItems().get(getIndex());
                            deleteEmployee(employee);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(btn);
                        }
                    }
                };
                return cell;
            }
        };

        actionColumn.setCellFactory(cellFactory);
    }

    private void deleteEmployee(Employee employee) {
        employeeData.remove(employee);
        deleteEmployeeFromDatabase(employee);
    }

    private void deleteEmployeeFromDatabase(Employee employee) {
        String deleteQuery = "DELETE FROM employees WHERE EmployeID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(deleteQuery)) {
            pstmt.setInt(1, employee.getEmployeeId());
            pstmt.executeUpdate();
            System.out.println("Deleted employee: " + employee.getName());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateEmployeeRoleInDatabase(Employee employee) {
        String updateQuery = "UPDATE employees SET role = ? WHERE EmployeID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {
            pstmt.setInt(1, employee.getRole());
            pstmt.setInt(2, employee.getEmployeeId());
            pstmt.executeUpdate();
            System.out.println("Updated role for employee: " + employee.getName() + " to " + employee.getRole());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateEmployeeNameInDatabase(Employee employee) {
        String updateQuery = "UPDATE employees SET name = ? WHERE EmployeID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {
            pstmt.setString(1, employee.getName());
            pstmt.setInt(2, employee.getEmployeeId());
            pstmt.executeUpdate();
            System.out.println("Updated name for employee: " + employee.getName());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateEmployeeInDatabase(Employee employee) {
        String updateQuery = "UPDATE employees SET name = ?, role = ?, `key` = ?, cin = ? WHERE EmployeID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {
            pstmt.setString(1, employee.getName());
            pstmt.setInt(2, employee.getRole());
            pstmt.setString(3, employee.getKey());
            pstmt.setString(4, employee.getCin());
            pstmt.setInt(5, employee.getEmployeeId());
            pstmt.executeUpdate();
            System.out.println("Updated employee: " + employee.getName());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSaveButtonAction() {
        for (Employee employee : employeeData) {
            if (employee.getEmployeeId() == 0) {
                addEmployeeToDatabase(employee); // Add new employee to the database
            } else {
                updateEmployeeInDatabase(employee); // Update existing employee in the database
            }
        }
        System.out.println("All changes saved to the database.");
    }

    @FXML
    private void handleAddButtonAction() {
        String name = nameTextField.getText();
        String roleString = roleComboBox.getValue();
        String key = keyTextField.getText();
        String cin = cinTextField.getText();
        int role = roles.indexOf(roleString);

        if (name == null || name.isEmpty()) {
            showAlert("Name is required.");
            return;
        }

        if (roleString == null) {
            showAlert("Role must be selected.");
            return;
        }

        if (key == null || key.isEmpty() || key.length() > 6) {
            showAlert("Key must be provided and must be 6 characters or less.");
            return;
        }

        if (cin == null || cin.isEmpty() || !cin.matches("\\d+")) {
            showAlert("CIN must be provided and must be numeric.");
            return;
        }

        Employee newEmployee = new Employee(0, name, role, key, cin);
        employeeData.add(newEmployee); // Add the new employee to the table
        addEmployeeToDatabase(newEmployee); // Add the new employee to the database
        employeeTable.refresh(); // Refresh the table to show the new employee
        System.out.println("Added employee to table: " + newEmployee.getName());
        nameTextField.clear();
        roleComboBox.setValue(null);
        keyTextField.clear();
        cinTextField.clear();
    }

    private void addEmployeeToDatabase(Employee employee) {
        String insertQuery = "INSERT INTO employees (name, role, `key`, cin) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, employee.getName());
            pstmt.setInt(2, employee.getRole());
            pstmt.setString(3, employee.getKey());
            pstmt.setString(4, employee.getCin());
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    employee.setEmployeeId(generatedKeys.getInt(1));
                }
            }
            System.out.println("Added employee to database: " + employee.getName());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Validation Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class Employee {
        private int employeeId;
        private String name;
        private int role;
        private String key;
        private String cin;

        public Employee(int employeeId, String name, int role, String key, String cin) {
            this.employeeId = employeeId;
            this.name = name;
            this.role = role;
            this.key = key;
            this.cin = cin;
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

        public int getRole() {
            return role;
        }

        public void setRole(int role) {
            this.role = role;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getCin() {
            return cin;
        }

        public void setCin(String cin) {
            this.cin = cin;
        }
    }
}