-- # for gbs-651
CREATE TABLE IF NOT EXISTS REMOTE_IP (
  ID BIGINT
     AUTO_INCREMENT
     PRIMARY KEY,
  IP varchar(40) NOT NULL,                                                                          
  DESCRIPTION varchar(4000)
) AUTO_INCREMENT = 1000;