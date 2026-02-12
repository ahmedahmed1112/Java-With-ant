package model;

import util.Constants;

public class Feedback {
    private String feedbackId;
    private String assessmentId;
    private String studentId;
    private String lecturerId;
    private String feedbackText;
    private String dateProvided;

    public Feedback() {
    }

    public Feedback(String feedbackId, String assessmentId, String studentId,
                    String lecturerId, String feedbackText, String dateProvided) {
        this.feedbackId = feedbackId;
        this.assessmentId = assessmentId;
        this.studentId = studentId;
        this.lecturerId = lecturerId;
        this.feedbackText = feedbackText;
        this.dateProvided = dateProvided;
    }

    public String getFeedbackId() { return feedbackId; }
    public void setFeedbackId(String feedbackId) { this.feedbackId = feedbackId; }
    public String getAssessmentId() { return assessmentId; }
    public void setAssessmentId(String assessmentId) { this.assessmentId = assessmentId; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getLecturerId() { return lecturerId; }
    public void setLecturerId(String lecturerId) { this.lecturerId = lecturerId; }
    public String getFeedbackText() { return feedbackText; }
    public void setFeedbackText(String feedbackText) { this.feedbackText = feedbackText; }
    public String getDateProvided() { return dateProvided; }
    public void setDateProvided(String dateProvided) { this.dateProvided = dateProvided; }

    public String toFileString() {
        return String.join(Constants.DELIMITER,
                feedbackId, assessmentId, studentId, lecturerId,
                feedbackText, dateProvided);
    }
}
