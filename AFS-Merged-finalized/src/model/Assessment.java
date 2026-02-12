package model;

import util.Constants;

public class Assessment {
    private String assessmentId;
    private String moduleId;
    private String assessmentName;
    private String assessmentType;
    private double totalMarks;
    private double weightage;
    private String createdBy;

    public Assessment() {
    }

    public Assessment(String assessmentId, String moduleId, String assessmentName,
                      String assessmentType, double totalMarks, double weightage,
                      String createdBy) {
        this.assessmentId = assessmentId;
        this.moduleId = moduleId;
        this.assessmentName = assessmentName;
        this.assessmentType = assessmentType;
        this.totalMarks = totalMarks;
        this.weightage = weightage;
        this.createdBy = createdBy;
    }

    public String getAssessmentId() { return assessmentId; }
    public void setAssessmentId(String assessmentId) { this.assessmentId = assessmentId; }
    public String getModuleId() { return moduleId; }
    public void setModuleId(String moduleId) { this.moduleId = moduleId; }
    public String getAssessmentName() { return assessmentName; }
    public void setAssessmentName(String assessmentName) { this.assessmentName = assessmentName; }
    public String getAssessmentType() { return assessmentType; }
    public void setAssessmentType(String assessmentType) { this.assessmentType = assessmentType; }
    public double getTotalMarks() { return totalMarks; }
    public void setTotalMarks(double totalMarks) { this.totalMarks = totalMarks; }
    public double getWeightage() { return weightage; }
    public void setWeightage(double weightage) { this.weightage = weightage; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String toFileString() {
        return String.join(Constants.DELIMITER,
                assessmentId, moduleId, assessmentName, assessmentType,
                String.valueOf(totalMarks), String.valueOf(weightage), createdBy);
    }

    @Override
    public String toString() {
        return assessmentId + " - " + assessmentName + " (" + assessmentType + ")";
    }
}
