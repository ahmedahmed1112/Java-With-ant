package com.mycompany.assignment;

public class Transaction {

    private String transactionId;
    private String studentId;
    private double amount;
    private String type; // PAYMENT / TOP_UP

    public Transaction(String transactionId, String studentId,
                       double amount, String type) {
        this.transactionId = transactionId;
        this.studentId = studentId;
        this.amount = amount;
        this.type = type;
    }

    public String toFileString() {
        return transactionId + "," + studentId + "," + amount + "," + type;
    }
}
