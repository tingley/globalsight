# GBS-1158: PM set up their super user
# Vincent Yan, 2010/06/22

CREATE TABLE IF NOT EXISTS USER_DEFAULT_ROLES (
   ID bigint(20) NOT NULL AUTO_INCREMENT,
   SOURCE_LOCALE bigint(20) NOT NULL,
   TARGET_LOCALE bigint(20) NOT NULL,
   USER_ID varchar(100) NOT NULL,
   PRIMARY KEY (ID),
   KEY FK_LOCALE_PAIR_SOURCE_LOCALE (SOURCE_LOCALE),
   KEY FK_LOCALE_PAIR_TARGET_LOCALE (TARGET_LOCALE),
   CONSTRAINT FK_LOCALE_PAIR_SOURCE_LOCALE FOREIGN KEY (SOURCE_LOCALE) REFERENCES LOCALE (ID),
   CONSTRAINT FK_LOCALE_PAIR_TARGET_LOCALE FOREIGN KEY (TARGET_LOCALE) REFERENCES LOCALE (ID)
 ) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS USER_DEFAULT_ACTIVITIES (
   ID bigint(20) NOT NULL AUTO_INCREMENT,
   DEFAULT_ROLE_ID bigint(20) NOT NULL,
   ACTIVITY_NAME varchar(40) NOT NULL,
   PRIMARY KEY (ID),
   KEY FK_DEFAULT_ACTIVITIES (DEFAULT_ROLE_ID),
   CONSTRAINT `FK_DEFAULT_ACTIVITIES` FOREIGN KEY (DEFAULT_ROLE_ID) REFERENCES USER_DEFAULT_ROLES (ID)
 ) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
