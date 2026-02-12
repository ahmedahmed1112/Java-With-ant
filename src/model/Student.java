package model;

import util.Constants;

public class Student extends User {
    private String studentId;
    private String intake;
    private String moduleId;

    public Student() {
        super();
        this.role = "STUDENT";
    }

    // Simple 3-param constructor (for lecturer's student list)
    public Student(String studentId, String name, String moduleId) {
        super();
        this.studentId = studentId;
        this.setName(name);
        this.moduleId = moduleId;
        this.role = "STUDENT";
    }

    // Full constructor from students.txt
    public Student(String username, String password, String name, String gender,
                   String email, String phone, int age,
                   String studentId, String intake, String moduleId) {
        super(username, password, name, gender, email, phone, age);
        this.studentId = studentId;
        this.intake = intake;
        this.moduleId = moduleId;
        this.role = "STUDENT";
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getIntake() {
        return intake;
    }

    public void setIntake(String intake) {
        this.intake = intake;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    @Override
    public void editProfile() {
        // Student profile editing handled by UI
    }

    public String toFileString() {
        return String.join(Constants.DELIMITER,
                safe(getUsername()), safe(getPassword()), safe(getName()), safe(getGender()),
                safe(getEmail()), safe(getPhone()), String.valueOf(getAge()),
                safe(studentId), safe(intake), safe(moduleId));
    }

    @Override
    public String toString() {
        return safe(studentId) + " - " + safe(getName());
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}
