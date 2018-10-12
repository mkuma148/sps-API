package com.capgemini.controller;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.capgemini.exceptionhandling.ResourceNotFoundException;
import com.capgemini.model.CommonFriendsListResponse;
import com.capgemini.model.EmailsListRecievesUpdatesResponse;
import com.capgemini.model.Subscriber;
import com.capgemini.model.UserFriendsListResponse;
import com.capgemini.service.FriendMangmtService;
import com.capgemini.validation.FriendManagementValidation;

@RestController
@Validated
@EntityScan(basePackages = { "com.capgemini.model" })
@RequestMapping(value = "/api")
public class FriendManagementController {

	private final Logger LOG = LoggerFactory.getLogger(getClass());
	private static final String SUCCESS_STATUS = "Success";
	private static final String ERROR_STATUS = "error";

	public FriendMangmtService frndMngtServc;
	FriendManagementValidation fmError;

	@Autowired
	public FriendManagementController(FriendMangmtService frndMngtServc, FriendManagementValidation fmError) {
		this.frndMngtServc = frndMngtServc;
		this.fmError = fmError;
	}

	/**
	 * 
	 * @param userReq
	 * @param results
	 * @return ResponseEntity
	 * @throws ResourceNotFoundException
	 */

	@RequestMapping(value = "/create", method = RequestMethod.POST)
	public ResponseEntity<FriendManagementValidation> newFriendConnection(
			@Valid @RequestBody com.capgemini.model.UserRequest userReq, BindingResult results)
			throws ResourceNotFoundException {
		LOG.info("newFriendConnection :: ");
		FriendManagementValidation fmResponse = new FriendManagementValidation();
		// BaseResponse baseResponse = new BaseResponse();
		ResponseEntity<FriendManagementValidation> responseEntity = null;
		try {
			fmResponse = frndMngtServc.addNewFriendConnection(userReq);
			String isNewfrndMangmReqSuccess = fmResponse.getStatus();

			// LOG.info("newFriendConnection :: "+isNewfrndMangmReqSuccess);

			if (isNewfrndMangmReqSuccess.equalsIgnoreCase("Success")) {
				fmResponse.setStatus(SUCCESS_STATUS);
				responseEntity = new ResponseEntity<FriendManagementValidation>(fmResponse, HttpStatus.OK);
			} else {
				fmResponse.setStatus(ERROR_STATUS);
			}
			responseEntity = new ResponseEntity<FriendManagementValidation>(fmResponse, HttpStatus.OK);

		} catch (Exception e) {
			LOG.debug(e.getLocalizedMessage());
			responseEntity = new ResponseEntity<FriendManagementValidation>(fmResponse, HttpStatus.SERVICE_UNAVAILABLE);

		}

		return responseEntity;

	}

	/**
	 * 
	 * @param friendListRequest
	 * @param result
	 * @return
	 * @throws ResourceNotFoundException
	 */
	@RequestMapping(value = "/friendlist", method = RequestMethod.POST)
	public ResponseEntity<UserFriendsListResponse> getFriendList(
			@Valid @RequestBody com.capgemini.model.FriendListRequest friendListRequest, BindingResult result)
			throws ResourceNotFoundException {
		LOG.info("--getFriendList :: " + friendListRequest.getEmail());
		UserFriendsListResponse response = frndMngtServc.getFriendList(friendListRequest);
		ResponseEntity<UserFriendsListResponse> friendListResponseEntity = null;
		try {
			if (response.getStatus() == SUCCESS_STATUS) {
				response.setStatus(SUCCESS_STATUS);
				friendListResponseEntity = new ResponseEntity<UserFriendsListResponse>(response, HttpStatus.OK);
			} else {
				response.setStatus(ERROR_STATUS);
				friendListResponseEntity = new ResponseEntity<UserFriendsListResponse>(response,
						HttpStatus.BAD_REQUEST);
			}
		} catch (Exception e) {
			LOG.debug(e.getLocalizedMessage());
			friendListResponseEntity = new ResponseEntity<UserFriendsListResponse>(response,
					HttpStatus.SERVICE_UNAVAILABLE);

		}
		return friendListResponseEntity;

	}

	/**
	 * 
	 * 
	 * @param commonFrndReq
	 * @return
	 * @throws ResourceNotFoundException
	 */

	@RequestMapping(value = "/friends", method = RequestMethod.POST)
	public ResponseEntity<CommonFriendsListResponse> getCommonFriendList(
			@Valid @RequestBody com.capgemini.model.CommonFriendsListRequest commonFrndReq)
			throws ResourceNotFoundException {
		LOG.info("getCommonFriendList");
		ResponseEntity<CommonFriendsListResponse> commonFriendResponseEntity = null;
		CommonFriendsListResponse response = new CommonFriendsListResponse();
		try {
			response = frndMngtServc.retrieveCommonFriendList(commonFrndReq.getFriends().get(0),
					commonFrndReq.getFriends().get(1));

			if (response.getStatus() == SUCCESS_STATUS) {
				response.setStatus(SUCCESS_STATUS);
				commonFriendResponseEntity = new ResponseEntity<CommonFriendsListResponse>(response, HttpStatus.OK);
			} else {
				response.setStatus(ERROR_STATUS);
				commonFriendResponseEntity = new ResponseEntity<CommonFriendsListResponse>(response,
						HttpStatus.BAD_REQUEST);
			}
		} catch (Exception e) {
			LOG.debug(e.getLocalizedMessage());
			commonFriendResponseEntity = new ResponseEntity<CommonFriendsListResponse>(response,
					HttpStatus.SERVICE_UNAVAILABLE);

		}
		return commonFriendResponseEntity;
	}

