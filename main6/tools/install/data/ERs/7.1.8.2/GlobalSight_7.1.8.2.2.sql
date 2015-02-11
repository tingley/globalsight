# For GBS-1353: TDA TM, By walter

CREATE TABLE IF NOT EXISTS tda_tm (                                
          `ID` bigint(20) NOT NULL AUTO_INCREMENT,             
          `TDAENABLE` tinyint(1) NOT NULL,                     
          `HOST_NAME` varchar(50) NOT NULL,                    
          `USERNAME` varchar(50) NOT NULL,                     
          `PASSWORD` varchar(50) NOT NULL,                     
          `DESCRIPTION` varchar(200) DEFAULT NULL,             
          `TM_FIPROFILE_ID` bigint(20) NOT NULL,               
          PRIMARY KEY (`ID`)                                   
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 