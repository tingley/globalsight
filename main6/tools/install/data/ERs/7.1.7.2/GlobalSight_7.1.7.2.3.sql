#for gbs-974
CREATE TABLE IF NOT EXISTS `job_gsedition_info` (
                      `ID` bigint(20) NOT NULL AUTO_INCREMENT,             
                      `JOB_ID` bigint(20) NOT NULL,                        
                      `ORIGINAL_TASK_ID` bigint(20) NOT NULL,              
                      `URL` varchar(100) DEFAULT NULL,                     
                      `USER_NAME` varchar(80) DEFAULT NULL,                
                      `PASSWORD` varchar(30) DEFAULT NULL,  
                      `SENDING_BACK_STATUS` varchar(50) DEFAULT 'begin',                
                      PRIMARY KEY (`ID`)                                   
                    ) ENGINE=InnoDB AUTO_INCREMENT=1000;
