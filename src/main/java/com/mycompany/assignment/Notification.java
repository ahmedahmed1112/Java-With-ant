package com.mycompany.assignment;

public class Notification {

    private String notificationId;
    private String userId;
    private String message;

    public Notification(String notificationId, String userId, String message) {
        this.notificationId = notificationId;
        this.userId = userId;
        this.message = message;
    }

    public String toFileString() {
        return notificationId + "," + userId + "," + message;
    }
}
