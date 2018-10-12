CREATE TABLE friendManagement(
Id integer not null AUTO_INCREMENT, 
email varchar(255) not null, 
friend_list varchar(255), 
subscriber varchar(255),
subscribedBy varchar(255),
updated varchar(255), 
updated_timestamp timestamp, primary key(Id)); 


/*CREATE TABLE friendManagement(
Id integer not null AUTO_INCREMENT, 
email varchar(255) not null, 
friend_list varchar(255), 
subscription varchar(255), 
text_message varchar(255), 
updated_timestamp timestamp, primary key(Id));*/

CREATE TABLE UNSUBSCRIBE(
Id integer not null AUTO_INCREMENT, 
Requestor_email varchar(255), 
Target_email varchar(255), 
Subscription_Status varchar(50));



