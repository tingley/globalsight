# gbs-1886

# 1
CREATE TABLE IF NOT EXISTS `base_filter` (
  `ID` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `FILTER_NAME` varchar(255) NOT NULL,
  `FILTER_DESCRIPTION` varchar(4000) DEFAULT NULL,
  `COMPANY_ID` bigint(20) unsigned NOT NULL,
  `CONFIG_XML` text,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `base_filter_mapping` (
  `ID` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `base_filter_id` bigint(20) NOT NULL,
  `filter_table_name` varchar(60) NOT NULL,
  `filter_id` bigint(20) NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

# 2
select @ccc := count(*) from filter_configuration where FILTER_TABLE_NAME = 'base_filter';
INSERT INTO filter_configuration (NAME, KNOWN_FORMAT_ID, FILTER_TABLE_NAME, FILTER_DESCRIPTION, COMPANY_ID)
(select 'Internal Text Filter','|6|','base_filter','The filter to handle internal text.', tb1.id from company tb1 where @ccc = 0);

# 3
INSERT INTO SYSTEM_PARAMETER (`NAME`, `VALUE`, `COMPANY_ID`) VALUES ('doPropertyInternalTextMigration', 'true', '1');