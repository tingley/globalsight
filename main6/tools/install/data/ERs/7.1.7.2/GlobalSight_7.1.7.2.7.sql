# for gbs-974
CREATE TABLE IF NOT EXISTS `issue_edition_relation` (
                          `ID` bigint(20) NOT NULL AUTO_INCREMENT,
                          `ORIGINAL_TU_ID` bigint(20) DEFAULT NULL,
                          `ORIGINAL_TUV_ID` bigint(20) DEFAULT NULL,
                          `TUV_ID` bigint(20) DEFAULT NULL,
                          `ORIGINAL_ISSUE_HISTORY_ID` varchar(60) DEFAULT NULL,
                          PRIMARY KEY (`ID`)
                        ) ENGINE=InnoDB AUTO_INCREMENT=1000;
