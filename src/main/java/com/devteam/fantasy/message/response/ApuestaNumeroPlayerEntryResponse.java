package com.devteam.fantasy.message.response;

import java.util.List;

public class ApuestaNumeroPlayerEntryResponse {
    private String name;
    private List<NumeroPlayerEntryResponse> list;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<NumeroPlayerEntryResponse> getList() {
        return list;
    }

    public void setList(List<NumeroPlayerEntryResponse> list) {
        this.list = list;
    }
}
