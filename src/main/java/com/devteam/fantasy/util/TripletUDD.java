package com.devteam.fantasy.util;
/**
 * User date detail
 */

public class TripletUDD {
    private String title;
    private double balance;
    private String date;

    public TripletUDD() {
    }

    public TripletUDD(String title, double balance, String date) {
        this.title = title;
        this.balance = balance;
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
