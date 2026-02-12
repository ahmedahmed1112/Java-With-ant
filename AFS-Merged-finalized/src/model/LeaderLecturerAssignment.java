package model;

public class LeaderLecturerAssignment {
    private String leaderId;
    private String lecturerId;

    public LeaderLecturerAssignment(String leaderId, String lecturerId) {
        this.leaderId = leaderId;
        this.lecturerId = lecturerId;
    }

    public String getLeaderId() { return leaderId; }
    public String getLecturerId() { return lecturerId; }

    @Override
    public String toString() {
        return leaderId + "|" + lecturerId;
    }
}
