package com.capgemini.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.capgemini.exceptionhandling.ResourceNotFoundException;
import com.capgemini.model.CommonFriendsListResponse;
import com.capgemini.model.EmailsListRecievesUpdatesResponse;
import com.capgemini.model.Subscriber;
import com.capgemini.model.UserFriendsListResponse;
import com.capgemini.repository.FriendMangmtRepo;
import com.capgemini.validation.FriendManagementValidation;

@Service
public class FrientMangmtService {

	@Autowired
	FriendMangmtRepo friendMangmtRepo;

	@Autowired
	public FrientMangmtService(FriendMangmtRepo friendMangmtRepo) {
		this.friendMangmtRepo = friendMangmtRepo;
	}

	public FriendManagementValidation addNewFriendConnection(com.capgemini.model.UserRequest userReq) {
		FriendManagementValidation fmResponse = friendMangmtRepo.addNewFriendConnection(userReq);
		return fmResponse;
	}

	public FriendManagementValidation subscribeTargetFriend(com.capgemini.model.Subscriber subscriber)
			throws ResourceNotFoundException {

		return friendMangmtRepo.subscribeTargetFriend(subscriber);

	}

	public FriendManagementValidation unSubscribeTargetFriend(Subscriber subscriber) throws ResourceNotFoundException {
		return friendMangmtRepo.unSubscribeTargetFriend(subscriber);
	}

	public UserFriendsListResponse getFriendList(com.capgemini.model.FriendListRequest friendListRequest)
			throws ResourceNotFoundException {

		return friendMangmtRepo.getFriendsList(friendListRequest);

	}

	public CommonFriendsListResponse retrieveCommonFriendList(String email1, String email2)
			throws ResourceNotFoundException {

		return friendMangmtRepo.retrieveCommonFriendList(email1, email2);
	}

	public EmailsListRecievesUpdatesResponse emailListRecievesupdates(
			com.capgemini.model.EmailsListRecievesUpdatesRequest emailsList) throws ResourceNotFoundException {

		return friendMangmtRepo.emailListRecievesupdates(emailsList);
	}

}
