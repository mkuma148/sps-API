package com.capgemini.model;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class CommonFriendsListRequest {

	/*
	 * The API should receive the following JSON request: { “friends”: [
	 * “andy@example.com”, “john@example.com”] }
	 */

	@NotNull
	@NotEmpty
	@Size(max = 4, message = "{friendArraySize.size}")
	private List<@Valid @NotEmpty @NotNull @Email String> friends;

	public List<String> getFriends() {
		return friends;
	}

	public void setFriends(List<String> friends) {
		this.friends = friends;
	}

}
