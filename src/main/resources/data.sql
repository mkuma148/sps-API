insert into friendmanagement(email, friend_list, subscriber, subscribedBy, updated, updated_timestamp) values('john@example.com', '', '', '', '', now());
insert into friendmanagement(email, friend_list, subscriber, subscribedBy, updated, updated_timestamp) values('andy@example.com', '', '', '', '', now());
insert into friendmanagement(email, friend_list, subscriber, subscribedBy, updated, updated_timestamp) values('lily@example.com', '', '', '', '', now());
insert into friendmanagement(email, friend_list, subscriber, subscribedBy, updated, updated_timestamp) values('lucy@example.com', '', '', '', '', now()); 

insert into unsubscribe(Requestor_email, Target_email, Subscription_Status) values('john@example.com', 'andy@example.com', 'Blocked');
insert into unsubscribe(Requestor_email, Target_email, Subscription_Status) values('lily@example.com', 'lucy@example.com', 'Blocked');