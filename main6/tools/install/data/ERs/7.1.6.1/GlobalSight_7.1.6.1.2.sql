CREATE TABLE IF NOT EXISTS AUTOACTION (                             
              ID bigint(20) NOT NULL AUTO_INCREMENT,              
              NAME varchar(40) NOT NULL,                          
              EMAIL varchar(50) NOT NULL,                         
              DESCRIPTION varchar(200) DEFAULT NULL,
              COMPANY_ID bigint(20),
              PRIMARY KEY (ID)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8;


ALTER TABLE ACTIVITY ADD COLUMN AUTOMATIC_ACTION_ID VARCHAR(20) NULL;