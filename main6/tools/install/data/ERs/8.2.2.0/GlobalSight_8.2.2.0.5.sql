
CREATE TABLE IF NOT EXISTS `frame_maker_filter` (
  `ID` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `FILTER_NAME` varchar(255) NOT NULL,
  `FILTER_DESCRIPTION` varchar(2000) DEFAULT NULL,
  `COMPANY_ID` bigint(20) unsigned NOT NULL,
  `TRANSLATE_FOOT_NOTE` CHAR(1) NOT NULL DEFAULT 'Y',
  `TRANSLATE_LEFT_MASTER_PAGE` CHAR(1) NOT NULL DEFAULT 'N',
  `TRANSLATE_RIGHT_MASTER_PAGE` CHAR(1) NOT NULL DEFAULT 'N',
  `TRANSLATE_OTHER_MASTER_PAGE` CHAR(1) NOT NULL DEFAULT 'N',
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;