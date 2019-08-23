package com.devteam.fantasy.message.response;

import com.devteam.fantasy.util.PairJB;
import com.devteam.fantasy.util.RiesgoMaximo;

import java.util.List;

public class ResumenSemanaAnteriorResponse {
    private String title;
    private double totalSemana;
    private double comisionesSemana;
    private double entradaNetaSemana;
    private double totalPremio;
    private double balance;

    private List<PairJB> pairJBList;


    public ResumenSemanaAnteriorResponse() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getTotalSemana() {
        return totalSemana;
    }

    public void setTotalSemana(double totalSemana) {
        this.totalSemana = totalSemana;
    }

    public double getComisionesSemana() {
        return comisionesSemana;
    }

    public void setComisionesSemana(double comisionesSemana) {
        this.comisionesSemana = comisionesSemana;
    }

    public double getEntradaNetaSemana() {
        return entradaNetaSemana;
    }

    public void setEntradaNetaSemana(double entradaNetaSemana) {
        this.entradaNetaSemana = entradaNetaSemana;
    }

    public double getTotalPremio() {
        return totalPremio;
    }

    public void setTotalPremio(double totalPremio) {
        this.totalPremio = totalPremio;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public List<PairJB> getPairJBList() {
        return pairJBList;
    }

    public void setPairJBList(List<PairJB> pairJBList) {
        this.pairJBList = pairJBList;
    }
}
