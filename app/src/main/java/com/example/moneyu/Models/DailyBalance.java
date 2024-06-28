package com.example.moneyu.Models;

public class DailyBalance {
    private String day;
    private double balance;
    private String date;

    public DailyBalance(String day, double balance) {
        this.day = day;
        this.balance = balance;
    }

    public DailyBalance() {

    }

    public void setDay(String day) {
        this.day = day;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getDay() {
        return day;
    }

    public double getBalance() {
        return balance;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
    }
}