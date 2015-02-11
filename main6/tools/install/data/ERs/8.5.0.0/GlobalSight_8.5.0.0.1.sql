# GBS-3016: Use one TM profile and Loc profile for all locales whether all or some languages require MT

# If have "tmp.new(54)" permission, it should have "mtp.view(388)", "mtp.new(389)" and "mtp.edit(390)" permissions too.
UPDATE permissiongroup
SET permission_set = CONCAT(permission_set, '388|')
WHERE permission_set LIKE '%|54|%'
AND permission_set NOT LIKE '%|388|%';

UPDATE permissiongroup
SET permission_set = CONCAT(permission_set, '389|')
WHERE permission_set LIKE '%|54|%'
AND permission_set NOT LIKE '%|389|%';

UPDATE permissiongroup
SET permission_set = CONCAT(permission_set, '390|')
WHERE permission_set LIKE '%|54|%'
AND permission_set NOT LIKE '%|390|%';

ALTER TABLE l10n_profile_wftemplate_info ADD COLUMN MT_PROFILE_ID BIGINT(20) DEFAULT -1;

CREATE TABLE mt_profile (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `MT_PROFILE_NAME` varchar(60) DEFAULT NULL,
  `MT_ENGINE` varchar(60) DEFAULT NULL,
  `DESCRIPTION` varchar(200) DEFAULT NULL,
  `MT_CONFIDENCE_SCORE` bigint(20) DEFAULT 0,
  `URL` varchar(100) DEFAULT NULL,
  `PORT` int(11) DEFAULT NULL,
  `USERNAME` varchar(50) DEFAULT NULL,
  `PASSWORD` varchar(100) DEFAULT NULL,
  `CATEGORY` varchar(50) DEFAULT NULL,
  `ACCOUNTINFO` varchar(100) DEFAULT NULL,
  `COMPANY_ID` bigint(20) DEFAULT NULL,
  `TIMESTAMP` timestamp NULL DEFAULT NULL,
  `SHOW_IN_EDITOR` char(1) DEFAULT NULL,
  `IS_ACTIVE` char(1) DEFAULT NULL,
  `EXTENT_JSON_INFO` longtext,
  PRIMARY KEY (`ID`)
) AUTO_INCREMENT = 1000;

CREATE TABLE mt_profile_extent_info (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `MT_PROFILE_ID` bigint(20) DEFAULT NULL,
  `LANGUAGE_PAIR_CODE` bigint(20) DEFAULT NULL,
  `LANGUAGE_PAIR_NAME` varchar(100) DEFAULT NULL,
  `DOMAIN_CODE` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`ID`)
)  AUTO_INCREMENT = 1000;
