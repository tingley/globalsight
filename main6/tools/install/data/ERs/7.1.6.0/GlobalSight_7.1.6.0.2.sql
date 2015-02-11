-- #600
ALTER TABLE file_profile ADD COLUMN FILTER_ID INTEGER  default -2;
ALTER TABLE file_profile ADD COLUMN FILTER_TABLE_NAME VARCHAR(45);

CREATE TABLE `filter_configuration` (
  `ID` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `NAME` varchar(100) NOT NULL,
  `KNOWN_FORMAT_ID` varchar(250) NOT NULL,
  `FILTER_TABLE_NAME` varchar(60) NOT NULL,
  `FILTER_DESCRIPTION` varchar(4000) DEFAULT NULL,
  `COMPANY_ID` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `filter_configuration` 
VALUES 
(1,'Java Properties Filter','|4|10|11|','java_properties_filter','The filter for java properties files.',1),
(2,'Java Script Filter','|5|','java_script_filter','The filter for java script files.',1),
(3,'MS Office Doc Filter','|14|33|','ms_office_doc_filter','The filter for MS office doc files.',1),
(4,'XML Rule Filter','|7|15|16|17|25|','xml_rule_filter','The filter for XML Rules.',1),
(5,'HTML Filter','|1|','html_filter','The filter for HTML files.',1);

CREATE TABLE `java_properties_filter` (
  `ID` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `FILTER_NAME` varchar(255) NOT NULL,
  `FILTER_DESCRIPTION` varchar(4000) DEFAULT NULL,
  `ENABLE_SID_SUPPORT` char(1) NOT NULL DEFAULT 'N',
  `ENABLE_UNICODE_ESCAPE` char(1) NOT NULL DEFAULT 'N',
  `COMPANY_ID` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `java_properties_filter` VALUES 
(1,'Java Properties filter (Default)','The default java properties filter.','N','N',1);

CREATE TABLE `java_script_filter` (
  `ID` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `FILTER_NAME` varchar(255) NOT NULL,
  `FILTER_DESCRIPTION` varchar(4000) DEFAULT NULL,
  `JS_FUNCTION_FILTER` varchar(1000) NOT NULL,
  `COMPANY_ID` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `java_script_filter`
--
INSERT INTO `java_script_filter` VALUES 
(1,'Java Script Filter(Default)','The default java script filter.','l10n',1);

CREATE TABLE `ms_office_doc_filter` (
  `ID` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `FILTER_NAME` varchar(255) NOT NULL,
  `FILTER_DESCRIPTION` varchar(4000) DEFAULT NULL,
  `IS_HEADER_TRANSLATE` char(1) NOT NULL,
  `COMPANY_ID` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `ms_office_doc_filter`
--
INSERT INTO `ms_office_doc_filter` VALUES 
(1,'MS Office Doc Filter(Default)','The default MS Office Doc filter.','Y',1);

CREATE TABLE `xml_rule_filter` (
  `ID` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `FILTER_NAME` varchar(255) NOT NULL,
  `FILTER_DESCRIPTION` varchar(4000) DEFAULT NULL,
  `XML_RULE_ID` bigint(20) NOT NULL DEFAULT '-1',
  `COMPANY_ID` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `xml_rule_filter` VALUES 
(1,'XML Rule Filter(Default)','The default XML rule filter.',1,1);

