package com.capgemini.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class FriendMangmtService {

	private final Logger LOG = LoggerFactory.getLogger(getClass());

	@Autowired
	FriendMangmtRepo friendMangmtRepository;

	@Autowired
	public FriendMangmtService(FriendMangmtRepo friendMangmtRepo) {
		this.friendMangmtRepository = friendMangmtRepo;
	}

	public FriendManagementValidation addNewFriendConnection(com.capgemini.model.UserRequest userReq)
			throws ResourceNotFoundException {
		LOG.info(":: In Service class .. addNewFriendConnection()");
		FriendManagementValidation fmResponse = friendMangmtRepository.addNewFriendConnection(userReq);
		return fmResponse;
	}

	public FriendManagementValidation subscribeTargetFriend(com.capgemini.model.Subscriber subscriber)
			throws ResourceNotFoundException {
		LOG.info(":: In Service class .. subscribeTargetFriend()");

		return friendMangmtRepository.subscribeTargetFriend(subscriber);

	}

	public FriendManagementValidation unSubscribeTargetFriend(Subscriber subscriber) throws ResourceNotFoundException {
		LOG.info(":: In Service class .. unSubscribeTargetFriend()");
		return friendMangmtRepository.unSubscribeTargetFriend(subscriber);
	}


	public UserFriendsListResponse getFriendList(com.capgemini.model.FriendListRequest friendListRequest)
			throws ResourceNotFoundException {
		LOG.info(":: In Service class .. getFriendList()");

		return friendMangmtRepository.getFriendsList(friendListRequest);

	}

	public CommonFriendsListResponse retrieveCommonFriendList(final String email1, final String email2)
			throws ResourceNotFoundException {
		LOG.info(":: In Service class .. retrieveCommonFriendList()");

		return friendMangmtRepository.retrieveCommonFriendList(email1, email2);
	}

	public EmailsListRecievesUpdatesResponse emailListRecievesupdates(
			com.capgemini.model.EmailsListRecievesUpdatesRequest emailsList) throws ResourceNotFoundException {
		LOG.info(":: In Service class .. EmailsListRecievesUpdatesRequest()");

		return friendMangmtRepository.emailListRecievesupdates(emailsList);
	}

}