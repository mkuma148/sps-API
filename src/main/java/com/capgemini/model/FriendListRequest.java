package com.capgemini.model;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class FriendListRequest {
	
	@NotNull 
	@NotEmpty(message = "{requestorEmail.notempty}") 
	@Email(message = "{requestorEmail.valid}")
	@Size(max = 30, message = "{requestorEmail.size}")
	private String email;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

}
