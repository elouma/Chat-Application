CREATE TABLE user_Info
(
first_name varchar(50),
last_name varchar(50),
user_name varchar(80),
user_password varchar(40),
email varchar(255),
course_name varchar(40),
course_type varchar(20),
create_date date,
Is_active bool,
photo bytea null,
user_Id serial PRIMARY KEY
);

CREATE TABLE group_Info
(
group_name varchar(20),
group_create_date date,
Is_active bool,
group_Id serial PRIMARY KEY);

CREATE TABLE user_group
(
user_Id int constraint fk_ug_user  REFERENCES user_Info(user_Id),
group_Id int constraint fk_ug_group REFERENCES group_Info(group_Id),
ug_create_date date,
Is_active bool,
PRIMARY KEY (user_id,group_id)
);

CREATE TABLE reminder_frequency
(
title varchar(25),
frequency int unique,
is_active bool,
frequency_id serial PRIMARY KEY
);

CREATE TABLE message_Info
(
recipient_Id int,
subject varchar(122),
user_Id int constraint fk_message_user  REFERENCES user_Info(user_Id),
create_date date,
message_body text,
parent_message_Id int NULL constraint fk_message_Info  REFERENCES message_Info(message_Id) ,
expiry_date date,
is_reminder bool,
next_reminder_date date null,
frequency int null constraint fk_recipient_frequency  REFERENCES reminder_frequency(frequency),
message_Id serial PRIMARY KEY
);

CREATE TABLE user_contact
(
user1_Id int constraint fk_ug_user1  REFERENCES user_Info(user_Id),
user2_Id int constraint fk_ug_user2  REFERENCES user_Info(user_Id),
uc_create_date date,
Is_pending bool,
PRIMARY KEY (user1_Id,user2_Id)
);

INSERT INTO group_info VALUES ('seine','2020-02-01',TRUE);
INSERT INTO group_info VALUES ('se2','2020-02-02',TRUE);
INSERT INTO group_info VALUES ('HCI','2020-02-02',TRUE);
INSERT INTO user_info VALUES ('siyu', 'tang','sxt966','123456','sxt966@student.bham.ac.uk','computer science','msc','2019-09-01',false);
INSERT INTO user_info VALUES ('hang', 'shi','hxs929','654321','hxs929@student.bham.ac.uk','computer science','msc','2019-09-01',true);
INSERT INTO user_info VALUES ('Rumit', 'Mehta','rxm669','654321','rxm669@student.bham.ac.uk','computer science','msc','2019-09-01',true);
INSERT INTO user_info VALUES ('Jean ', 'Emmanuel Messey-Elouma','jym918','654321','jym918@student.bham.ac.uk','computer science','msc','2019-09-01',true);
INSERT INTO user_info VALUES ('Jian', 'Tang','jxt967','654321','jxt967@student.bham.ac.uk','computer science','msc','2019-09-01',true);
INSERT INTO user_group VALUES (1,1,'2020-02-01',true);
INSERT INTO user_group VALUES (1,2,'2020-02-01',true);
INSERT INTO user_group VALUES (2,1,'2020-02-01',true);
INSERT INTO user_group VALUES (2,2,'2020-02-01',true);
INSERT INTO user_group VALUES (3,1,'2020-02-01',true);
INSERT INTO user_group VALUES (3,2,'2020-02-01',true);
INSERT INTO user_group VALUES (4,3,'2020-02-01',true);
INSERT INTO user_group VALUES (4,1,'2020-02-01',true);
INSERT INTO user_group VALUES (5,2,'2020-02-01',true);
INSERT INTO user_group VALUES (5,3,'2020-02-01',true);
INSERT INTO user_contact VALUES (1,2,'2020-01-01',TRUE);
INSERT INTO user_contact VALUES (1,3,'2020-01-01',TRUE);
INSERT INTO user_contact VALUES (1,5,'2020-01-01',TRUE);
INSERT INTO user_contact VALUES (2,3,'2020-01-01',TRUE);
INSERT INTO user_contact VALUES (2,4,'2020-01-01',TRUE);
INSERT INTO user_contact VALUES (3,5,'2020-01-01',TRUE);
INSERT INTO message_info VALUES(1,'greeting',2,'2020-02-11','Hi! Siyu!',null,'2021-02-11',false,null,null);
INSERT INTO message_info VALUES(2,'greeting',1,'2020-02-11','Hi! Hang!',null,'2021-02-11',false,null,null);
INSERT INTO message_info VALUES(4,'greeting',3,'2020-02-11','how was your day?',null,'2021-02-11',false,null,null);
INSERT INTO message_info VALUES(3,'greeting',4,'2020-02-11','great! thx!',null,'2021-02-11',false,null,null);
