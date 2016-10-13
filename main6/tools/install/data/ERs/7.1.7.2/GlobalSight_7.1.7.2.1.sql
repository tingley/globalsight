# For GBS-837
# 1
INSERT INTO KNOWN_FORMAT_TYPE VALUES ( 
40, 'INDD (CS4)','InDesign INDD CS4','indd_cs4',
   'ADOBE_IMPORTED_EVENT',
   'XML_LOCALIZED_EVENT'
);

# 2
CREATE TABLE `indd_filter` (
  `ID` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `FILTER_NAME` varchar(255) NOT NULL,
  `FILTER_DESCRIPTION` varchar(4000) DEFAULT NULL,
  `COMPANY_ID` bigint(20) unsigned NOT NULL,
  `TRANSLATE_HIDDEN_LAYER` char(1) NOT NULL DEFAULT 'N',
  `TRANSLATE_MASTER_LAYER` char(1) NOT NULL DEFAULT 'Y',
  `TRANSLATE_FILE_INFO` char(1) NOT NULL DEFAULT 'N',
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

# 3
select @ccc := count(*) from filter_configuration where FILTER_TABLE_NAME = 'indd_filter';
INSERT INTO filter_configuration (NAME, KNOWN_FORMAT_ID, FILTER_TABLE_NAME, FILTER_DESCRIPTION, COMPANY_ID)
(select 'InDesign Filter','|31|36|37|38|40|','indd_filter','The filter for InDesign files.', tb1.id from company tb1 where @ccc = 0);