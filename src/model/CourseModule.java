package model;

import util.Constants;

public class CourseModule {
    private String moduleId;
    private String moduleName;
    private String lecturerId;
    private String leaderId;

    public CourseModule() {
    }

    public CourseModule(String moduleId, String moduleName, String lecturerId) {
        this.moduleId = moduleId;
        this.moduleName = moduleName;
        this.lecturerId = lecturerId;
    }

    public CourseModule(String moduleId, String moduleName, String lecturerId, String leaderId) {
        this.moduleId = moduleId;
        this.moduleName = moduleName;
        this.lecturerId = lecturerId;
        this.leaderId = leaderId;
    }

    public String getModuleId() { return moduleId; }
    public void setModuleId(String moduleId) { this.moduleId = moduleId; }
    public String getModuleName() { return moduleName; }
    public void setModuleName(String moduleName) { this.moduleName = moduleName; }
    public String getLecturerId() { return lecturerId; }
    public void setLecturerId(String lecturerId) { this.lecturerId = lecturerId; }
    public String getLeaderId() { return leaderId; }
    public void setLeaderId(String leaderId) { this.leaderId = leaderId; }

    public String toFileString() {
        return String.join(Constants.DELIMITER, moduleId, moduleName, safe(lecturerId));
    }

    @Override
    public String toString() {
        return moduleId + " - " + moduleName;
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}
