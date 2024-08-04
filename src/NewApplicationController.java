import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class NewApplicationController {

    @FXML
    private TextField applicationIdField;

    @FXML
    private ComboBox<String> userIdComboBox;

    @FXML
    private DatePicker applicationDatePicker;

    // @FXML
    // private ComboBox<String> licenseClassComboBox;

    @FXML
    private TextField applicationFeesField;

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private DatePicker dobDatePicker;

    @FXML
    private TextField addressField;

    @FXML
    private TextField phoneNumberField;

    @FXML
    private TextField carteIdentiteField;

    @FXML
    private ComboBox<String> guidedByComboBox;


    private Map<String, Integer> carteIdentiteToUserIdMap = new HashMap<>();
    private Map<String, String> carteIdentiteToCodeLicenseMap = new HashMap<>();

    @FXML
    private void initialize() {
        // Fetch CarteIdentite and UserID from the database and populate the userIdComboBox
        fetchUserIdData();

        // Fetch employee data and populate the guidedByComboBox
        fetchEmployeeData();

        // Fetch license types and populate the licenseClassComboBox
        // fetchLicenseTypesData();

        // Add event handler to userIdComboBox to refresh data when clicked
        userIdComboBox.setOnMouseClicked(event -> fetchUserIdData());

        // Add event handler to userIdComboBox to set applicationIdField when a CarteIdentite is selected
        userIdComboBox.setOnAction(event -> {
            String selectedCarteIdentite = userIdComboBox.getValue();
            String codeLicense = carteIdentiteToCodeLicenseMap.get(selectedCarteIdentite);
            applicationIdField.setText(codeLicense);
        });
    }

    private void fetchEmployeeData() {
        guidedByComboBox.getItems().clear();
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT EmployeID, name FROM employees")) {

            while (resultSet.next()) {
                int employeeId = resultSet.getInt("EmployeID");
                String name = resultSet.getString("name");
                String displayText = employeeId + " - " + name;
                guidedByComboBox.getItems().add(displayText);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fetchUserIdData() {
        carteIdentiteToUserIdMap.clear();
        carteIdentiteToCodeLicenseMap.clear();
        userIdComboBox.getItems().clear();
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT UserID, CarteIdentite, CodeLicense, firstname, lastname FROM USERS")) {

            while (resultSet.next()) {
                int userId = resultSet.getInt("UserID");
                String carteIdentite = resultSet.getString("CarteIdentite");
                String codeLicense = resultSet.getString("CodeLicense");
                String firstName = resultSet.getString("firstname");
                String lastName = resultSet.getString("lastname");
                String displayText = carteIdentite + " - " + firstName + " " + lastName;
                carteIdentiteToUserIdMap.put(displayText, userId);
                carteIdentiteToCodeLicenseMap.put(displayText, codeLicense);
                userIdComboBox.getItems().add(displayText);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSubmitApplication() {
        // Handle the submission of the new application
        String applicationId = applicationIdField.getText();
        String selectedCarteIdentite = userIdComboBox.getValue();
        Integer userId = carteIdentiteToUserIdMap.get(selectedCarteIdentite);
        LocalDate applicationDate = applicationDatePicker.getValue();
        // String selectedLicenseClass = licenseClassComboBox.getValue();
        // Integer licenseTypeId = licenseClassToIdMap.get(selectedLicenseClass);
        String applicationFees = applicationFeesField.getText();

        if (applicationId.isEmpty() || selectedCarteIdentite == null || applicationDate == null || 
            // selectedLicenseClass == null || 
            applicationFees.isEmpty() || guidedByComboBox.getValue() == null) {
            showAlert("Error", "All fields must be filled out.", Alert.AlertType.ERROR);
            return;
        }

        System.out.println("Submitting new application:");
        System.out.println("Application ID: " + applicationId);
        System.out.println("User ID: " + userId);
        System.out.println("Application Date: " + applicationDate);
        // System.out.println("License Type ID: " + licenseTypeId);
        System.out.println("Application Fees: " + applicationFees);
        System.out.println("Chef Guided By: " + guidedByComboBox.getValue());

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                 "INSERT INTO applications (UserID, ApplicationDate, CreatedBy, ApplicationFees) VALUES (?, ?, ?, ?)")) {

            preparedStatement.setInt(1, userId);
            preparedStatement.setDate(2, java.sql.Date.valueOf(applicationDate));
            preparedStatement.setString(3, guidedByComboBox.getValue());
            preparedStatement.setBigDecimal(4, new java.math.BigDecimal(applicationFees));

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                showAlert("Success", "Application submitted successfully.", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Error", "Failed to submit application.", Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "An error occurred while submitting the application.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handlePushPersonalInfo() {
        // Handle pushing personal info to the USERS table
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        LocalDate dob = dobDatePicker.getValue();
        String address = addressField.getText();
        String phoneNumber = phoneNumberField.getText();
        String carteIdentite = carteIdentiteField.getText();
        String codeLicense = generateCodeLicense();

        if (firstName.isEmpty() || lastName.isEmpty() || dob == null || address.isEmpty() || 
            phoneNumber.isEmpty() || carteIdentite.isEmpty() || codeLicense.isEmpty()) {
            showAlert("Error", "All fields must be filled out.", Alert.AlertType.ERROR);
            return;
        }

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                 "INSERT INTO USERS (CarteIdentite, CodeLicense, firstname, lastname, address, phone, birth) VALUES (?, ?, ?, ?, ?, ?, ?)")) {

            preparedStatement.setString(1, carteIdentite);
            preparedStatement.setString(2, codeLicense);
            preparedStatement.setString(3, firstName);
            preparedStatement.setString(4, lastName);
            preparedStatement.setString(5, address);
            preparedStatement.setString(6, phoneNumber);
            preparedStatement.setDate(7, java.sql.Date.valueOf(dob));

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                showAlert("Success", "Personal info pushed to USERS table successfully.", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Error", "Failed to push personal info to USERS table.", Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "An error occurred while pushing personal info to USERS table.", Alert.AlertType.ERROR);
        }
    }

    private String generateCodeLicense() {
        String alphanumeric = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 5; i++) {
            int index = random.nextInt(alphanumeric.length());
            sb.append(alphanumeric.charAt(index));
        }
        return sb.toString();
    }

    @FXML
    private void handleClose() {
        // Handle closing the new application window
        Stage stage = (Stage) applicationIdField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}