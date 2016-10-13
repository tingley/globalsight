# For GBS-519 
CREATE TABLE IF NOT EXISTS `xliff_alt` (                              
             `ID` bigint(20) NOT NULL AUTO_INCREMENT,              
             `TUV_ID` bigint(20) NOT NULL,                         
             `SEGMENT` text NOT NULL,                              
             `LANGUAGE` varchar(30) DEFAULT NULL,                  
             PRIMARY KEY (`ID`)                                    
           ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE translation_unit add Xliff_TARGET_SEGMENT text; 
ALTER TABLE translation_unit add XLIFF_TARGET_LANGUAGE varchar(30) DEFAULT NULL;
insert into `known_format_type` ( NAME, DESCRIPTION, FORMAT_TYPE, PRE_EXTRACT_EVENT, PRE_MERGE_EVENT) values ('Xliff', 'Xliff file', 'xlf', 'XML_IMPORTED_EVENT', 'XML_LOCALIZED_EVENT');


 