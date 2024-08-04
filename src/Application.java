import java.time.LocalDate;

public class Application {
    private int applicationId;
    private int userId;
    private int licenseTypeId;
    private LocalDate applicationDate;
    private String createdBy;
    private double applicationFees;
    private int status;

    public Application(int applicationId, int userId, int licenseTypeId, LocalDate applicationDate, String createdBy, double applicationFees, int status) {
        this.applicationId = applicationId;
        this.userId = userId;
        this.licenseTypeId = licenseTypeId;
        this.applicationDate = applicationDate;
        this.createdBy = createdBy;
        this.applicationFees = applicationFees;
        this.status = status;
    }

    // Getters
    public int getApplicationId() { return applicationId; }
    public int getUserId() { return userId; }
    public int getLicenseTypeId() { return licenseTypeId; }
    public LocalDate getApplicationDate() { return applicationDate; }
    public String getCreatedBy() { return createdBy; }
    public double getApplicationFees() { return applicationFees; }
    public int getStatus() { return status; }
}