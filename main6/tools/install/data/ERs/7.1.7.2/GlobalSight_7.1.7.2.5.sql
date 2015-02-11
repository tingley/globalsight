#for gbs-974
CREATE TABLE IF NOT EXISTS `gs_edition_activity` (                    
                       `ID` bigint(20) NOT NULL AUTO_INCREMENT,                             
                       `NAME` varchar(50) NOT NULL,                          
                       `FILEPROFILE` bigint(20) NOT NULL,  
                       `FILEPROFILE_NAME` varchar(50) DEFAULT NULL,                   
                       `SOURCE_FILE_REFERENCE` int(1) NOT NULL DEFAULT '0',  
                       `GSEDITION_ID` bigint(20) NOT NULL,                   
                       `DESCRIPTION` varchar(1000) DEFAULT NULL,             
                       PRIMARY KEY (`ID`)                                    
                     ) ENGINE=InnoDB AUTO_INCREMENT=1000 DEFAULT CHARSET=utf8;  