	/**
	 * @param subscriber
	 * @param result
	 * @return
	 * @throws ResourceNotFoundException
	 */
	@RequestMapping(value = "/subscribe", method = RequestMethod.POST)
	public ResponseEntity<FriendManagementValidation> subscribeFriend(
			@Valid @RequestBody com.capgemini.model.Subscriber subscriber, BindingResult result)
			throws ResourceNotFoundException {
		LOG.info("Calling subscribe Friend ::");
		// Validation
		if (result.hasErrors()) {
			return handleValidation(result);
		}

		ResponseEntity<FriendManagementValidation> subscribeFriendResponseEntity = null;
		FriendManagementValidation fmv = new FriendManagementValidation();

		try {
			fmv = frndMngtServc.subscribeTargetFriend(subscriber);
			if (fmv.getStatus() == SUCCESS_STATUS) {
				subscribeFriendResponseEntity = new ResponseEntity<FriendManagementValidation>(fmv, HttpStatus.OK);
			} else {
				subscribeFriendResponseEntity = new ResponseEntity<FriendManagementValidation>(fmv,
						HttpStatus.BAD_REQUEST);
			}
		} catch (Exception e) {
			LOG.debug(e.getLocalizedMessage());
			subscribeFriendResponseEntity = new ResponseEntity<FriendManagementValidation>(fmv,
					HttpStatus.SERVICE_UNAVAILABLE);
		}

		return subscribeFriendResponseEntity;

	}

	/**
	 * 
	 * @param subscriber
	 * @param result
	 * @return
	 * @throws ResourceNotFoundException
	 */

	@RequestMapping(value = "/unsubscribe", method = RequestMethod.POST)
	public ResponseEntity<FriendManagementValidation> unSubscribeFriend(
			@Valid @RequestBody com.capgemini.model.Subscriber subscriber, BindingResult result)
			throws ResourceNotFoundException {
		// Validation
		ResponseEntity<FriendManagementValidation> unsubscribeFriendResponseEntity = null;
		boolean isValid = validateInput(subscriber);
		LOG.info("::: Caling unSubscribeFriend () ::");
		if (!isValid) {
			unsubscribeFriendResponseEntity = new ResponseEntity<FriendManagementValidation>(HttpStatus.BAD_REQUEST);
		}

		FriendManagementValidation fmv = null;
		try {
			fmv = frndMngtServc.unSubscribeTargetFriend(subscriber);
			if (fmv.getStatus() == SUCCESS_STATUS) {
				unsubscribeFriendResponseEntity = new ResponseEntity<FriendManagementValidation>(fmv, HttpStatus.OK);
			} else {
				unsubscribeFriendResponseEntity = new ResponseEntity<FriendManagementValidation>(fmv,
						HttpStatus.BAD_REQUEST);
			}
		} catch (Exception e) {
			LOG.debug(e.getLocalizedMessage());
			unsubscribeFriendResponseEntity = new ResponseEntity<FriendManagementValidation>(fmv,
					HttpStatus.BAD_REQUEST);
		}

		return unsubscribeFriendResponseEntity;
	}

	/**
	 * 
	 * @param emailsList
	 * @param result
	 * @return
	 * @throws ResourceNotFoundException
	 */

	@RequestMapping(value = "/friends/updatelist", method = RequestMethod.POST)
	public ResponseEntity<EmailsListRecievesUpdatesResponse> emailListRecievesupdates(
			@Valid @RequestBody com.capgemini.model.EmailsListRecievesUpdatesRequest emailsList, BindingResult result)
			throws ResourceNotFoundException {

		LOG.info("::  Calling emailListRecievesupdates ::");

		ResponseEntity<EmailsListRecievesUpdatesResponse> responseEntity = null;
		EmailsListRecievesUpdatesResponse response = new EmailsListRecievesUpdatesResponse();
		try {
			response = frndMngtServc.emailListRecievesupdates(emailsList);
			if (response.getStatus().toString() == SUCCESS_STATUS) {
				response.setStatus(SUCCESS_STATUS);
				responseEntity = new ResponseEntity<EmailsListRecievesUpdatesResponse>(response, HttpStatus.OK);
			} else {
				response.setStatus(ERROR_STATUS);
				responseEntity = new ResponseEntity<EmailsListRecievesUpdatesResponse>(response,
						HttpStatus.BAD_REQUEST);
			}
		} catch (Exception e) {
			LOG.debug(e.getLocalizedMessage());
			responseEntity = new ResponseEntity<EmailsListRecievesUpdatesResponse>(response,
					HttpStatus.SERVICE_UNAVAILABLE);

		}
		return responseEntity;

	}

	/**
	 * This method is used for client validation
	 * 
	 * @param result
	 * @return
	 */
	private ResponseEntity<FriendManagementValidation> handleValidation(BindingResult result) {
		fmError.setStatus("Failed");
		if (result.getFieldError("requestor") != null && result.getFieldError("target") != null) {
			fmError.setErrorDescription(result.getFieldError("requestor").getDefaultMessage() + " "
					+ result.getFieldError("target").getDefaultMessage());
		} else if (result.getFieldError("target") != null) {
			fmError.setErrorDescription(result.getFieldError("target").getDefaultMessage());
		} else {
			fmError.setErrorDescription(result.getFieldError("requestor").getDefaultMessage());

		}
		return new ResponseEntity<FriendManagementValidation>(fmError, HttpStatus.BAD_REQUEST);

	}

	/**
	 * 
	 * 
	 * @param subscriber
	 * @return
	 */

	private boolean validateInput(Subscriber subscriber) {
		final String requestor = subscriber.getRequestor();
		final String target = subscriber.getTarget();
		if (requestor == null || target == null || requestor.equalsIgnoreCase(target)) {
			return false;
		}
		return true;
	}

}