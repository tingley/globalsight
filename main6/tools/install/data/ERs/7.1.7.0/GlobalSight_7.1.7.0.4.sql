# For GBS-923
CREATE TABLE IF NOT EXISTS `ms_office_excel_filter` (             
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,          
  `FILTER_NAME` varchar(255) NOT NULL,              
  `FILTER_DESCRIPTION` varchar(1000) NOT NULL,      
  `COMPANY_ID` bigint(20) NOT NULL,                 
  `SECOND_FILTER_ID` bigint(20) NOT NULL,           
  `SECOND_FILTER_TABLE_NAME` varchar(45) NOT NULL,  
  PRIMARY KEY (`ID`)                                
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8