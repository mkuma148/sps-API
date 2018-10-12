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

	
	public String getErrorDescription() {
		return description;
	}

	public void setErrorDescription(String description) {
		this.description = description;
	}
	
	public FriendManagementValidation() {
		
	}
	
	public FriendManagementValidation(String status, String errorDescription) {
		super();
		this.status = status;
		this.description = errorDescription;
	}
	
	
	

	
	
	
}
