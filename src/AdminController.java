import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;


public class AdminController {

    @FXML private Button logoutButton;
    @FXML private TextField applicantNameField;
    @FXML private TextField licenseTypeField;
    @FXML private DatePicker applicationDatePicker;
    @FXML private Label notificationLabel;
    @FXML private ImageView reloadImageView;
    @FXML private TextField filterField;
    @FXML private ComboBox<String> filterTypeComboBox;

    @FXML private TableView<Application> applicationsTable;
    @FXML private TableColumn<Application, Integer> applicationIdColumn;
    @FXML private TableColumn<Application, Integer> userIdColumn;
    @FXML private TableColumn<Application, Integer> licenseTypeIdColumn;
    @FXML private TableColumn<Application, LocalDate> applicationDateColumn;
    @FXML private TableColumn<Application, String> createdByColumn;
    @FXML private TableColumn<Application, Double> applicationFeesColumn;
    @FXML private TableColumn<Application, String> modifyColumn;
    @FXML private TableColumn<Application, Integer> statusColumn;
    @FXML private TableColumn<Application, String> licenseColumn; // New column

    private ObservableList<Application> applicationData = FXCollections.observableArrayList();
    private FilteredList<Application> filteredData;

    @FXML
    private void initialize() {
        applicationIdColumn.setCellValueFactory(new PropertyValueFactory<>("applicationId"));
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        licenseTypeIdColumn.setCellValueFactory(new PropertyValueFactory<>("licenseTypeId"));
        applicationDateColumn.setCellValueFactory(new PropertyValueFactory<>("applicationDate"));
        createdByColumn.setCellValueFactory(new PropertyValueFactory<>("createdBy"));
        applicationFeesColumn.setCellValueFactory(new PropertyValueFactory<>("applicationFees"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        statusColumn.setCellFactory(column -> new TableCell<Application, Integer>() {
            private final Circle circle = new Circle(5);
            private final StackPane stackPane = new StackPane(circle);

            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    circle.setFill(item == 1 ? Color.GREEN : Color.RED);
                    setGraphic(stackPane);
                }
            }
        });

        modifyColumn.setCellFactory(modifyButtonCellFactory());
        licenseColumn.setCellFactory(licenseButtonCellFactory()); // Set cell factory for new column
    
        loadApplicationData();

        // Initialize filtered data
        filteredData = new FilteredList<>(applicationData, p -> true);

        // Bind the SortedList comparator to the TableView comparator
        SortedList<Application> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(applicationsTable.comparatorProperty());

        // Add sorted (and filtered) data to the table
        applicationsTable.setItems(sortedData);
    }

    @FXML
    private void handleFilter() {
        String filterText = filterField.getText().toLowerCase();
        filteredData.setPredicate(application -> {
            if (filterText == null || filterText.isEmpty()) {
                return true;
            }

            // Compare application fields with filter text
            if (String.valueOf(application.getApplicationId()).toLowerCase().contains(filterText)) {
                return true;
            } else if (String.valueOf(application.getUserId()).toLowerCase().contains(filterText)) {
                return true;
            } else if (String.valueOf(application.getLicenseTypeId()).toLowerCase().contains(filterText)) {
                return true;
            } else if (application.getApplicationDate().toString().toLowerCase().contains(filterText)) {
                return true;
            } else if (application.getCreatedBy().toLowerCase().contains(filterText)) {
                return true;
            } else if (String.valueOf(application.getApplicationFees()).toLowerCase().contains(filterText)) {
                return true;
            } else if (String.valueOf(application.getStatus()).toLowerCase().contains(filterText)) {
                return true;
            }
            return false; // Does not match
        });
    }

    public Callback<TableColumn<Application, String>, TableCell<Application, String>> modifyButtonCellFactory() {
        return new Callback<>() {
            @Override
            public TableCell<Application, String> call(final TableColumn<Application, String> param) {
                final TableCell<Application, String> cell = new TableCell<>() {
                    private final Button btn = new Button("Modify");

                    {
                        btn.setOnAction(event -> {
                            Application application = getTableView().getItems().get(getIndex());
                            openModifyWindow(application);
                        });
                    }

                    @Override
                    protected void updateItem(String item, boolean empty) {
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
    }

    public Callback<TableColumn<Application, String>, TableCell<Application, String>> licenseButtonCellFactory() {
        return new Callback<>() {
            @Override
            public TableCell<Application, String> call(final TableColumn<Application, String> param) {
                final TableCell<Application, String> cell = new TableCell<>() {
                    private final Button btn = new Button("Add");

                    {
                        btn.setOnAction(event -> {
                            Application application = getTableView().getItems().get(getIndex());
                            openLicenseWindow(application);
                        });
                    }

                    @Override
                    protected void updateItem(String item, boolean empty) {
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
    }

    private void openModifyWindow(Application application) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("modify_user.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Modify User");

            // Get the controller and pass the user ID and application ID
            ModifyUserController controller = loader.getController();
            controller.loadUserData(application.getUserId(), application.getApplicationId());

            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openLicenseWindow(Application application) {
        System.out.println("New License for Application ID: " + application.getApplicationId());
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("new_license.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("New License");

            // Pass the applicationId to the NewLicenseController
            NewLicenseController controller = loader.getController();
            controller.setApplicationId(application.getApplicationId());

            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadApplicationData() {
        applicationData.clear();
        String query = "SELECT ApplicationID, UserID, LicenseTypeID, ApplicationDate, CreatedBy, ApplicationFees, Status FROM applications";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                applicationData.add(new Application(
                    rs.getInt("ApplicationID"),
                    rs.getInt("UserID"),
                    rs.getInt("LicenseTypeID"),
                    rs.getDate("ApplicationDate").toLocalDate(),
                    rs.getString("CreatedBy"),
                    rs.getDouble("ApplicationFees"),
                    rs.getInt("Status")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        applicationsTable.setItems(applicationData);
    }

    @FXML
    public void handleReload() {
        loadApplicationData();
        notificationLabel.setText("Data reloaded successfully!");
    }

    @FXML
    private void handleMouseEnter() {
        reloadImageView.setOpacity(0.7);
    }

    @FXML
    private void handleMouseExit() {
        reloadImageView.setOpacity(1.0);
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("interface.fxml"));
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Application License");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEmployees() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("employee.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Employee Management");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleInternationalLicense() {
        System.out.println("International License Application");
    }

    @FXML
    private void handleScheduleTest() {
        System.out.println("Schedule Test");
    }

    @FXML
    private void handleNewApplication() {
        System.out.println("New Application");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("new_application.fxml"));
            Stage newStage = new Stage();
            newStage.setScene(new Scene(loader.load()));
            newStage.setTitle("New Application");
            newStage.setResizable(false);
            newStage.initStyle(StageStyle.UTILITY);
            newStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSubmitApplication() {
        String applicantName = applicantNameField.getText();
        String licenseType = licenseTypeField.getText();
        LocalDate applicationDate = applicationDatePicker.getValue();

        System.out.println("Submitting new application:");
        System.out.println("Applicant Name: " + applicantName);
        System.out.println("License Type: " + licenseType);
        System.out.println("Application Date: " + applicationDate);

        // Add logic to save the application to the database

        loadApplicationData();
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        stage.close();
    }
}