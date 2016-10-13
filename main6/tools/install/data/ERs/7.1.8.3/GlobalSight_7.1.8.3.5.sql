# For GBS-1540: PO Filter, By Joey

# 1
INSERT INTO filter_configuration (NAME, KNOWN_FORMAT_ID, FILTER_TABLE_NAME, FILTER_DESCRIPTION, COMPANY_ID)
  (select 'Portable Object Filter','|42|','po_filter','The filter for Portable Object files.', c.id from company c where c.id not in 
	(select distinct COMPANY_ID from filter_configuration where FILTER_TABLE_NAME = 'po_filter'));       
     
# 2
CREATE TABLE IF NOT EXISTS `po_filter` (             
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,          
  `FILTER_NAME` varchar(255) NOT NULL,              
  `FILTER_DESCRIPTION` varchar(1000) NOT NULL,      
  `COMPANY_ID` bigint(20) NOT NULL,                 
  `SECOND_FILTER_ID` bigint(20) NOT NULL,           
  `SECOND_FILTER_TABLE_NAME` varchar(45) NOT NULL,  
  PRIMARY KEY (`ID`)                                
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8;
