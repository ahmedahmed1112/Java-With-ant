package model;

import util.Constants;

public class Grade {
    private String gradeId;
    private String assessmentId;
    private String studentId;
    private double marks;
    private String grade;
    private String lecturerId;
    private String dateEntered;

    public Grade() {
    }

    public Grade(String gradeId, String assessmentId, String studentId,
                 double marks, String grade, String lecturerId, String dateEntered) {
        this.gradeId = gradeId;
        this.assessmentId = assessmentId;
        this.studentId = studentId;
        this.marks = marks;
        this.grade = grade;
        this.lecturerId = lecturerId;
        this.dateEntered = dateEntered;
    }

    public String getGradeId() { return gradeId; }
    public void setGradeId(String gradeId) { this.gradeId = gradeId; }
    public String getAssessmentId() { return assessmentId; }
    public void setAssessmentId(String assessmentId) { this.assessmentId = assessmentId; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public double getMarks() { return marks; }
    public void setMarks(double marks) { this.marks = marks; }
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
    public String getLecturerId() { return lecturerId; }
    public void setLecturerId(String lecturerId) { this.lecturerId = lecturerId; }
    public String getDateEntered() { return dateEntered; }
    public void setDateEntered(String dateEntered) { this.dateEntered = dateEntered; }

    public String toFileString() {
        return String.join(Constants.DELIMITER,
                gradeId, assessmentId, studentId,
                String.valueOf(marks), grade, lecturerId, dateEntered);
    }
}
