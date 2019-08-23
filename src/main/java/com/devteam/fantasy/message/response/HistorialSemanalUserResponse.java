package com.devteam.fantasy.message.response;

import com.devteam.fantasy.util.TripletUDD;

import java.util.List;

public class HistorialSemanalUserResponse {
    private double balance;
    private List<TripletUDD> uddList;

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public List<TripletUDD> getUddList() {
        return uddList;
    }

    public void setUddList(List<TripletUDD> uddList) {
        this.uddList = uddList;
    }
}
