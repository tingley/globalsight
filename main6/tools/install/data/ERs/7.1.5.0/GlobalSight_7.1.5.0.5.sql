-- for CVS Module-Mapping (GBS-448)
CREATE TABLE IF NOT EXISTS module_mapping (                                                 
                  ID bigint(20) NOT NULL AUTO_INCREMENT,                                      
                  SOURCE_LOCALE varchar(40) NOT NULL,                                         
                  SOURCE_lOCALE_LONG varchar(100) DEFAULT NULL,                               
                  SOURCE_MODULE varchar(200) NOT NULL,                                        
                  TARGET_LOCALE varchar(40) NOT NULL,                                         
                  TARGET_LOCALE_LONG varchar(100) DEFAULT NULL,                               
                  TARGET_MODULE varchar(200) NOT NULL,  
                  USER_ID varchar(200) NOT NULL,
                  IS_ACTIVE char(1) DEFAULT '1',                                              
                  PRIMARY KEY (ID),                                                           
                  UNIQUE KEY SOURCE_LOCALE (SOURCE_LOCALE,TARGET_LOCALE,SOURCE_MODULE,TARGET_MODULE,USER_ID)  
                ) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS module_rename (                                                                            
                 ID bigint(20) NOT NULL AUTO_INCREMENT,                                                                
                 SOURCE_NAME varchar(40) NOT NULL,                                                                     
                 TARGET_NAME varchar(40) NOT NULL,                                                                     
                 MODULE_MAPPING_ID bigint(20) DEFAULT NULL,                                                            
                 PRIMARY KEY (ID),                                                                                     
                 KEY FK_MODULE_MAPPING_ID (MODULE_MAPPING_ID),                                                       
                 CONSTRAINT FK_MODULE_MAPPING_ID FOREIGN KEY (MODULE_MAPPING_ID) REFERENCES module_mapping (ID)  
               ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE target_page ADD COLUMN CVS_TARGET_MODULE VARCHAR(2000) NULL;
ALTER TABLE target_page ADD COLUMN CVS_TARGET_FILENAME VARCHAR(2000) NULL;
commit;