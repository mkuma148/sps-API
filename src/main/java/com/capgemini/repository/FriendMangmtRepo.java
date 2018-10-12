package com.capgemini.repository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.capgemini.exceptionhandling.ResourceNotFoundException;
import com.capgemini.model.CommonFriendsListResponse;
import com.capgemini.model.EmailsListRecievesUpdatesResponse;
import com.capgemini.model.UserFriendsListResponse;
import com.capgemini.validation.FriendManagementValidation;


/**
 * @author vishu
 *
 */
@Repository
public class FriendMangmtRepo {

	@Autowired
	FriendManagementValidation fmError;

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Autowired
	public FriendMangmtRepo(FriendManagementValidation fmError,JdbcTemplate jdbcTemplate){
		this.fmError=fmError;
		this.jdbcTemplate=jdbcTemplate;
	}

	@Autowired
	NamedParameterJdbcTemplate namedParameterJdbcTemplate;


	/**
	 * This API is invoked to add connection between two emails
	 * @param userReq
	 * @return
	 */
	public FriendManagementValidation addNewFriendConnection(com.capgemini.model.UserRequest userReq) {
		try {

			String requestor = userReq.getRequestor();
			String target = userReq.getTarget();


			String query = "SELECT email FROM friendmanagement";

			List<String> emails = jdbcTemplate.queryForList(query, String.class);
			fmError.setStatus("Success");
			fmError.setErrorDescription("Successfully connected");
			if(requestor.equals(target)) {
				fmError.setStatus("Failed");
				fmError.setErrorDescription("Requestor and target should not be same");
				return fmError;
			}

			if (emails.contains(requestor) && emails.contains(target)) {

				boolean isBlocked = isBlocked(requestor, target);
				if (!isBlocked) {
					if (isAlreadyFriend(requestor, target)) {
						fmError.setStatus("Failed");
						fmError.setErrorDescription("Already friends");
					} else {
						connectFriend(requestor, target);
						connectFriend(target, requestor);
					}
				} else {
					fmError.setStatus("Failed");
					fmError.setErrorDescription("target blocked");
				}
			}else if(!emails.contains(requestor) && !emails.contains(target)) {
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
			e.printStackTrace();
		}

		return fmError;

	} 


	/**
	 * This API is invoked to get the list of all the friends connected to requestor friend
	 * @param email
	 * @return
	 * @throws ResourceNotFoundException
	 */
	public UserFriendsListResponse getFriendsList(com.capgemini.model.FriendListRequest friendListRequest) throws ResourceNotFoundException {

		UserFriendsListResponse emailListresponse = new UserFriendsListResponse();
		System.out.println("----getFriendList-----"+friendListRequest.getEmail());
		String friendList = getFriendList(friendListRequest.getEmail());
		if ("".equals(friendList) || friendList==null) {
			emailListresponse.setStatus("Failed");
			emailListresponse.setCount(0);
		} else {
			String[] friendListQueryParam = friendList.split(",");
			List<String> friends = getEmailByIds(Arrays.asList(friendListQueryParam));
			if(friends.size()==0) {

			}else {
				emailListresponse.setStatus("Success");
				emailListresponse.setCount(friends.size());
				for (String friend : friends) {
					emailListresponse.getFriends().add(friend);
				}
			}
		}
		return emailListresponse;

	} 


	/**
	 * This API is invoked to get the common friends between two friends
	 * @param email1
	 * @param email2
	 * @return
	 */
	public CommonFriendsListResponse retrieveCommonFriendList(String email1 ,String email2){
		CommonFriendsListResponse commonFrndListresponse = new CommonFriendsListResponse();

		String friendList1 = getFriendList(email1);
		String friendList2 = getFriendList(email2);
		String[] friendList1Container = friendList1.split(",");
		String[] friendList2Container = friendList2.split(",");

		Set<String> friend1Set = new HashSet<String>(Arrays.asList(friendList1Container));
		Set<String> friend2Set = new HashSet<String>(Arrays.asList(friendList2Container));
		friend1Set.retainAll(friend2Set);

		List<String> friends = getEmailByIds(new ArrayList<String>(friend1Set));
		if(friends.size() == 0) {

		}else {
			commonFrndListresponse.setStatus("success");
			commonFrndListresponse.setCount(friends.size());
			for (String friend : friends) {
				commonFrndListresponse.getFriends().add(friend);
			}}
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

		String requestor = subscriber.getRequestor();
		String target = subscriber.getTarget();

		String query = "SELECT email FROM friendmanagement";
		List<String> emails = jdbcTemplate.queryForList(query, String.class);

		if(requestor.equals(target)) {
			fmError.setStatus("Failed");
			fmError.setErrorDescription("Requestor and target should not be same");
			return fmError;
		}
		fmError.setStatus("Success");
		fmError.setErrorDescription("Subscribed successfully");
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
						fmError.setStatus("Failed");
						fmError.setErrorDescription("Target already subscribed");
					}
				}

			} else {
				fmError.setStatus("Failed");
				fmError.setErrorDescription("Check Target or Requestor email id");
			}
		}else {
			fmError.setStatus("Failed");
			fmError.setErrorDescription("target blocked");
		}
		return fmError;
	}


	/**
	 * This API is invoked to retrive all email address that can receive updates from an email address
	 * @param emailsList
	 * @return
	 */
	public EmailsListRecievesUpdatesResponse emailListRecievesupdates(com.capgemini.model.EmailsListRecievesUpdatesRequest emailsList ){

		EmailsListRecievesUpdatesResponse  EmailsList = new EmailsListRecievesUpdatesResponse();

		String query = "SELECT email FROM friendmanagement";
		List<String> emails = jdbcTemplate.queryForList(query, String.class);
		String sender = emailsList.getSender();
		String text = emailsList.getText();
		text = text.trim();
		String reciever = text.substring(text.lastIndexOf(' ') + 1).substring(1);

		if(emails.contains(sender)) {
			if(emails.contains(reciever)) {
				String recieverId = getId(reciever);
				updateQueryForUpdated(recieverId, sender);
			}else {
				insertEmail(reciever);
				String recieverId = getId(reciever);
				updateQueryForUpdated(recieverId, sender);
			}


			String friendList = getFriendList(sender);
			String[] senderFriends = friendList.split(",");

			String subscribedBy = getSubscribedByList(sender);
			String[] subscribedFriends = subscribedBy.split(",");


			Set<String> set = new HashSet<String>();
			set.addAll(Arrays.asList(senderFriends));
			set.addAll(Arrays.asList(subscribedFriends));
			List<String> emailsUnion = new ArrayList<String>(set);

			List<String> commonEmails = getEmailByIds(emailsUnion);


			if(!commonEmails.contains(reciever)) {
				commonEmails.add(reciever);
			}


			EmailsList.setStatus("Success");
			for(String email:commonEmails) {
				EmailsList.getFriends().add(email);
			}
		}else {
			EmailsList.setStatus("Failed");
		}
		return EmailsList;
	}


	/**
	 * This API is invoked to unsubscribe and remove id from subscribe and subscribedBy column
	 * @param subscriber
	 * @return
	 * @throws ResourceNotFoundException
	 */
	public FriendManagementValidation unSubscribeTargetFriend(com.capgemini.model.Subscriber subscriber)throws ResourceNotFoundException {
		String requestor = subscriber.getRequestor();
		String target = subscriber.getTarget();

		String query = "SELECT email FROM friendmanagement";
		List<String> emails = jdbcTemplate.queryForList(query, String.class);

		if(emails.contains(requestor) && emails.contains(target)) {
			String sql = "SELECT subscriber FROM friendmanagement WHERE email=?";
			String subscribers = (String) jdbcTemplate.queryForObject(
					sql, new Object[] { requestor }, String.class);
			if(subscribers == null || subscribers.isEmpty()) {
				fmError.setStatus("Failed");
				fmError.setErrorDescription("Requestor does not subscribe to any email");
			}else {
//				unsubscribeTarget(email);
				String[] subs = subscribers.split(",");
				ArrayList<String> subscriberList = new ArrayList<>(Arrays.asList(subs));
				String targetId = getId(target);
				if(subscriberList.contains(targetId)) {
					StringJoiner sjTarget = new StringJoiner(",");
					for(String sub:subscriberList) {
						if(!sub.equals(targetId)) {
							sjTarget.add(sub);
						}
					}
					updateQueryForSubscriber(sjTarget.toString(),requestor);
					
					//This section is used to remove requestor id from subscribedBy column
					String sqlQuery = "SELECT subscribedBy FROM friendmanagement WHERE email=?";
					String subscribedBys = (String) jdbcTemplate.queryForObject(
							sqlQuery, new Object[] { target }, String.class);
					String[] subscribedBy = subscribedBys.split(",");
					ArrayList<String> subscribedByList = new ArrayList<>(Arrays.asList(subscribedBy));
					String requestorId = getId(requestor);
					if(subscribedByList.contains(requestorId)) {
						StringJoiner sjRequestor = new StringJoiner(",");
						for(String sub:subscribedByList) {
							if(!sub.equals(requestorId)) {
								sjRequestor.add(sub);
							}
						}
						updateQueryForSubscribedBy(sjRequestor.toString(),target);
					}
					
					updateUnsubscribeTable(requestor, target, "Blocked");
					
					fmError.setStatus("Success");
					fmError.setErrorDescription("Unsubscribed successfully");
				}else {
					fmError.setStatus("Failed");
					fmError.setErrorDescription("No Target available");
				}
			}
		}else{
			fmError.setStatus("Failed");
			fmError.setErrorDescription("Please provide valid Requestor and Target email");
		}
		return fmError;
	}


	/**
	 * This method is invoked to insert new record in a friendmanagement table
	 * @param email
	 * @return
	 */
	private int insertEmail(String email) {
		return jdbcTemplate.update("insert into friendmanagement(email, friend_list, subscriber, subscribedBy, updated, updated_timestamp) values(?,?,?,?,?,?)",
				new Object[] { email,"" , "", "", "", 
						new Timestamp((new Date()).getTime()) });
	} 


	/**
	 * This method is used to get the all the email by ID
	 * @param friendListQueryParam
	 * @return
	 */
	private List<String> getEmailByIds(List<String> friendListQueryParam){

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
	private String getSubscriptionList(String email) {
		String sqlrFriendList = "SELECT subscriber FROM friendmanagement WHERE email=?";
		String friendList = (String) jdbcTemplate.queryForObject(sqlrFriendList, new Object[] { email }, String.class);
		return friendList;
	}


	/**
	 * This method is used to get the subscribedBy Ids for a particular email
	 * @param email
	 * @return
	 */
	private String getSubscribedByList(String email) {
		String sqlrFriendList = "SELECT subscribedBy FROM friendmanagement WHERE email=?";
		String friendList = (String) jdbcTemplate.queryForObject(sqlrFriendList, new Object[] { email }, String.class);
		return friendList;
	}


	/**
	 * This method is invoked to connect friend
	 * 
	 * @param firstEmail
	 * @param secondEmail
	 */
	private void connectFriend(String firstEmail, String secondEmail) {
		String requestorId = getId(firstEmail);
		String friendList = getFriendList(secondEmail);

		friendList = friendList.isEmpty() ? requestorId : friendList + "," + requestorId;

		jdbcTemplate.update("update friendmanagement " + " set friend_list = ?" + " where email = ?",
				new Object[] { friendList, secondEmail });
	}

	/**
	 * This method is invoked to check whether the friend is already connected
	 * 
	 * @param requestor
	 * @param target
	 * @return
	 */
	private boolean isAlreadyFriend(String requestor, String target) {
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
		System.out.println("alreadyFriend " + alreadyFriend);
		return alreadyFriend;

	}

	/**
	 * this method is invoked to get Id of particular email
	 * 
	 * @param email
	 * @return
	 */
	private String getId(String email) {
		String sql = "SELECT id FROM friendmanagement WHERE email=?";
		String requestorId = (String) jdbcTemplate.queryForObject(sql, new Object[] { email }, String.class);
		return requestorId;
	}

	/**
	 * This method is invoked to get the list of friends
	 * 
	 * @param email
	 * @return
	 */
	private String getFriendList(String email) {
		String sqlrFriendList = "SELECT friend_list FROM friendmanagement WHERE email=?";
		String friendList = (String) jdbcTemplate.queryForObject(sqlrFriendList, new Object[] { email }, String.class);
		return friendList;

	}

	/**
	 * This method is invoked to check whether the target is blocked or not
	 * 
	 * @param requestor_email
	 * @param target_email
	 * @return
	 */
	private boolean isBlocked(String requestor_email, String target_email) {
		boolean status = false;
		try {
			String sqlrFriendList = "SELECT Subscription_Status FROM unsubscribe WHERE Requestor_email=? AND Target_email=?";
			String Subscription_Status = (String) jdbcTemplate.queryForObject(sqlrFriendList,
					new Object[] { requestor_email, target_email }, String.class);
			System.out.println("Subscription_Status " + Subscription_Status);
			if (Subscription_Status.equalsIgnoreCase("Blocked")) {
				status = true;
			}
		} catch (Exception e) {

		}
		return status;
	}







	private void updateUnsubscribeTable(String requestor, String target,String status) {
		jdbcTemplate.update("insert into UNSUBSCRIBE(Requestor_email, Target_email, Subscription_Status) values(?, ?, ?)",
				new Object[] { requestor, target, status });
	}


	/**
	 * This method is used to update the subscribedBy column
	 * @param requestor
	 * @param target
	 */
	private void updateSubscribedBy(String requestor, String target) {
		String requestorId = getId(requestor);
		String subscribedList = getSubscribedByList(target);
		if (subscribedList.isEmpty()) {
			updateQueryForSubscribedBy(requestorId, target);
		}else {
			String[] subscr = subscribedList.split(",");
			ArrayList<String> subscrList = new ArrayList<String>(Arrays.asList(subscr));

			if (!subscrList.contains(requestorId)) {
				requestorId = subscribedList + "," + requestorId;
				updateQueryForSubscribedBy(requestorId, target);
			}
		}
	}


	private void updateQueryForSubscriber(String targetId, String requestor) {
		jdbcTemplate.update(
				"update friendmanagement " + " set subscriber = ? " + " where email = ?",
				new Object[] { targetId, requestor });
	}

	private void updateQueryForSubscribedBy(String requestorId, String target) {
		jdbcTemplate.update(
				"update friendmanagement " + " set subscribedBy = ? " + " where email = ?",
				new Object[] { requestorId, target });
	}

	private void updateQueryForUpdated(String recieverId, String sender) {
		jdbcTemplate.update(
				"update friendmanagement " + " set updated = ? " + " where email = ?",
				new Object[] { recieverId, sender });
	}

}
