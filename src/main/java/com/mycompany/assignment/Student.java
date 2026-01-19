package com.mycompany.assignment;

import java.util.ArrayList;
import java.util.List;

public class Student {

    private String studentId;
    private String name;
    private String password;
    private double credit;
    private List<Order> orders;

    public Student(String studentId, String name, String password, double credit) {
        this.studentId = studentId;
        this.name = name;
        this.password = password;
        this.credit = credit;
        this.orders = new ArrayList<>();
    }

    public String getStudentId() {
        return studentId;
    }

    public String getName() {
        return name;
    }

    public double getCredit() {
        return credit;
    }

    // Pay using wallet credit
    public boolean pay(double amount) {
        if (amount > 0 && credit >= amount) {
            credit -= amount;
            return true;
        }
        return false;
    }

    // Top up wallet
    public void topUp(double amount) {
        if (amount > 0) {
            credit += amount;
        }
    }

    // Order history
    public void addOrder(Order order) {
        orders.add(order);
    }

    public List<Order> getOrders() {
        return orders;
    }

    // Save format for txt file
    public String toFileString() {
        return studentId + "," + name + "," + password + "," + credit;
    }
}
