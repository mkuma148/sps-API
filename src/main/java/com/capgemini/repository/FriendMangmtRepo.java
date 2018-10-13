package com.capgemini.repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import static com.capgemini.config.FriendMgntConstants.*;
import com.capgemini.exceptionhandling.ResourceNotFoundException;
import com.capgemini.model.CommonFriendsListResponse;
import com.capgemini.model.EmailsListRecievesUpdatesResponse;
import com.capgemini.model.UserFriendsListResponse;
import com.capgemini.validation.FriendManagementValidation;

@Repository
public class FriendMangmtRepo {

	private final Logger LOG = LoggerFactory.getLogger(getClass());

	@Autowired
	FriendManagementValidation friendMgmtValidation;
	
	@Autowired
	friendManagementHelper friendManagementHelper;

	@Autowired
	public FriendMangmtRepo(FriendManagementValidation fmError) {
		this.friendMgmtValidation = fmError;
	}

	@Autowired
	NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	/**
	 * This API is invoked to add connection between two email
	 * @param userReq
	 * @return
	 */
	public FriendManagementValidation addNewFriendConnection(final com.capgemini.model.UserRequest userReq)
			throws ResourceNotFoundException {
		try {

			final String requestor = userReq.getRequestor();
			final String target = userReq.getTarget();

			List<String> emails = friendManagementHelper.getListOfAllEmails();

			friendMgmtValidation.setStatus(FRIEND_MANAGEMENT_SUCCESS);
			friendMgmtValidation.setDescription(FRIEND_MANAGEMENT_SUCCESSFULLY_CONNECTED);
			if (requestor.equals(target)) {
				friendMgmtValidation.setStatus(FRIEND_MANAGEMENT_FAILED);
				friendMgmtValidation.setDescription(FRIEND_MANAGEMENT_EMAIL_SAME);
				return friendMgmtValidation;
			}

			if (emails.contains(requestor) && emails.contains(target)) {

				boolean isBlocked = friendManagementHelper.isBlocked(requestor, target);
				if (!isBlocked) {
					if (friendManagementHelper.isAlreadyFriend(requestor, target)) {
						friendMgmtValidation.setStatus(FRIEND_MANAGEMENT_FAILED);
						friendMgmtValidation.setDescription(FRIEND_MANAGEMENT_ALREADY_FRIEND);
					} else {
						friendManagementHelper.connectFriend(requestor, target);
						friendManagementHelper.connectFriend(target, requestor);
					}
				} else {
					friendMgmtValidation.setStatus(FRIEND_MANAGEMENT_FAILED);
					friendMgmtValidation.setDescription(FRIEND_MANAGEMENT_TARGET_BLOCKED);
				}
			} else if (!emails.contains(requestor) && !emails.contains(target)) {
				friendManagementHelper.createNewFriend(requestor);
				friendManagementHelper.createNewFriend(target);
				friendManagementHelper.connectFriend(requestor, target);
				friendManagementHelper.connectFriend(target, requestor);
			} else if (emails.contains(requestor)) {
				friendManagementHelper.createNewFriend(target);
				friendManagementHelper.connectFriend(requestor, target);
				friendManagementHelper.connectFriend(target, requestor);
			} else {
				friendManagementHelper.createNewFriend(requestor);
				friendManagementHelper.connectFriend(requestor, target);
				friendManagementHelper.connectFriend(target, requestor);
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOG.debug(e.getLocalizedMessage());
		}

		return friendMgmtValidation;

	}

	/**
	 * This API is invoked to get the list of all the friends connected to requestor
	 * friend
	 * @param email
	 * @return
	 * @throws ResourceNotFoundException
	 */
	public UserFriendsListResponse getMutualFriends(com.capgemini.model.FriendListRequest friendListRequest)
			throws ResourceNotFoundException {
		
		UserFriendsListResponse emailListresponse = new UserFriendsListResponse();
		final List<String> emails = friendManagementHelper.getListOfAllEmails();
		if(emails!=null && emails.contains(friendListRequest.getEmail())) {
			LOG.info(":: Friend List " + friendListRequest.getEmail());
			String friendList = friendManagementHelper.getFriendList(friendListRequest.getEmail());
			if ("".equals(friendList) || friendList == null) {
				emailListresponse.setStatus(FRIEND_MANAGEMENT_FAILED);
				emailListresponse.setCount(0);
			} else {
				String[] friendListQueryParam = friendList.split(FRIEND_MANAGEMENT_COMMA);
				List<String> friends = friendManagementHelper.getEmailByIds(Arrays.asList(friendListQueryParam));
				if (friends.size() == 0) {

				} else {
					emailListresponse.setStatus(FRIEND_MANAGEMENT_SUCCESS);
					emailListresponse.setCount(friends.size());
					for (String friend : friends) {
						emailListresponse.getFriends().add(friend);
					}
				}
			}
		}else {
			friendMgmtValidation.setStatus(FRIEND_MANAGEMENT_FAILED);
			friendMgmtValidation.setDescription(FRIEND_MANAGEMENT_EMAIL_NOT_EXISTS);
		}
		return emailListresponse;

	}

	/**
	 * This API is invoked to get the common friends between two friends
	 * @param email1
	 * @param email2
	 * @return
	 */
	public CommonFriendsListResponse retrieveCommonFriendList(String email1, String email2)
			throws ResourceNotFoundException {
		CommonFriendsListResponse commonFrndListresponse = new CommonFriendsListResponse();

		List<String> emails = friendManagementHelper.getListOfAllEmails();
		if(emails.contains(email1) && emails.contains(email2)) {

			String friendList1 = friendManagementHelper.getFriendList(email1);
			String friendList2 = friendManagementHelper.getFriendList(email2);

			String[] friendList1Container = friendList1.split(FRIEND_MANAGEMENT_COMMA);
			String[] friendList2Container = friendList2.split(FRIEND_MANAGEMENT_COMMA);

			Set<String> friend1Set = new HashSet<String>(Arrays.asList(friendList1Container));
			Set<String> friend2Set = new HashSet<String>(Arrays.asList(friendList2Container));

			friend1Set.retainAll(friend2Set);

			List<String> friends = friendManagementHelper.getEmailByIds(new ArrayList<String>(friend1Set));
			if (friends.size() == 0) {
				commonFrndListresponse.setStatus(FRIEND_MANAGEMENT_FAILED);
			} else {
				commonFrndListresponse.setStatus(FRIEND_MANAGEMENT_SUCCESS);
				commonFrndListresponse.setCount(friends.size());
				for (String friend : friends) {
					commonFrndListresponse.getFriends().add(friend);
				}
			}
		}else {
			commonFrndListresponse.setStatus(FRIEND_MANAGEMENT_FAILED);
		}
		return commonFrndListresponse;
	}

	/**
	 * This API is invoked to subscribe to updates from an email address
	 * @param subscriber
	 * @return
	 * @throws ResourceNotFoundException
	 */
	public FriendManagementValidation subscribeFriend(com.capgemini.model.Subscriber subscriber)
			throws ResourceNotFoundException {

		final String requestor = subscriber.getRequestor();
		final String target = subscriber.getTarget();

		List<String> emails = friendManagementHelper.getListOfAllEmails();

		if (requestor.equals(target)) {
			friendMgmtValidation.setStatus(FRIEND_MANAGEMENT_FAILED);
			friendMgmtValidation.setDescription(FRIEND_MANAGEMENT_EMAIL_SAME);
			return friendMgmtValidation;
		}
		friendMgmtValidation.setStatus(FRIEND_MANAGEMENT_SUCCESS);
		friendMgmtValidation.setDescription(FRIEND_MANAGEMENT_SUCCESSFULLY_SUBSCRIBED);
		boolean isBlocked = friendManagementHelper.isBlocked(requestor, target);
		if (!isBlocked) {
			if (emails.contains(target) && emails.contains(requestor)) {
				String subscribers = friendManagementHelper.getSubscriptionList(requestor);
				String targetId = friendManagementHelper.getId(target);
				if (subscribers.isEmpty()) {
					friendManagementHelper.updateQueryForSubscriber(targetId, requestor);

					friendManagementHelper.updateSubscribedBy(requestor, target);

				} else {
					String[] subs = subscribers.split(FRIEND_MANAGEMENT_COMMA);
					ArrayList<String> al = new ArrayList<String>(Arrays.asList(subs));

					if (!al.contains(targetId)) {
						targetId = subscribers + FRIEND_MANAGEMENT_COMMA + targetId;
						friendManagementHelper.updateQueryForSubscriber(targetId, requestor);

						friendManagementHelper.updateSubscribedBy(requestor, target);

					} else {
						friendMgmtValidation.setStatus(FRIEND_MANAGEMENT_FAILED);
						friendMgmtValidation.setDescription(FRIEND_MANAGEMENT_TARGET_SUBSCRIBED);
					}
				}

			} else {
				friendMgmtValidation.setStatus(FRIEND_MANAGEMENT_FAILED);
				friendMgmtValidation.setDescription(FRIEND_MANAGEMENT_CHECK_EMAIL);
			}
		} else {
			friendMgmtValidation.setStatus(FRIEND_MANAGEMENT_FAILED);
			friendMgmtValidation.setDescription(FRIEND_MANAGEMENT_TARGET_BLOCKED);
		}
		return friendMgmtValidation;
	}


	/**
	 * This API is invoked to unsubscribe and remove id from subscribe and
	 * subscribedBy column
	 * @param subscriber
	 * @return
	 * @throws ResourceNotFoundException
	 */
	public FriendManagementValidation unSubscribeTargetFriend(final com.capgemini.model.Subscriber subscriber)
			throws ResourceNotFoundException {
		final String requestor = subscriber.getRequestor();
		final String target = subscriber.getTarget();

		List<String> emails = friendManagementHelper.getListOfAllEmails();

		if (emails.contains(requestor) && emails.contains(target)) {
			String subscribers = friendManagementHelper.getSubscriptionList(requestor);

			if (subscribers == null || subscribers.isEmpty()) {
				friendMgmtValidation.setStatus(FRIEND_MANAGEMENT_FAILED);
				friendMgmtValidation.setDescription(FRIEND_MANAGEMENT_REQUESTOR_NOT_SUBSCRIBED);
			} else {
				String[] subs = subscribers.split(FRIEND_MANAGEMENT_COMMA);
				ArrayList<String> subscriberList = new ArrayList<>(Arrays.asList(subs));
				String targetId = friendManagementHelper.getId(target);
				if (subscriberList.contains(targetId)) {
					StringJoiner sjTarget = new StringJoiner(FRIEND_MANAGEMENT_COMMA);
					for (String sub : subscriberList) {
						if (!sub.equals(targetId)) {
							sjTarget.add(sub);
						}
					}
					friendManagementHelper.updateQueryForSubscriber(sjTarget.toString(), requestor);

					final String subscribedBys = friendManagementHelper.getSubscribedByList(target);
					String[] subscribedBy = subscribedBys.split(FRIEND_MANAGEMENT_COMMA);
					ArrayList<String> subscribedByList = new ArrayList<>(Arrays.asList(subscribedBy));
					String requestorId = friendManagementHelper.getId(requestor);
					if (subscribedByList.contains(requestorId)) {
						StringJoiner sjRequestor = new StringJoiner(FRIEND_MANAGEMENT_COMMA);
						for (String sub : subscribedByList) {
							if (!sub.equals(requestorId)) {
								sjRequestor.add(sub);
							}
						}
						friendManagementHelper.updateQueryForSubscribedBy(sjRequestor.toString(), target);
					}

					friendManagementHelper.updateUnsubscribeTable(requestor, target, FRIEND_MANAGEMENT_BLOCKED);

					friendMgmtValidation.setStatus(FRIEND_MANAGEMENT_SUCCESS);
					friendMgmtValidation.setDescription(FRIEND_MANAGEMENT_SUCCESSFULLY_UNSUBSCRIBED);
				} else {
					friendMgmtValidation.setStatus(FRIEND_MANAGEMENT_FAILED);
					friendMgmtValidation.setDescription(FRIEND_MANAGEMENT_NO_TARGET);
				}
			}
		} else {
			friendMgmtValidation.setStatus(FRIEND_MANAGEMENT_FAILED);
			friendMgmtValidation.setDescription(FRIEND_MANAGEMENT_INVALID_EMAIL);
		}
		return friendMgmtValidation;
	}


	/**
	 * This API is invoked to retrive all email address that can receive updates
	 * from an email address
	 * @param emailsList
	 * @return
	 */
	public EmailsListRecievesUpdatesResponse emailListRecievesupdates(
			final com.capgemini.model.EmailsListRecievesUpdatesRequest emailsList) throws ResourceNotFoundException {

		EmailsListRecievesUpdatesResponse EmailsList = new EmailsListRecievesUpdatesResponse();

		List<String> emails = friendManagementHelper.getListOfAllEmails();
		String sender = emailsList.getSender();
		String text = emailsList.getText();
		text = text.trim();
		String reciever = text.substring(text.lastIndexOf(' ') + 1).substring(1);

		if (emails.contains(sender)) {
			if (emails.contains(reciever)) {
				String recieverId = friendManagementHelper.getId(reciever);
				friendManagementHelper.updateQueryForUpdated(recieverId, sender);
			} else {
				friendManagementHelper.createNewFriend(reciever);
				String recieverId = friendManagementHelper.getId(reciever);
				friendManagementHelper.updateQueryForUpdated(recieverId, sender);
			}

			String friendList = friendManagementHelper.getFriendList(sender);
			String[] senderFriends = friendList.split(FRIEND_MANAGEMENT_COMMA);
			LOG.info("senderFriends "+senderFriends[0]);

			String subscribedBy = friendManagementHelper.getSubscribedByList(sender);
			String[] subscribedFriends = subscribedBy.split(FRIEND_MANAGEMENT_COMMA);
			LOG.info("subscribedFriends "+subscribedFriends[0]);

			Set<String> set = new HashSet<String>();
			if(senderFriends[0].equals("") && subscribedFriends[0].equals("")) {

			}else if(senderFriends[0].equals("")) {
				set.addAll(Arrays.asList(subscribedFriends));
			}else if(subscribedFriends[0].equals("")){
				set.addAll(Arrays.asList(senderFriends));
			}else {
				set.addAll(Arrays.asList(senderFriends));
				set.addAll(Arrays.asList(subscribedFriends));
			}

			List<String> emailsUnion = new ArrayList<String>(set);
			List<String> commonEmails = friendManagementHelper.getEmailByIds(emailsUnion);

			if (!commonEmails.contains(reciever)) {
				commonEmails.add(reciever);
			}

			EmailsList.setStatus(FRIEND_MANAGEMENT_SUCCESS);
			for (String email : commonEmails) {
				EmailsList.getFriends().add(email);
			}
		} else {
			EmailsList.setStatus(FRIEND_MANAGEMENT_FAILED);
		}
		return EmailsList;
	}

}