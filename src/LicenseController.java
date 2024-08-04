import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class LicenseController {

    @FXML
    private TableView<LicenseData> licenseTable;
    @FXML
    private TableColumn<LicenseData, Integer> applicationIdColumn;
    @FXML
    private TableColumn<LicenseData, String> licenseClassColumn;
    @FXML
    private TableColumn<LicenseData, Integer> validityPeriodColumn;
    @FXML
    private TableColumn<LicenseData, String> statusColumn; // New status column
    @FXML
    private GridPane userGrid;
    @FXML
    private Label nameLabel;
    @FXML
    private Label genderLabel;
    @FXML
    private Label phoneLabel;
    @FXML
    private Label birthLabel;
    @FXML
    private Label addressLabel;
    @FXML
    private Label cinLabel;

    private int userId;

    public void setUserId(int userId) {
        this.userId = userId;
        loadLicenseData();
        loadUserData();
    }

    private void loadLicenseData() {
        ObservableList<LicenseData> data = FXCollections.observableArrayList();

        String licenseQuery = "SELECT a.ApplicationID, l.LicenseID, lt.LicenseClass, lt.ValidityPeriod, l.ExpiryDate " +
                              "FROM applications a, licenses l, license_types lt " +
                              "WHERE a.ApplicationID = l.ApplicationID " +
                              "AND l.LicenseTypeID = lt.LicenseTypeID " +
                              "AND a.UserID = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement licenseStmt = connection.prepareStatement(licenseQuery)) {
            licenseStmt.setInt(1, userId);
            ResultSet licenseResultSet = licenseStmt.executeQuery();
            while (licenseResultSet.next()) {
                int applicationId = licenseResultSet.getInt("ApplicationID");
                String licenseClass = licenseResultSet.getString("LicenseClass");
                int validityPeriod = licenseResultSet.getInt("ValidityPeriod");
                String expiryDate = licenseResultSet.getString("ExpiryDate");
                System.out.println("ApplicationID: " + applicationId + ", LicenseClass: " + licenseClass + ", ValidityPeriod: " + validityPeriod + ", ExpiryDate: " + expiryDate); // Debug statement
                data.add(new LicenseData(applicationId, licenseClass, validityPeriod, expiryDate));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        applicationIdColumn.setCellValueFactory(new PropertyValueFactory<>("applicationId"));
        licenseClassColumn.setCellValueFactory(new PropertyValueFactory<>("licenseClass"));
        validityPeriodColumn.setCellValueFactory(new PropertyValueFactory<>("validityPeriod"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusColumn.setCellFactory(column -> new TableCell<LicenseData, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Circle dot = new Circle(5);
                    if (status.equals("License expired!")) {
                        dot.setFill(Color.RED);
                    } else {
                        dot.setFill(Color.GREEN);
                    }
                    setText(status);
                    setGraphic(dot);
                }
            }
        });
        licenseTable.setItems(data);
    }

    private void loadUserData() {
        String userQuery = "SELECT CarteIdentite, CodeLicense, firstname, lastname, address, phone, birth, gender FROM users WHERE UserID = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement selectStmt = connection.prepareStatement(userQuery)) {
            selectStmt.setInt(1, userId);
            ResultSet resultSet = selectStmt.executeQuery();

            if (resultSet.next()) {
                // Process the result set and update the UI components
                String name = resultSet.getString("firstname") + " " + resultSet.getString("lastname");
                nameLabel.setText(name);

                String gender = resultSet.getString("gender");
                if ("M".equals(gender)) {
                    genderLabel.setText("Male");
                } else if ("F".equals(gender)) {
                    genderLabel.setText("Female");
                } else {
                    genderLabel.setText(gender); // Fallback to original value if not "M" or "F"
                }

                phoneLabel.setText(resultSet.getString("phone"));
                birthLabel.setText(resultSet.getString("birth"));
                addressLabel.setText(resultSet.getString("address"));
                cinLabel.setText(resultSet.getString("CarteIdentite"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        // Implement logout logic here, e.g., navigate to login screen
        System.out.println("User logged out");
    }

    public static class LicenseData {
        private final int applicationId;
        private final String licenseClass;
        private final int validityPeriod;
        private final String expiryDate;

        public LicenseData(int applicationId, String licenseClass, int validityPeriod, String expiryDate) {
            this.applicationId = applicationId;
            this.licenseClass = licenseClass;
            this.validityPeriod = validityPeriod;
            this.expiryDate = expiryDate;
        }

        public int getApplicationId() {
            return applicationId;
        }

        public String getLicenseClass() {
            return licenseClass;
        }

        public int getValidityPeriod() {
            return validityPeriod;
        }

        public String getStatus() {
            LocalDate expiry = LocalDate.parse(expiryDate);
            if (expiry.isBefore(LocalDate.now())) {
                return "License expired!";
            } else {
                return "Approved";
            }
        }
    }
}