import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class NewLicenseController {

    @FXML private ComboBox<String> licenseTypeComboBox;
    @FXML private DatePicker startDateField;
    @FXML private DatePicker expiryDateField;
    @FXML private TextField licenseValidityPeriod;
    @FXML private TableView<ObservableList<String>> licensesTable;
    @FXML private TableColumn<ObservableList<String>, String> licenseIdColumn;
    @FXML private TableColumn<ObservableList<String>, String> licenseTypeColumn;
    @FXML private TableColumn<ObservableList<String>, String> validityPeriodColumn;
    @FXML private TableColumn<ObservableList<String>, String> startDateColumn;
    @FXML private TableColumn<ObservableList<String>, String> expiryDateColumn;
    @FXML private TableColumn<ObservableList<String>, String> statusColumn;

    private int applicationId;
    private ObservableList<ObservableList<String>> licensesData = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        // Initialize the ComboBox with license types from the database
        populateLicenseTypes();

        // Set the start date to the current date
        startDateField.setValue(LocalDate.now());

        // Add listener to ComboBox to update validity period
        licenseTypeComboBox.setOnAction(event -> updateValidityPeriod());

        // Initialize the table columns
        licenseIdColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(0)));
        licenseTypeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(1)));
        validityPeriodColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(2)));
        startDateColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(3)));
        expiryDateColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(4)));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(5)));
        statusColumn.setCellFactory(column -> new TableCell<ObservableList<String>, String>() {
            private final Circle circle = new Circle(5);

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    setText(item);
                    if (item.equals("Active")) {
                        circle.setFill(Color.GREEN);
                    } else if (item.equals("Expired")) {
                        circle.setFill(Color.RED);
                    } else if (item.equals("Pending")) {
                        circle.setFill(Color.YELLOW);
                    } else {
                        circle.setFill(Color.GRAY);
                    }
                    setGraphic(circle);
                }
            }
        });

        // Load licenses data
        loadLicenses();
    }

    public void setApplicationId(int applicationId) {
        this.applicationId = applicationId;
        loadLicenses(); // Load licenses when applicationId is set
    }

    private void populateLicenseTypes() {
        String query = "SELECT LicenseClass FROM license_types";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                licenseTypeComboBox.getItems().add(rs.getString("LicenseClass"));
            }

            // Set the ComboBox value to the first item if the list is not empty
            if (!licenseTypeComboBox.getItems().isEmpty()) {
                licenseTypeComboBox.setValue(licenseTypeComboBox.getItems().get(0));
                updateValidityPeriod(); // Update the validity period for the selected item
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateValidityPeriod() {
        String selectedLicenseType = licenseTypeComboBox.getValue();
        if (selectedLicenseType == null) {
            return;
        }

        String query = "SELECT ValidityPeriod FROM license_types WHERE LicenseClass = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, selectedLicenseType);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                licenseValidityPeriod.setText(String.valueOf(rs.getInt("ValidityPeriod")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSubmit() {
        String licenseType = licenseTypeComboBox.getValue();
        System.out.println(licenseType);
        LocalDate startDate = startDateField.getValue();
        LocalDate expiryDate = expiryDateField.getValue();
        // LocalDate currentDate = LocalDate.now();

        if (licenseType == null || startDate == null || expiryDate == null) {
            showAlert("Error", "License type, start date, and expiry date must all be filled out.");
            return;
        }

        // if (startDate.isBefore(currentDate)) {
        //     showAlert("Error", "Start date cannot be in the past.");
        //     return;
        // }

        if (expiryDate.isBefore(startDate.plusWeeks(1))) {
            showAlert("Error", "Expiry date must be at least one week after the start date.");
            return;
        }

        saveLicenseToDatabase(applicationId, licenseType, startDate, expiryDate);
    }

    private void saveLicenseToDatabase(int applicationId, String licenseType, LocalDate startDate, LocalDate expiryDate) {
        String getLicenseTypeIdQuery = "SELECT LicenseTypeID FROM license_types WHERE LicenseClass = ?";
        String insertLicenseQuery = "INSERT INTO LICENSES (ApplicationID, StartDate, ExpiryDate, LicenseTypeID) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement getLicenseTypeIdStmt = conn.prepareStatement(getLicenseTypeIdQuery);
             PreparedStatement insertLicenseStmt = conn.prepareStatement(insertLicenseQuery)) {

            // Get the LicenseTypeID
            getLicenseTypeIdStmt.setString(1, licenseType);
            ResultSet rs = getLicenseTypeIdStmt.executeQuery();
            if (rs.next()) {
                int licenseTypeId = rs.getInt("LicenseTypeID");
                System.out.println("LicenseTypeID: " + licenseTypeId); // Log the LicenseTypeID

                // Insert the new license
                insertLicenseStmt.setInt(1, applicationId);
                insertLicenseStmt.setDate(2, java.sql.Date.valueOf(startDate));
                insertLicenseStmt.setDate(3, java.sql.Date.valueOf(expiryDate));
                insertLicenseStmt.setInt(4, licenseTypeId);

                int rowsAffected = insertLicenseStmt.executeUpdate();
                if (rowsAffected > 0) {
                    showAlert("Info", "License saved successfully.");
                    loadLicenses(); // Reload licenses after saving
                } else {
                    showAlert("Error", "Failed to save license.");
                }
            } else {
                showAlert("Error", "License type not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "An error occurred while saving the license: " + e.getMessage());
        }
    }

    private void loadLicenses() {
        licensesData.clear();
        String query = "SELECT l.LicenseID, lt.LicenseClass, lt.ValidityPeriod, l.StartDate, l.ExpiryDate, l.LicenseTypeID " +
                       "FROM LICENSES l " +
                       "JOIN APPLICATIONS a ON l.ApplicationID = a.ApplicationID " +
                       "JOIN license_types lt ON l.LicenseTypeID = lt.LicenseTypeID";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add(rs.getString("LicenseID"));
                row.add(rs.getString("LicenseClass"));
                row.add(rs.getString("ValidityPeriod"));
                
                LocalDate startDate = rs.getDate("StartDate").toLocalDate();
                LocalDate expiryDate = rs.getDate("ExpiryDate").toLocalDate();
                LocalDate currentDate = LocalDate.now();
                
                row.add(startDate.toString());
                row.add(expiryDate.toString());
                
                // Determine the status
                String status;
                if (currentDate.isBefore(startDate)) {
                    status = "Pending";
                } else if (currentDate.isEqual(startDate)) {
                    status = "Active";
                } else if (currentDate.isAfter(expiryDate)) {
                    status = "Expired";
                } else {
                    status = "Active";
                }
                row.add(status);

                licensesData.add(row);
            }

            licensesTable.setItems(licensesData);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load licenses: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}