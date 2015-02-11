# For GBS-1163
# 1
INSERT INTO KNOWN_FORMAT_TYPE VALUES ( 
41, 'OpenOffice document','OpenOffice document(odt, ods, odp)','openoffice-xml',
   'OPENOFFICE_IMPORTED_EVENT',
   'XML_LOCALIZED_EVENT'
);

# 2
CREATE TABLE `openoffice_filter` (
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
select @ccc := count(*) from filter_configuration where FILTER_TABLE_NAME = 'openoffice_filter';
INSERT INTO filter_configuration (NAME, KNOWN_FORMAT_ID, FILTER_TABLE_NAME, FILTER_DESCRIPTION, COMPANY_ID)
(select 'OpenOffice Filter','|41|','openoffice_filter','The filter for OpenOffice files.', tb1.id from company tb1 where @ccc = 0);

#4
select @cccOfExt := count(*) from EXTENSION where NAME = 'odt';
INSERT INTO EXTENSION (NAME, COMPANY_ID) (select 'odt', tb1.id from company tb1 where @cccOfExt = 0);
INSERT INTO EXTENSION (NAME, COMPANY_ID) (select 'ods', tb1.id from company tb1 where @cccOfExt = 0);
INSERT INTO EXTENSION (NAME, COMPANY_ID) (select 'odp', tb1.id from company tb1 where @cccOfExt = 0);

#5 system parameters
insert into SYSTEM_PARAMETER (ID, NAME, VALUE, COMPANY_ID) VALUES (72, 'openoffice.install.key', 'false', '1');
insert into SYSTEM_PARAMETER (ID, NAME, VALUE, COMPANY_ID) VALUES (73, 'openoffice.install.dir', 'C:/Program Files/OpenOffice.org 3', '1');