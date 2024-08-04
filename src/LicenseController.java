import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class LicenseController {

    @FXML
    private TableView<LicenseData> licenseTable;
    @FXML
    private TableColumn<LicenseData, Integer> applicationIdColumn;
    @FXML
    private TableColumn<LicenseData, Integer> licenseIdColumn; // New column for License ID
    @FXML
    private TableColumn<LicenseData, String> licenseClassColumn;
    @FXML
    private TableColumn<LicenseData, Integer> validityPeriodColumn;

    private int userId;

    public void setUserId(int userId) {
        this.userId = userId;
        loadLicenseData();
    }

    private void loadLicenseData() {
        ObservableList<LicenseData> data = FXCollections.observableArrayList();
        String applicationQuery = "SELECT a.ApplicationID, l.LicenseID, lt.LicenseClass, lt.ValidityPeriod " +
                                  "FROM applications a, licenses l, license_types lt " +
                                  "WHERE a.ApplicationID = l.ApplicationID " +
                                  "AND l.LicenseTypeID = lt.LicenseTypeID " +
                                  "AND a.UserID = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement appStmt = connection.prepareStatement(applicationQuery)) {
            appStmt.setInt(1, userId);
            ResultSet appResultSet = appStmt.executeQuery();
            while (appResultSet.next()) {
                int applicationId = appResultSet.getInt("ApplicationID");
                int licenseId = appResultSet.getInt("LicenseID");
                String licenseClass = appResultSet.getString("LicenseClass");
                int validityPeriod = appResultSet.getInt("ValidityPeriod");
                System.out.println("ApplicationID: " + applicationId + ", LicenseID: " + licenseId + ", LicenseClass: " + licenseClass + ", ValidityPeriod: " + validityPeriod); // Debug statement

                data.add(new LicenseData(applicationId, licenseId, licenseClass, validityPeriod));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        applicationIdColumn.setCellValueFactory(new PropertyValueFactory<>("applicationId"));
        licenseIdColumn.setCellValueFactory(new PropertyValueFactory<>("licenseId"));
        licenseClassColumn.setCellValueFactory(new PropertyValueFactory<>("licenseClass"));
        validityPeriodColumn.setCellValueFactory(new PropertyValueFactory<>("validityPeriod"));
        licenseTable.setItems(data);
    }

    public void insertLicense(int applicationId, int licenseTypeId, String startDate, String expiryDate) {
        String insertQuery = "INSERT INTO licenses (ApplicationID, LicenseTypeID, StartDate, ExpiryDate) VALUES (?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
            insertStmt.setInt(1, applicationId);
            insertStmt.setInt(2, licenseTypeId);
            insertStmt.setString(3, startDate);
            insertStmt.setString(4, expiryDate);
            insertStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static class LicenseData {
        private final int applicationId;
        private final int licenseId;
        private final String licenseClass;
        private final int validityPeriod;

        public LicenseData(int applicationId, int licenseId, String licenseClass, int validityPeriod) {
            this.applicationId = applicationId;
            this.licenseId = licenseId;
            this.licenseClass = licenseClass;
            this.validityPeriod = validityPeriod;
        }

        public int getApplicationId() {
            return applicationId;
        }

        public int getLicenseId() {
            return licenseId;
        }

        public String getLicenseClass() {
            return licenseClass;
        }

        public int getValidityPeriod() {
            return validityPeriod;
        }
    }
}