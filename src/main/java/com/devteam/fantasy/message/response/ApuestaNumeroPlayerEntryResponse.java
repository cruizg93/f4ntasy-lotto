package com.devteam.fantasy.message.response;

import java.util.List;

public class ApuestaNumeroPlayerEntryResponse {
    private String name;
    private List<NumeroPlayerEntryResponse> list;
    private String type;
    private String hour;
    private String day;

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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getHour() {
		return hour;
	}

	public void setHour(String hour) {
		this.hour = hour;
	}

	public String getDay() {
		return day;
	}

	public void setDay(String day) {
		this.day = day;
	}
    
    
}
