package model;

import util.Constants;

public class Lecturer extends User {
    private String assignedModuleId;
    private String academicLeaderId;

    public Lecturer() {
        super();
        this.role = "LECTURER";
    }

    public Lecturer(String username, String password, String name) {
        super(username, password, name, "", "", "", 0);
        this.role = "LECTURER";
    }

    public Lecturer(String username, String password, String name, String gender,
                    String email, String phone, int age,
                    String assignedModuleId, String academicLeaderId) {
        super(username, password, name, gender, email, phone, age);
        this.assignedModuleId = assignedModuleId;
        this.academicLeaderId = academicLeaderId;
        this.role = "LECTURER";
    }

    // Full constructor with userId and role for factory
    public Lecturer(String userId, String username, String password, String name,
                    String gender, String email, String phone, int age,
                    String assignedModuleId, String academicLeaderId) {
        super(userId, username, password, name, gender, email, phone, age, "LECTURER");
        this.assignedModuleId = assignedModuleId;
        this.academicLeaderId = academicLeaderId;
    }

    public String getAssignedModuleId() {
        return assignedModuleId;
    }

    public void setAssignedModuleId(String assignedModuleId) {
        this.assignedModuleId = assignedModuleId;
    }

    public String getAcademicLeaderId() {
        return academicLeaderId;
    }

    public void setAcademicLeaderId(String academicLeaderId) {
        this.academicLeaderId = academicLeaderId;
    }

    @Override
    public void editProfile() {
        // Lecturer profile editing handled by UI
    }

    public void designAssessment() {
    }

    public void keyInMarks() {
    }

    public void provideFeedback() {
    }

    public String toFileString() {
        return String.join(Constants.DELIMITER,
                getUsername(), getPassword(), getName(), getGender(),
                getEmail(), getPhone(), String.valueOf(getAge()),
                safe(assignedModuleId), safe(academicLeaderId));
    }

    @Override
    public String toString() {
        return "Lecturer: " + getName() + " | Module: " + assignedModuleId;
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}
