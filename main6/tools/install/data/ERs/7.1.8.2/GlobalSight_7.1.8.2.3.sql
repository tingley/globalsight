# For GBS-1091
# 1
INSERT INTO KNOWN_FORMAT_TYPE VALUES (
43, 'Office2010 document','Microsoft Office 2010 document','office-xml',
   'MSOFFICE_IMPORTED_EVENT',
   'XML_LOCALIZED_EVENT'
);

# 2
CREATE TABLE IF NOT EXISTS `office2010_filter` (
  `ID` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `FILTER_NAME` varchar(255) NOT NULL,
  `FILTER_DESCRIPTION` varchar(4000) DEFAULT NULL,
  `UNEXTRACTABLE_WORD_PARAGRAPH_STYLES` varchar(4000) NOT NULL,
  `UNEXTRACTABLE_WORD_CHARACTER_STYLES` varchar(4000) NOT NULL,
  `IS_HEADER_TRANSLATE` char(1) NOT NULL,
  `IS_FILEINFO_TRANSLATE` char(1) NOT NULL,
  `COMPANY_ID` bigint(20) unsigned NOT NULL,
  `XML_FILTER_ID` bigint(20) NOT NULL,
  `SECOND_FILTER_ID` bigint(20) NOT NULL,    
  `SECOND_FILTER_TABLE_NAME` varchar(45) NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

# 3
select @ccc := count(*) from filter_configuration where FILTER_TABLE_NAME = 'office2010_filter';
INSERT INTO filter_configuration (NAME, KNOWN_FORMAT_ID, FILTER_TABLE_NAME, FILTER_DESCRIPTION, COMPANY_ID)
(select 'MS Office 2010 Filter','|43|','office2010_filter','The filter for MS Office 2010 files.', tb1.id from company tb1 where @ccc = 0);
