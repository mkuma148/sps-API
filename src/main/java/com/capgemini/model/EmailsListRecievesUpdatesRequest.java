package com.capgemini.model;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class EmailsListRecievesUpdatesRequest {

	@NotNull 
	@NotEmpty(message = "{senderEmail.notempty}") 
	@Email(message = "{senderEmail.valid}")
    @Size(max = 30, message = "{senderEmail.size}")
	private String sender;
	@NotNull
	@NotEmpty(message = "{message.notempty}")
	private String text;
	
	public String getSender() {
		return sender;
	}
	public void setSender(String sender) {
		this.sender = sender;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	
	
}
