import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ControllerClass {

    @FXML
    private AnchorPane adminContent;

    @FXML
    private TextField adminUsernameField;

    @FXML
    private TextField adminPasswordField;

    private boolean isAdminPanelVisible = false;

    @FXML
    private TextField cinField;

    @FXML
    private TextField keyField;

    @FXML
    private void toggleAdminPanel() {
        TranslateTransition slideAdmin = new TranslateTransition(Duration.seconds(0.5), adminContent);

        if (!isAdminPanelVisible) {
            slideAdmin.setToY(0);  // Slide up to show
        } else {
            slideAdmin.setToY(400);  // Slide down to hide
        }

        slideAdmin.play();

        isAdminPanelVisible = !isAdminPanelVisible;
    }

    @FXML
    private void handleAdminLogin() {
        String username = adminUsernameField.getText();
        String password = adminPasswordField.getText();

        // Check for empty fields
        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Username and password must not be empty.", Alert.AlertType.ERROR);
            return;
        }

        // Validate credentials against the database
        if (DatabaseConnection.validateAdminCredentials(username, password)) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("admin_interface.fxml"));
                Stage stage = new Stage();
                stage.setScene(new Scene(loader.load()));
                stage.setTitle("Admin Panel");
                stage.show();

                // Close the current window
                Stage currentStage = (Stage) adminContent.getScene().getWindow();
                currentStage.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // Handle invalid login
            showAlert("Error", "Invalid admin credentials.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleValidateButtonAction() {
        String cin = cinField.getText();
        String key = keyField.getText();

        if (cin.isEmpty() || key.isEmpty()) {
            showAlert("Error", "Both fields must be filled out.", Alert.AlertType.ERROR);
            return;
        }

        int validationResult = validateUser(cin, key);
        if (validationResult == 1) {
            try {
                // Load the new FXML file
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("license.fxml"));
                Parent root = fxmlLoader.load();

                // Get the controller and pass the user data
                LicenseController controller = fxmlLoader.getController();
                int userId = getUserId(cin, key); // Method to get the UserID
                controller.setUserId(userId);

                // Create a new stage (window)
                Stage stage = new Stage();
                stage.setTitle("License Window");
                stage.setScene(new Scene(root));
                stage.show();

                // Close the current window
                Stage currentStage = (Stage) cinField.getScene().getWindow();
                currentStage.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (validationResult == 0) {
            showAlert("Error", "The application is still pending.", Alert.AlertType.ERROR);
        } else {
            showAlert("Error", "Invalid CIN or Code License.", Alert.AlertType.ERROR);
        }
    }

    private int validateUser(String cin, String key) {
        String userQuery = "SELECT UserID FROM users WHERE CarteIdentite = ? AND CodeLicense = ?";
        String applicationQuery = "SELECT STATUS FROM applications WHERE UserID = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement userStmt = connection.prepareStatement(userQuery);
             PreparedStatement applicationStmt = connection.prepareStatement(applicationQuery)) {

            userStmt.setString(1, cin);
            userStmt.setString(2, key);
            ResultSet userResult = userStmt.executeQuery();

            if (userResult.next()) {
                int userId = userResult.getInt("UserID");

                applicationStmt.setInt(1, userId);
                ResultSet applicationResult = applicationStmt.executeQuery();

                if (applicationResult.next()) {
                    int status = applicationResult.getInt("STATUS");
                    return status; // Return the status (0 for pending, 1 for approved)
                } else {
                    return -1; // No matching application found
                }
            } else {
                return -1; // No matching user found
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private int getUserId(String cin, String key) {
        String query = "SELECT UserID FROM users WHERE CarteIdentite = ? AND CodeLicense = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, cin);
            stmt.setString(2, key);
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("UserID");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Return -1 if user not found
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}