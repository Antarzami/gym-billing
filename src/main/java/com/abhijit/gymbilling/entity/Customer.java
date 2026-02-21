package com.abhijit.gymbilling.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String phoneNumber;
    private Double totalAmount = 0.0;
    private Double paidAmount = 0.0;
    private String status = "UNPAID";
    private LocalDate billingDate;
    private LocalDate dueDate;

    // 🔥 NEW FIELD
    private boolean reminderSent = false;

    public Customer() {}

    // GETTERS
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getPhoneNumber() { return phoneNumber; }
    public Double getTotalAmount() { return totalAmount; }
    public Double getPaidAmount() { return paidAmount; }
    public String getStatus() { return status; }
    public LocalDate getBillingDate() { return billingDate; }
    public LocalDate getDueDate() { return dueDate; }
    public boolean isReminderSent() { return reminderSent; }

    // SETTERS
    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
    public void setPaidAmount(Double paidAmount) { this.paidAmount = paidAmount; }
    public void setStatus(String status) { this.status = status; }
    public void setBillingDate(LocalDate billingDate) { this.billingDate = billingDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public void setReminderSent(boolean reminderSent) { this.reminderSent = reminderSent; }
}
