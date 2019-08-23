package com.devteam.fantasy.util;

public class PairSV {

    private String title;
    private Double balance;


    public PairSV(String title, Double balance) {
        this.title = title;
        this.balance = balance;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }
}
