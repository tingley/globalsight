#for gbs-974
CREATE TABLE IF NOT EXISTS `gs_edition` (                            
              `ID` bigint(20) NOT NULL AUTO_INCREMENT,             
              `NAME` varchar(50) NOT NULL,                         
              `HOST_NAME` varchar(50) NOT NULL,                    
              `HOST_PORT` varchar(50) NOT NULL,                    
              `USER_NAME` varchar(50) NOT NULL,                    
              `PASSWORD` varchar(50) NOT NULL,                     
              `COMPANYID` bigint(20) NOT NULL,                     
              `DESCRIPTION` varchar(200) DEFAULT NULL,             
              PRIMARY KEY (`ID`)                                   
            ) ENGINE=InnoDB AUTO_INCREMENT=1000 DEFAULT CHARSET=utf8;  