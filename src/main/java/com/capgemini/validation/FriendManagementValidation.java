package com.capgemini.validation;

import org.springframework.stereotype.Component;

@Component
public class FriendManagementValidation {
   
	String status;
	String description;
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	
	
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public FriendManagementValidation() {
		
	}
	
	public FriendManagementValidation(String status, String description) {
		super();
		this.status = status;
		this.description = description;
	}
	
	
	

	
	
	
}
