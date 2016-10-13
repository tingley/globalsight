# For GBS-1001 webservice APIs
CREATE TABLE IF NOT EXISTS `remote_access_history` (
                         `ID` bigint(20) NOT NULL AUTO_INCREMENT,
                         `ACCESS_TOKEN` varchar(60) DEFAULT NULL,
                         `API_NAME` varchar(60) DEFAULT NULL,
                         `USER_ID` varchar(25) DEFAULT NULL,
                         `CREATE_DATE` datetime DEFAULT NULL,
                         `LAST_ACCESS_DATE` datetime DEFAULT NULL,
                         `CONTENTS` text,
                         PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
