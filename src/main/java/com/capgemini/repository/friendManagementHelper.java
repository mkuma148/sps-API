package com.capgemini.repository;

import static com.capgemini.config.FriendMgntConstants.FRIEND_MANAGEMENT_BLOCKED;
import static com.capgemini.config.FriendMgntConstants.FRIEND_MANAGEMENT_COMMA;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.StringJoiner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.capgemini.exceptionhandling.ResourceNotFoundException;

@Component
public class friendManagementHelper {

	private final Logger LOG = LoggerFactory.getLogger(getClass());
	
	@Autowired
	JdbcTemplate jdbcTemplate;
	
	@Autowired
	public friendManagementHelper(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	
	/**
	 * This method is invoked to insert new record in a friendmanagement table
	 * @param email
	 * @return
	 */
	public int createNewFriend(final String email) {
		return jdbcTemplate.update(
				"insert into friendmanagement(email, friend_list, subscriber, subscribedBy, updated, updated_timestamp) values(?,?,?,?,?,?)",
				new Object[] { email, "", "", "", "", new Timestamp((new Date()).getTime()) });
	}

	/**
	 * This method is used to get the all the email by ID
	 * @param friendListQueryParam
	 * @return
	 */
	public List<String> getEmailByIds(final List<String> friendListQueryParam) {

		StringJoiner email_Ids = new StringJoiner(FRIEND_MANAGEMENT_COMMA, "SELECT email FROM friendmanagement WHERE id in (", ")");

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
	public String getSubscriptionList(final String email) {
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
	public String getSubscribedByList(final String email) {
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
	public void connectFriend(final String firstEmail, final String secondEmail) throws ResourceNotFoundException{
		String requestorId = getId(firstEmail);
		String friendList = getFriendList(secondEmail);

		friendList = friendList.isEmpty() ? requestorId : friendList + FRIEND_MANAGEMENT_COMMA + requestorId;

		jdbcTemplate.update("update friendmanagement " + " set friend_list = ?" + " where email = ?",
				new Object[] { friendList, secondEmail });
	}

	/**
	 * This method is invoked to check whether the friend is already connected
	 * @param requestor
	 * @param target
	 * @return
	 */
	public boolean isAlreadyFriend(final String requestor, final String target) throws ResourceNotFoundException{
		boolean alreadyFriend = false;

		String requestorId = getId(requestor);
		String targetId = getId(target);

		String requestorFriendList = getFriendList(requestor);
		String[] requestorFriends = requestorFriendList.split(FRIEND_MANAGEMENT_COMMA);

		String targetFirendList = getFriendList(target);
		String[] targetFriends = targetFirendList.split(FRIEND_MANAGEMENT_COMMA);

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
	public String getId(final String email) {
		final String sql = "SELECT id FROM friendmanagement WHERE email=?";
		final String requestorId = (String) jdbcTemplate.queryForObject(sql, new Object[] { email }, String.class);
		return requestorId;
	}

	/**
	 * This method is invoked to get the list of friends
	 * @param email
	 * @return
	 */
	public String getFriendList(final String email) throws ResourceNotFoundException{
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
	public boolean isBlocked(final String requestor_email, final String target_email) {
		boolean status = false;
		try {
			final String sqlrFriendList = "SELECT Subscription_Status FROM unsubscribe WHERE Requestor_email=? AND Target_email=?";
			final String Subscription_Status = (String) jdbcTemplate.queryForObject(sqlrFriendList,
					new Object[] { requestor_email, target_email }, String.class);
			LOG.info(":: Subscription_Status " + Subscription_Status);
			if (Subscription_Status.equalsIgnoreCase(FRIEND_MANAGEMENT_BLOCKED)) {
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
	public void updateUnsubscribeTable(final String requestor, final String target, String status) {
		jdbcTemplate.update(
				"insert into UNSUBSCRIBE(Requestor_email, Target_email, Subscription_Status) values(?, ?, ?)",
				new Object[] { requestor, target, status });
	}


	/**
	 * This method is used to update the subscribedBy column
	 * @param requestor
	 * @param target
	 */
	public void updateSubscribedBy(final String requestor, final String target) {
		String requestorId = getId(requestor);
		String subscribedList = getSubscribedByList(target);
		if (subscribedList.isEmpty()) {
			updateQueryForSubscribedBy(requestorId, target);
		} else {
			String[] subscr = subscribedList.split(FRIEND_MANAGEMENT_COMMA);
			ArrayList<String> subscrList = new ArrayList<String>(Arrays.asList(subscr));

			if (!subscrList.contains(requestorId)) {
				requestorId = subscribedList + FRIEND_MANAGEMENT_COMMA + requestorId;
				updateQueryForSubscribedBy(requestorId, target);
			}
		}
	}


	/**
	 * This method is used to update subscriber column
	 * @param targetId
	 * @param requestor
	 */
	public void updateQueryForSubscriber(final String targetId, final String requestor) {
		jdbcTemplate.update("update friendmanagement " + " set subscriber = ? " + " where email = ?",
				new Object[] { targetId, requestor });
	}


	/**
	 * This method is used to update subscribedBy column
	 * @param requestorId
	 * @param target
	 */
	public void updateQueryForSubscribedBy(final String requestorId, final String target) {
		jdbcTemplate.update("update friendmanagement " + " set subscribedBy = ? " + " where email = ?",
				new Object[] { requestorId, target });
	}


	/**
	 * This method is used to update the updated column
	 * @param recieverId
	 * @param sender
	 */
	public void updateQueryForUpdated(final String recieverId, final String sender) {
		jdbcTemplate.update("update friendmanagement " + " set updated = ? " + " where email = ?",
				new Object[] { recieverId, sender });
	}


	/** This method is used to get all the email from the friend table
	 * @return
	 */
	public List<String> getListOfAllEmails(){
		final String query = "SELECT email FROM friendmanagement";
		final List<String> emails = jdbcTemplate.queryForList(query, String.class);
		return emails;
	}
}
