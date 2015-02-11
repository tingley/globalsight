# gbs-3012
# 1
CREATE TABLE IF NOT EXISTS `plain_text_filter` (
  `ID` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `FILTER_NAME` varchar(255) NOT NULL,
  `FILTER_DESCRIPTION` varchar(4000) DEFAULT NULL,
  `COMPANY_ID` bigint(20) unsigned NOT NULL,
  `CONFIG_XML` text,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

# 2
select @ccc := count(*) from filter_configuration where FILTER_TABLE_NAME = 'plain_text_filter';
INSERT INTO filter_configuration (NAME, KNOWN_FORMAT_ID, FILTER_TABLE_NAME, FILTER_DESCRIPTION, COMPANY_ID)
(select 'Plain Text Filter','|6|','plain_text_filter','The filter for Plain Text files', tb1.id from company tb1 where @ccc = 0);

# 3
update filter_configuration set KNOWN_FORMAT_ID='|0|' where FILTER_TABLE_NAME = 'base_filter';
