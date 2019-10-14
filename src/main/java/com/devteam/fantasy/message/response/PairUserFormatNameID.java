package com.devteam.fantasy.message.response;

public class PairUserFormatNameID {

	private Long id;
	
	private String name;

	public PairUserFormatNameID() {
		// TODO Auto-generated constructor stub
	}	
	public PairUserFormatNameID(Long id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
}
