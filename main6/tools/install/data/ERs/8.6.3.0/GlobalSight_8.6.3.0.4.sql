#block login attempt

CREATE TABLE `LOGIN_ATTEMPT` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `IP` varchar(50) NOT NULL,
  `BLOCK_TIME` timestamp NOT NULL,
  `COUNT` int NOT NULL DEFAULT 10,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=1000 DEFAULT CHARSET=utf8;

alter table `login_attempt_config` 
   change `EXAMPT_IPS` `EXEMPT_IPS` varchar(4000);