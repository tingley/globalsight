-- # for gbs-692
CREATE TABLE IF NOT EXISTS TM_PROFILE_PROMT_INFO (
                         `ID` bigint(20) NOT NULL AUTO_INCREMENT,                                                                          
                         `TM_PROFILE_ID` bigint(20) DEFAULT NULL,                                                                          
                         `DIR_ID` bigint(20) DEFAULT NULL,                                                                                 
                         `DIR_NAME` varchar(40) DEFAULT NULL,                                                                              
                         `TOPIC_TEMPLATE_ID` varchar(40) DEFAULT NULL,                                                                     
                         PRIMARY KEY (`ID`),                                                                                               
                         KEY `FK_tm_profile_promt_info_tm_profile_id` (`TM_PROFILE_ID`),                                                   
                         CONSTRAINT `FK_tm_profile_promt_info_tm_profile_id` FOREIGN KEY (`TM_PROFILE_ID`) REFERENCES `tm_profile` (`ID`)  
                       ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE tm_profile ADD COLUMN PTSURL VARCHAR(100);