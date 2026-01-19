package com.mycompany.assignment;

public class Order {

    private String orderId;
    private String studentId;
    private String vendorId;
    private String orderType;   // DINE_IN / TAKEAWAY / DELIVERY
    private String status;      // PENDING / ACCEPTED / CANCELLED
    private double totalAmount;

    public Order(String orderId, String studentId, String vendorId,
                 String orderType, double totalAmount) {
        this.orderId = orderId;
        this.studentId = studentId;
        this.vendorId = vendorId;
        this.orderType = orderType;
        this.totalAmount = totalAmount;
        this.status = "PENDING";
    }

    public String getOrderId() {
        return orderId;
    }

    public String getStatus() {
        return status;
    }

    public void cancelOrder() {
        status = "CANCELLED";
    }

    public void acceptOrder() {
        status = "ACCEPTED";
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    // Save format for txt file
    public String toFileString() {
        return orderId + "," + studentId + "," + vendorId + ","
                + orderType + "," + status + "," + totalAmount;
    }
}
