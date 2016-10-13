# For GBS-1327: Excel Filter, By Joey on 2010/08/

# 1
select @ccc := count(*) from filter_configuration where FILTER_TABLE_NAME = 'ms_office_ppt_filter';
INSERT INTO filter_configuration (NAME, KNOWN_FORMAT_ID, FILTER_TABLE_NAME, FILTER_DESCRIPTION, COMPANY_ID)
(select 'MS Office PowerPoint Filter','|20|35|','ms_office_ppt_filter','The filter for MS PowerPoint files.', tb1.id from company tb1 where @ccc = 0);

# 2
CREATE TABLE IF NOT EXISTS `ms_office_ppt_filter` (             
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,          
  `FILTER_NAME` varchar(255) NOT NULL,              
  `FILTER_DESCRIPTION` varchar(1000) NOT NULL,      
  `COMPANY_ID` bigint(20) NOT NULL,                 
  `SECOND_FILTER_ID` bigint(20) NOT NULL,           
  `SECOND_FILTER_TABLE_NAME` varchar(45) NOT NULL,  
  PRIMARY KEY (`ID`)                                
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8;
  
# 3
ALTER TABLE ms_office_doc_filter ADD COLUMN SECOND_FILTER_ID bigint(20) NOT NULL;
ALTER TABLE ms_office_doc_filter ADD COLUMN SECOND_FILTER_TABLE_NAME varchar(45) NOT NULL;
