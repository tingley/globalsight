CREATE TABLE IF NOT EXISTS module_mapping_di (                                                 
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

CREATE TABLE IF NOT EXISTS module_rename_di (                                                                            
                 ID BIGINT(20) NOT NULL AUTO_INCREMENT,                                                                
                 SOURCE_NAME VARCHAR(40) NOT NULL,                                                                     
                 TARGET_NAME VARCHAR(40) NOT NULL,                                                                     
                 MODULE_MAPPING_ID BIGINT(20) DEFAULT NULL,                                                            
                 PRIMARY KEY (ID),                                                                                     
                 KEY FK_MODULE_MAPPING_DI_ID (MODULE_MAPPING_ID),                                                       
                 CONSTRAINT FK_MODULE_MAPPING_DI_ID FOREIGN KEY (MODULE_MAPPING_ID) REFERENCES module_mapping_di (ID)  
               ) ENGINE=INNODB DEFAULT CHARSET=utf8;
