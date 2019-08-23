package com.devteam.fantasy.message.response;

import java.util.ArrayList;

public class AdicionarNumberResponse {

    String username;

    ArrayList<NumeroPlayerEntryResponse> data;

    public AdicionarNumberResponse() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public ArrayList<NumeroPlayerEntryResponse> getNumeroPlayerEntryResponses() {
        return data;
    }

    public void setNumeroPlayerEntryResponses(ArrayList<NumeroPlayerEntryResponse> numeroPlayerEntryResponses) {
        this.data = numeroPlayerEntryResponses;
    }

    @Override
    public String toString() {
        return "AdicionarNumberResponse{" +
                "username='" + username + '\'' +
                ", data=" + data +
                '}';
    }
}
