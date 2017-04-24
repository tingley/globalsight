## GBS-4703 Consolidated SID Filter.
# 1
CREATE TABLE IF NOT EXISTS `sid_filter` (
  `ID` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `FILTER_NAME` varchar(255) NOT NULL,
  `FILTER_DESCRIPTION` varchar(4000) DEFAULT NULL,
  `COMPANY_ID` bigint(20) unsigned NOT NULL,
  `FILTER_TYPE` INT NOT NULL,
  `CONFIG_XML` text,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

# 2
select @ccc := count(*) from filter_configuration where FILTER_TABLE_NAME = 'sid_filter';
INSERT INTO filter_configuration (NAME, KNOWN_FORMAT_ID, FILTER_TABLE_NAME, FILTER_DESCRIPTION, COMPANY_ID)
(select 'SID Filter','','sid_filter','The filter for String IDs.', tb1.id from company tb1 where @ccc = 0);
INSERT INTO filter_configuration (NAME, KNOWN_FORMAT_ID, FILTER_TABLE_NAME, FILTER_DESCRIPTION, COMPANY_ID)
(select 'Global Exclusions','','global_exclusion_filter','The filter to handle extracted text.', tb1.id from company tb1 where @ccc = 0);

# 3
ALTER TABLE `java_properties_filter`   
  ADD COLUMN `SID_FILTER_ID` BIGINT(20) NULL;
  
# 4  
ALTER TABLE `filter_json`   
  ADD COLUMN `SID_FILTER_ID` BIGINT(20) NULL;

# 5   
CREATE TABLE IF NOT EXISTS `global_exclusion_filter` (
  `ID` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `FILTER_NAME` varchar(255) NOT NULL,
  `FILTER_DESCRIPTION` varchar(4000) DEFAULT NULL,
  `COMPANY_ID` bigint(20) unsigned NOT NULL,
  `FILTER_TYPE` INT NOT NULL,
  `CONFIG_XML` text,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;  