/*package com.capgemini.entity;


import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.type.TimestampType;

@Entity
public class FriendManagement {


	private Long id;
	private String email;
	private String friend_list;
	private String subscription;
	private String text_message;
	//@Temporal(TemporalType.TIMESTAMP)
	private Date  updated_timestamp;



	

	public FriendManagement(Long id, String email, String friend_list ,String  subscription ,String text_message) {
		super();
		this.id = id;
		this.email= email;
		this.friend_list = friend_list;
		this.subscription = subscription;
		this.text_message = text_message;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id", unique = true, nullable = false)

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = "email", unique = true, nullable = false, length = 10)
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	@Column(name = "friend_list")
	public String getFriend_list() {
		return friend_list;
	}
	public void setFriend_list(String friend_list) {
		this.friend_list = friend_list;
	}
	@Column(name = "subscription")
	public String getSubscription() {
		return subscription;
	}
	public void setSubscription(String subscription) {
		this.subscription = subscription;
	}
	@Column(name = "text_message")
	public String getText_message() {
		return text_message;
	}
	public void setText_message(String text_message) {
		this.text_message = text_message;
	}

	@Temporal(TemporalType.DATE)
	public Date getUpdated_timestamp() {
		return updated_timestamp;
	}

	public void setUpdated_timestamp(Date updated_timestamp) {
		this.updated_timestamp = updated_timestamp;
	}

}
*/