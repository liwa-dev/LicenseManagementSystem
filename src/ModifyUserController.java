import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

public class ModifyUserController {

    @FXML private TextField personIdField;
    @FXML private TextField CarteIdentite;
    @FXML private TextField codeLicense;
    @FXML private ComboBox<String> genderComboBox;
    @FXML private TextField name;
    @FXML private TextField Surname;
    @FXML private TextField Adress;
    @FXML private DatePicker dobDatePicker;
    @FXML private TextField phoneField;
    @FXML private TextField countryField;
    @FXML private TextField payment;
    @FXML private CheckBox statusCheckBox;
    @FXML private ComboBox<String> licenseClassComboBox;

    private Connection conn;
    private int currentUserId;
    private int currentApplicationId;

    @FXML
    public void initialize() {
        // Initialize gender ComboBox
        ObservableList<String> genderList = FXCollections.observableArrayList("Male", "Female");
        genderComboBox.setItems(genderList);

        // Initialize connection (assuming you have a method to get connection)
        try {
            conn = DatabaseConnection.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to establish database connection: " + e.getMessage());
        }

        // Load LicenseClass values into licenseClassComboBox
        loadLicenseClasses();
    }

    private void loadLicenseClasses() {
        String query = "SELECT LicenseClass FROM license_types";
        ObservableList<String> licenseClasses = FXCollections.observableArrayList();

        try (PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                licenseClasses.add(rs.getString("LicenseClass"));
            }

            licenseClassComboBox.setItems(licenseClasses);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load license classes: " + e.getMessage());
        }
    }

    public void loadUserData(int userId, int applicationId) {
        currentUserId = userId;
        currentApplicationId = applicationId;
        String userQuery = "SELECT * FROM users WHERE UserID = ?";
        String applicationQuery = "SELECT * FROM applications WHERE ApplicationID = ?";
        String licenseTypeQuery = "SELECT LicenseClass FROM license_types WHERE LicenseTypeID = ?";

        try (PreparedStatement userPstmt = conn.prepareStatement(userQuery);
             PreparedStatement applicationPstmt = conn.prepareStatement(applicationQuery)) {

            // Load user data
            userPstmt.setInt(1, userId);
            ResultSet userRs = userPstmt.executeQuery();
            if (userRs.next()) {
                personIdField.setText(String.valueOf(userRs.getInt("UserID")));
                CarteIdentite.setText(userRs.getString("CarteIdentite"));
                codeLicense.setText(userRs.getString("CodeLicense"));
                genderComboBox.setValue(convertDbGenderToComboBox(userRs.getString("gender")));
                name.setText(userRs.getString("firstname"));
                Surname.setText(userRs.getString("lastname"));
                Adress.setText(userRs.getString("address"));
                dobDatePicker.setValue(userRs.getDate("birth").toLocalDate());
                phoneField.setText(userRs.getString("phone"));  
            }

            // Load application status and license class
            applicationPstmt.setInt(1, applicationId);
            ResultSet applicationRs = applicationPstmt.executeQuery();
            if (applicationRs.next()) {
                payment.setText(applicationRs.getString("ApplicationFees")); 
                statusCheckBox.setSelected(applicationRs.getInt("STATUS") == 1);

                int licenseTypeId = applicationRs.getInt("LicenseTypeID");
                try (PreparedStatement licenseTypePstmt = conn.prepareStatement(licenseTypeQuery)) {
                    licenseTypePstmt.setInt(1, licenseTypeId);
                    ResultSet licenseTypeRs = licenseTypePstmt.executeQuery();
                    if (licenseTypeRs.next()) {
                        licenseClassComboBox.setValue(licenseTypeRs.getString("LicenseClass"));
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load user data: " + e.getMessage());
        }
    }

    @FXML
    private void handleSave() {
        // Validate license code length
        if (codeLicense.getText().length() != 5) {
            showAlert("Error", "License code must be exactly 5 characters long.");
            return;
        }

        // Validate phone number length
        if (phoneField.getText().length() != 8) {
            showAlert("Error", "Phone number must be exactly 8 digits long.");
            return;
        }

        // Validate payment field contains only numbers
        if (!payment.getText().matches("\\d+")) {
            showAlert("Error", "Payment field must contain only numeric values.");
            return;
        }
        

        String userQuery = "UPDATE users SET firstname=?, lastname=?, gender=?, CarteIdentite=?, CodeLicense=?, address=?, birth=?, phone=? WHERE UserID=?";
        String applicationQuery = "UPDATE applications SET ApplicationFees=?, STATUS=? WHERE ApplicationID=?";

        try (PreparedStatement userPstmt = conn.prepareStatement(userQuery);
             PreparedStatement applicationPstmt = conn.prepareStatement(applicationQuery)) {

            // Update user data
            userPstmt.setString(1, name.getText());
            userPstmt.setString(2, Surname.getText());
            userPstmt.setString(3, convertComboBoxGenderToDb(genderComboBox.getValue()));
            userPstmt.setString(4, CarteIdentite.getText());
            userPstmt.setString(5, codeLicense.getText());
            userPstmt.setString(6, Adress.getText());
            userPstmt.setDate(7, java.sql.Date.valueOf(dobDatePicker.getValue()));
            userPstmt.setString(8, phoneField.getText());
            userPstmt.setInt(9, currentUserId);
            userPstmt.executeUpdate();

            // Update application status
            applicationPstmt.setString(1, payment.getText());
            applicationPstmt.setInt(2, statusCheckBox.isSelected() ? 1 : 0);
            applicationPstmt.setInt(3, currentApplicationId);
            applicationPstmt.executeUpdate();

            showAlert("Success", "User data and application status updated successfully!");

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to update user data and application status: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        // TODO: Implement cancel functionality
        // This could close the window or navigate back to the previous screen
    }

    @FXML
    private void handleModify() {
        System.out.println("User ID: " + currentUserId);
        System.out.println("Application ID: " + currentApplicationId);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private String convertDbGenderToComboBox(String dbGender) {
        switch (dbGender) {
            case "M":
                return "Male";
            case "F":
                return "Female";
            default:
                return null;
        }
    }

    private String convertComboBoxGenderToDb(String comboBoxGender) {
        switch (comboBoxGender) {
            case "Male":
                return "M";
            case "Female":
                return "F";
            default:
                return null;
        }
    }
}