##GBS-905:BS-GS Integration
CREATE TABLE RSS_FEED (                               
            ID bigint(20) NOT NULL AUTO_INCREMENT,              
            RSS_URL varchar(100) DEFAULT NULL,                  
            ENCODING varchar(20) DEFAULT NULL,                  
            VERSION varchar(6) DEFAULT NULL,                    
            TITLE varchar(200) DEFAULT NULL,                    
            LINK varchar(200) DEFAULT NULL,                     
            DESCRIPTION text,                                   
            LANGUAGE varchar(12) DEFAULT NULL,                  
            COPYRIGHT varchar(200) DEFAULT NULL,                
            IMAGE_TITLE varchar(200) DEFAULT NULL,              
            IMAGE_LINK varchar(200) DEFAULT NULL,               
            IMAGE_URL varchar(100) DEFAULT NULL,                
            IS_DEFAULT char(1) DEFAULT 'N',                     
            COMPANY_ID bigint(20) DEFAULT NULL,                 
            PRIMARY KEY (ID),                                   
            UNIQUE KEY IDX_RSS_URL (RSS_URL, COMPANY_ID)                  
          ) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;


CREATE TABLE RSS_ITEM (                                                                
            ID bigint(20) NOT NULL AUTO_INCREMENT,                                               
            FEED_ID bigint(20) DEFAULT NULL,                                                     
            TITLE varchar(200) DEFAULT NULL,                                                     
            LINK varchar(200) DEFAULT NULL,                                                      
            DESCRIPTON text,                                                                     
            AUTHOR varchar(30) DEFAULT NULL,                                                     
            PUBDATE varchar(30) DEFAULT NULL,                                                    
            STATUS int(11) DEFAULT '0',                                                          
            PUBLISHED_DATE datetime DEFAULT NULL,                                                
            IS_READ int(11) DEFAULT '0',                                                         
            PRIMARY KEY (ID),                                                                    
            KEY FK_RSS_ITEM_FEED_ID (FEED_ID),                                                 
            CONSTRAINT `FK_RSS_ITEM_FEED_ID` FOREIGN KEY (FEED_ID) REFERENCES RSS_FEED (ID)  
          ) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
          
ALTER TABLE MODULE_MAPPING DROP INDEX MODULE_MAPPING_INDEX;
ALTER TABLE MODULE_MAPPING ADD UNIQUE INDEX MODULE_MAPPING_INDEX (ID, IS_ACTIVE,COMPANY_ID,SOURCE_LOCALE,TARGET_LOCALE,SOURCE_MODULE,TARGET_MODULE);