package com.capgemini.repository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
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
	JdbcTemplate jdbcTemplate;

	@Autowired
	public FriendMangmtRepo(FriendManagementValidation fmError, JdbcTemplate jdbcTemplate) {
		this.friendMgmtValidation = fmError;
		this.jdbcTemplate = jdbcTemplate;
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

			List<String> emails = getListOfAllEmails();

			friendMgmtValidation.setStatus(FRIEND_MANAGEMENT_SUCCESS);
			friendMgmtValidation.setDescription(FRIEND_MANAGEMENT_SUCCESSFULLY_CONNECTED);
			if (requestor.equals(target)) {
				friendMgmtValidation.setStatus(FRIEND_MANAGEMENT_FAILED);
				friendMgmtValidation.setDescription(FRIEND_MANAGEMENT_EMAIL_SAME);
				return friendMgmtValidation;
			}

			if (emails.contains(requestor) && emails.contains(target)) {

				boolean isBlocked = isBlocked(requestor, target);
				if (!isBlocked) {
					if (isAlreadyFriend(requestor, target)) {
						friendMgmtValidation.setStatus(FRIEND_MANAGEMENT_FAILED);
						friendMgmtValidation.setDescription(FRIEND_MANAGEMENT_ALREADY_FRIEND);
					} else {
						connectFriend(requestor, target);
						connectFriend(target, requestor);
					}
				} else {
					friendMgmtValidation.setStatus(FRIEND_MANAGEMENT_FAILED);
					friendMgmtValidation.setDescription(FRIEND_MANAGEMENT_TARGET_BLOCKED);
				}
			} else if (!emails.contains(requestor) && !emails.contains(target)) {
				insertEmail(requestor);
				insertEmail(target);
				connectFriend(requestor, target);
				connectFriend(target, requestor);
			} else if (emails.contains(requestor)) {
				insertEmail(target);
				connectFriend(requestor, target);
				connectFriend(target, requestor);
			} else {
				insertEmail(requestor);
				connectFriend(requestor, target);
				connectFriend(target, requestor);
			}
		} catch (Exception e) {
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
	public UserFriendsListResponse getFriendsList(com.capgemini.model.FriendListRequest friendListRequest)
			throws ResourceNotFoundException {

		List<String> emails = getListOfAllEmails();
		UserFriendsListResponse emailListresponse = new UserFriendsListResponse();
		if(emails.contains(friendListRequest.getEmail())) {
			LOG.info("----getFriendList-----" + friendListRequest.getEmail());
			String friendList = getFriendList(friendListRequest.getEmail());
			if ("".equals(friendList) || friendList == null) {
				emailListresponse.setStatus(FRIEND_MANAGEMENT_FAILED);
				emailListresponse.setCount(0);
			} else {
				String[] friendListQueryParam = friendList.split(",");
				List<String> friends = getEmailByIds(Arrays.asList(friendListQueryParam));
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

		List<String> emails = getListOfAllEmails();
		if(emails.contains(email1) && emails.contains(email2)) {

			String friendList1 = getFriendList(email1);
			String friendList2 = getFriendList(email2);

			String[] friendList1Container = friendList1.split(",");
			String[] friendList2Container = friendList2.split(",");

			Set<String> friend1Set = new HashSet<String>(Arrays.asList(friendList1Container));
			Set<String> friend2Set = new HashSet<String>(Arrays.asList(friendList2Container));

			friend1Set.retainAll(friend2Set);

			List<String> friends = getEmailByIds(new ArrayList<String>(friend1Set));
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
	public FriendManagementValidation subscribeTargetFriend(com.capgemini.model.Subscriber subscriber)
			throws ResourceNotFoundException {

		final String requestor = subscriber.getRequestor();
		final String target = subscriber.getTarget();

		List<String> emails = getListOfAllEmails();

		if (requestor.equals(target)) {
			friendMgmtValidation.setStatus(FRIEND_MANAGEMENT_FAILED);
			friendMgmtValidation.setDescription(FRIEND_MANAGEMENT_EMAIL_SAME);
			return friendMgmtValidation;
		}
		friendMgmtValidation.setStatus(FRIEND_MANAGEMENT_SUCCESS);
		friendMgmtValidation.setDescription(FRIEND_MANAGEMENT_SUCCESSFULLY_SUBSCRIBED);
		boolean isBlocked = isBlocked(requestor, target);
		if (!isBlocked) {
			if (emails.contains(target) && emails.contains(requestor)) {
				String subscribers = getSubscriptionList(requestor);
				String targetId = getId(target);
				if (subscribers.isEmpty()) {
					updateQueryForSubscriber(targetId, requestor);

					updateSubscribedBy(requestor, target);

				} else {
					String[] subs = subscribers.split(",");
					ArrayList<String> al = new ArrayList<String>(Arrays.asList(subs));

					if (!al.contains(targetId)) {
						targetId = subscribers + "," + targetId;
						updateQueryForSubscriber(targetId, requestor);

						updateSubscribedBy(requestor, target);

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

		List<String> emails = getListOfAllEmails();

		if (emails.contains(requestor) && emails.contains(target)) {
			String subscribers = getSubscriptionList(requestor);

			if (subscribers == null || subscribers.isEmpty()) {
				friendMgmtValidation.setStatus(FRIEND_MANAGEMENT_FAILED);
				friendMgmtValidation.setDescription(FRIEND_MANAGEMENT_REQUESTOR_NOT_SUBSCRIBED);
			} else {
				String[] subs = subscribers.split(",");
				ArrayList<String> subscriberList = new ArrayList<>(Arrays.asList(subs));
				String targetId = getId(target);
				if (subscriberList.contains(targetId)) {
					StringJoiner sjTarget = new StringJoiner(",");
					for (String sub : subscriberList) {
						if (!sub.equals(targetId)) {
							sjTarget.add(sub);
						}
					}
					updateQueryForSubscriber(sjTarget.toString(), requestor);

					final String subscribedBys = getSubscribedByList(target);
					String[] subscribedBy = subscribedBys.split(",");
					ArrayList<String> subscribedByList = new ArrayList<>(Arrays.asList(subscribedBy));
					String requestorId = getId(requestor);
					if (subscribedByList.contains(requestorId)) {
						StringJoiner sjRequestor = new StringJoiner(",");
						for (String sub : subscribedByList) {
							if (!sub.equals(requestorId)) {
								sjRequestor.add(sub);
							}
						}
						updateQueryForSubscribedBy(sjRequestor.toString(), target);
					}

					updateUnsubscribeTable(requestor, target, FRIEND_MANAGEMENT_BLOCKED);

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

		List<String> emails = getListOfAllEmails();
		String sender = emailsList.getSender();
		String text = emailsList.getText();
		text = text.trim();
		String reciever = text.substring(text.lastIndexOf(' ') + 1).substring(1);

		if (emails.contains(sender)) {
			if (emails.contains(reciever)) {
				String recieverId = getId(reciever);
				updateQueryForUpdated(recieverId, sender);
			} else {
				insertEmail(reciever);
				String recieverId = getId(reciever);
				updateQueryForUpdated(recieverId, sender);
			}

			String friendList = getFriendList(sender);
			String[] senderFriends = friendList.split(",");
			LOG.info("senderFriends "+senderFriends[0]);

			String subscribedBy = getSubscribedByList(sender);
			String[] subscribedFriends = subscribedBy.split(",");
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
			List<String> commonEmails = getEmailByIds(emailsUnion);

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



	/**
	 * This method is invoked to insert new record in a friendmanagement table
	 * @param email
	 * @return
	 */
	private int insertEmail(final String email) {
		return jdbcTemplate.update(
				"insert into friendmanagement(email, friend_list, subscriber, subscribedBy, updated, updated_timestamp) values(?,?,?,?,?,?)",
				new Object[] { email, "", "", "", "", new Timestamp((new Date()).getTime()) });
	}

	/**
	 * This method is used to get the all the email by ID
	 * @param friendListQueryParam
	 * @return
	 */
	private List<String> getEmailByIds(final List<String> friendListQueryParam) {

		StringJoiner email_Ids = new StringJoiner(",", "SELECT email FROM friendmanagement WHERE id in (", ")");

		for (String friendId : friendListQueryParam) {
			email_Ids.add(friendId);
		}
		String query = email_Ids.toString();
		return (List<String>) jdbcTemplate.queryForList(query, new Object[] {}, String.class);
	}

	/**
	 * This method is used to get all the subscriber for an email
	 * @param email
	 * @return
	 */
	private String getSubscriptionList(final String email) {
		String friendList="";
		String sqlrFriendList = "SELECT subscriber FROM friendmanagement WHERE email=?";
		friendList = (String) jdbcTemplate.queryForObject(sqlrFriendList, new Object[] { email }, String.class);
		return friendList;
	}

	/**
	 * This method is used to get the subscribedBy Ids for a particular email
	 * @param email
	 * @return
	 */
	private String getSubscribedByList(final String email) {
		String friendList="";
		try {
			String sqlrFriendList = "SELECT subscribedBy FROM friendmanagement WHERE email=?";
			friendList = (String) jdbcTemplate.queryForObject(sqlrFriendList, new Object[] { email }, String.class);
		}catch(Exception e) {
			LOG.debug("Exeption raised "+e.getMessage());
		}
		return friendList;
	}

	/**
	 * This method is invoked to connect friend
	 * @param firstEmail
	 * @param secondEmail
	 */
	private void connectFriend(final String firstEmail, final String secondEmail) throws ResourceNotFoundException{
		String requestorId = getId(firstEmail);
		String friendList = getFriendList(secondEmail);

		friendList = friendList.isEmpty() ? requestorId : friendList + "," + requestorId;

		jdbcTemplate.update("update friendmanagement " + " set friend_list = ?" + " where email = ?",
				new Object[] { friendList, secondEmail });
	}

	/**
	 * This method is invoked to check whether the friend is already connected
	 * @param requestor
	 * @param target
	 * @return
	 */
	private boolean isAlreadyFriend(final String requestor, final String target) throws ResourceNotFoundException{
		boolean alreadyFriend = false;

		String requestorId = getId(requestor);
		String targetId = getId(target);

		String requestorFriendList = getFriendList(requestor);
		String[] requestorFriends = requestorFriendList.split(",");

		String targetFirendList = getFriendList(target);
		String[] targetFriends = targetFirendList.split(",");

		if (Arrays.asList(requestorFriends).contains(targetId) && Arrays.asList(targetFriends).contains(requestorId)) {
			alreadyFriend = true;
		}
		LOG.info("alreadyFriend ... " + alreadyFriend);
		return alreadyFriend;

	}

	/**
	 * this method is invoked to get Id of particular email
	 * @param email
	 * @return
	 */
	private String getId(final String email) {
		final String sql = "SELECT id FROM friendmanagement WHERE email=?";
		final String requestorId = (String) jdbcTemplate.queryForObject(sql, new Object[] { email }, String.class);
		return requestorId;
	}

	/**
	 * This method is invoked to get the list of friends
	 * @param email
	 * @return
	 */
	private String getFriendList(final String email) throws ResourceNotFoundException{
		String friendList="";
		try {
			String sqlrFriendList = "SELECT friend_list FROM friendmanagement WHERE email=?";
			friendList = (String) jdbcTemplate.queryForObject(sqlrFriendList, new Object[] { email }, String.class);
		}catch(Exception e) {
			LOG.debug("Exeption raised "+e.getMessage());
		}
		return friendList;

	}

	/**
	 * This method is invoked to check whether the target is blocked or not
	 * @param requestor_email
	 * @param target_email
	 * @return
	 */
	private boolean isBlocked(final String requestor_email, final String target_email) {
		boolean status = false;
		try {
			final String sqlrFriendList = "SELECT Subscription_Status FROM unsubscribe WHERE Requestor_email=? AND Target_email=?";
			final String Subscription_Status = (String) jdbcTemplate.queryForObject(sqlrFriendList,
					new Object[] { requestor_email, target_email }, String.class);
			LOG.info(":: Subscription_Status " + Subscription_Status);
			if (Subscription_Status.equalsIgnoreCase("Blocked")) {
				status = true;
			}
		} catch (Exception e) {
			LOG.debug(e.getLocalizedMessage());

		}
		return status;
	}


	/**
	 * This method is used to update unsubscribed table
	 * @param requestor
	 * @param target
	 * @param status
	 */
	private void updateUnsubscribeTable(final String requestor, final String target, String status) {
		jdbcTemplate.update(
				"insert into UNSUBSCRIBE(Requestor_email, Target_email, Subscription_Status) values(?, ?, ?)",
				new Object[] { requestor, target, status });
	}


	/**
	 * This method is used to update the subscribedBy column
	 * @param requestor
	 * @param target
	 */
	private void updateSubscribedBy(final String requestor, final String target) {
		String requestorId = getId(requestor);
		String subscribedList = getSubscribedByList(target);
		if (subscribedList.isEmpty()) {
			updateQueryForSubscribedBy(requestorId, target);
		} else {
			String[] subscr = subscribedList.split(",");
			ArrayList<String> subscrList = new ArrayList<String>(Arrays.asList(subscr));

			if (!subscrList.contains(requestorId)) {
				requestorId = subscribedList + "," + requestorId;
				updateQueryForSubscribedBy(requestorId, target);
			}
		}
	}


	/**
	 * This method is used to update subscriber column
	 * @param targetId
	 * @param requestor
	 */
	private void updateQueryForSubscriber(final String targetId, final String requestor) {
		jdbcTemplate.update("update friendmanagement " + " set subscriber = ? " + " where email = ?",
				new Object[] { targetId, requestor });
	}


	/**
	 * This method is used to update subscribedBy column
	 * @param requestorId
	 * @param target
	 */
	private void updateQueryForSubscribedBy(final String requestorId, final String target) {
		jdbcTemplate.update("update friendmanagement " + " set subscribedBy = ? " + " where email = ?",
				new Object[] { requestorId, target });
	}


	/**
	 * This method is used to update the updated column
	 * @param recieverId
	 * @param sender
	 */
	private void updateQueryForUpdated(final String recieverId, final String sender) {
		jdbcTemplate.update("update friendmanagement " + " set updated = ? " + " where email = ?",
				new Object[] { recieverId, sender });
	}


	/** This method is used to get all the email from the friend table
	 * @return
	 */
	private List<String> getListOfAllEmails(){
		final String query = "SELECT email FROM friendmanagement";
		final List<String> emails = jdbcTemplate.queryForList(query, String.class);
		return emails;
	}

}