
#Friend Management API

This is an application with a need to build its own social network, "Friends Management" is a common requirement which usually starts off simple but can grow in complexity depending on the application's use case. Usually, the application comprised of features like "Friend", "Unfriend", "Block", "Receive Updates" etc.

#Technology Choice

#Spring Boot
1.	Spring Boot allows easy setup of standalone Spring-based applications.
2.	Ideal for spinning up microservices and easy to deploy.
3.	Makes data access less of a pain, i.e. JPA mappings through Spring Data.

#JDBC Template
1.	Spring JdbcTemplate is a powerful mechanism to connect to the database and execute SQL queries. 
2.	It internally uses JDBC api, but eliminates a lot of problems of JDBC API.
H2 Database (In memory database)
1.	H2 is an open-source lightweight Java database. 
2.	H2 database can be configured to run as in-memory database, which means that data will not persist on the disk.

#Swagger
1.	Swagger is a framework for describing API using a common language that everyone can understand.
2.	The Swagger spec standardizes API practices, how to define parameters, paths, responses, models, etc.

#PCF
1.	Cloud Foundry is an open source, multi-cloudapplication platform as a service (PaaS) governed by the Cloud Foundry Foundation organization
2.	The software was originally developed by VMware and then transferred to Pivotal Software, a joint venture by EMC, VMware and General Electric.
3.	This gives a chance to try out PCF in our software
	   
	    The Application is deployed on Pivotal cloud. It can be accessed via the below url and the path for all the api is /friendmgt/api
		https://sps-test.cfapps.io/friendmgt/api      
		For example: To access /create endpoint, the URL should be:
		https://sps-test.cfapps.io/friendmgt/api/create    
		Swagger UI is configured for the app and it is available: https://sps-test.cfapps.io/friendmgt/swagger-ui.html
  
#List of REST Endpoints and Explanation

#1.	 Establish Friendship between two persons

		a. URI:  https://sps-test.cfapps.io/friendmgt/api/create
		b. Method: POST
		c. Header: Content Type – Application/Json
		d. Request body: 
		{
		  "requestor":"john@example.com",
		  "target":"lily@example.com"
		   }
		e. Response body
		   	{
		      "status": "Success",
		      "description": "Successfully connected"
		   }
	
	     Defined Errors:
	      •	200: Success.
	      •	400: Bad Request

#2.	 Returns a list of friends of a person.

	   a.	 URI:  https://sps-test.cfapps.io/friendmgt/api/friendlist
	   b.	Method: POST
	   c.	Header: Content Type – Application/Json
	   d.	Request body: 
	   {
	      "email":"john@example.com"
	   }
	   e.	Response body
	       {
	            "status": "Success",
	            "count": 2,
	           "friends": [
	                     "lily@example.com",
	                     "abc@example.com" 
	                    ]
	       }
	
	
	     Defined Errors:
	      •	200: Success.
	      •	400: Bad Request


#3. 	Returns list of common friends of two persons 
	   a.	 URI:  https://sps-test.cfapps.io/friendmgt/api/commonFriends
	   b.	Method: POST
	   c.	Header: Content Type – Application/Json
	   d.	Request body: 
	        {
	            "friends": [
	                    "lily@example.com",
	                   "abc@example.com"
	          ]
	   
	        }
	   e.	Response body
	              {
	                      "status": "Success",
	                      "count": 1,
	                     "friends": [
	                                 "john@example.com"
	                       ]
	              }
	
	     Defined Errors:
	      •	200: Success.
	      •	400: Bad Request


#4. 	Person subscribe to another Person
	   a.	URI:  https://sps-test.cfapps.io/friendmgt/api/subscribe
	   b.	Method: POST
	   c.	 Header: Content Type – Application/Json
	   d.	 Request body: 
	        {
	            "requestor":"abc@example.com",
	             "target":"xyz@example.com"
	   }
	   e.	 Response body
	      {
	              "status": "Success",
	              "description": "Subscribed successfully" 
	       }
	
	     Defined Errors:
	      •	200: Success.
	      •	400: Bad Request

#5.	 Person block updates from another Person
	   a.	   URI:  https://sps-test.cfapps.io/friendmgt/api/unsubscribe
	   b.	Method: POST
	   c.	 Header: Content Type – Application/Json
	   d.	Request body: 
	        {
	      "requestor":"abc@example.com",
	      "target":"xyz@example.com" 
	     }
	   e.	 Response body
	       {
	            "status": "Success",
	              "description": "Unsubscribed successfully"
	        }
	
	    Defined Errors:
	      •	200: Success.
	      •	400: Bad Request

#6.	  Post an update which returns a list of emails that will receive the update.
	   a.	 URI:  https://sps-test.cfapps.io/friendmgt/api/allEmailAddress 
	   b.	Method: POST 
	   c.	Header: Content Type – Application/Json 
	   d.	Request body: 
	        {
	           "requestor":"abc@example.com",
	           "target":"xyz@example.com"
	       }
	   e.	 Response body
	       {
	            "status": "Success",
	             "description": "Unsubscribed successfully"
	       }
	
	     Defined Errors:
	      •	200: Success.
	      •	400: Bad Request




