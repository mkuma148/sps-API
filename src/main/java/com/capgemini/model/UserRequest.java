package com.capgemini.model;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class UserRequest {
	
	   
	/*@NotNull
	@NotEmpty(message = "{friendArray.notempty}")
	@Size(max = 4, message = "{friendArraySize.size}")
	//private List<@Email @NotNull @NotEmpty String> friends;
	private List<UserEmail> friends;
	

	public List<UserEmail> getFriends() {
		return friends;
	}

	public void setFriends(List<UserEmail> friends) {
		this.friends = friends;
	}*/

	
	@NotNull 
	@NotEmpty(message = "{requestorEmail.notempty}") 
	@Email(message = "{requestorEmail.valid}")
	@Size(max = 30, message = "{requestorEmail.size}")
	String requestor;

	@NotNull 
	@NotEmpty(message = "{targetEmail.notempty}") 
	@Email(message = "{targetEmail.valid}")
	@Size(max = 30, message = "{targetEmail.size}")
	String target;


	
	public String getRequestor() {
		return requestor;
	}
	public void setRequestor(String requestor) {
		this.requestor = requestor;
	}
	public String getTarget() {
		return target;
	}
	public void setTarget(String target) {
		this.target = target;
	}
	

	
	
	
	

}
