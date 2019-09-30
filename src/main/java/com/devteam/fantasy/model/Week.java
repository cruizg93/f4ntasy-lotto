package com.devteam.fantasy.model;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Week {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
	private int year;
	
	private Timestamp monday;
	
	private Timestamp sunday;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public Timestamp getMonday() {
		return monday;
	}

	public void setMonday(Timestamp monday) {
		this.monday = monday;
	}

	public Timestamp getSunday() {
		return sunday;
	}

	public void setSunday(Timestamp sunday) {
		this.sunday = sunday;
	}

	@Override
	public String toString() {
		return "Week [id=" + id + ", year=" + year + ", monday=" + monday + ", sunday=" + sunday + "]";
	}
}
