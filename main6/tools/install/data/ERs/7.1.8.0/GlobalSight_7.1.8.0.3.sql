# for gbs-1112
CREATE TABLE `tu_concept_relation` (        
                       `ID` bigint(20) NOT NULL AUTO_INCREMENT,  
                       `TU_ID` bigint(20) DEFAULT NULL,          
                       `CONCEPT_ID` bigint(20) DEFAULT NULL, 
                       `ADDED_LANGUAGE` varchar(255) DEFAULT NULL,     
                       PRIMARY KEY (`ID`)                        
                     ) ENGINE=InnoDB DEFAULT CHARSET=utf8 