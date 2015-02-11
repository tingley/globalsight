--
-- Table structure for table `tm3_tm`
--

DROP TABLE IF EXISTS `TM3_TM`;
CREATE TABLE `TM3_TM` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `type` smallint(6) NOT NULL,
  `tu_table` varchar(128) DEFAULT NULL,
  `tuv_table` varchar(128) DEFAULT NULL,
  `fuzzy_table` varchar(128) DEFAULT NULL,
  `attr_val_table` varchar(128) DEFAULT NULL,
  `srcLocaleId` bigint(20),
  `tgtLocaleId` bigint(20),
  `sharedStorageId` bigint(20),
  PRIMARY KEY (`id`),
  KEY(`sharedStorageId`)
) ENGINE=InnoDB DEFAULT CHARSET=UTF8;

--
-- Table structure for table `tm3_attr`
--

DROP TABLE IF EXISTS `TM3_ATTR`;
CREATE TABLE `TM3_ATTR` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tmId` bigint(20) NOT NULL,
  `name` varchar(128) NOT NULL,
  `columnName` varchar(32),
  `valueType` varchar(128) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `tmId` (`tmId`,`name`),
  CONSTRAINT `tm3_attr_ibfk_1` FOREIGN KEY (`tmId`) REFERENCES `TM3_TM` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=UTF8;


--
-- Table structure for table `tm3_events`
--

DROP TABLE IF EXISTS `TM3_EVENTS`;
CREATE TABLE `TM3_EVENTS` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `time` datetime NOT NULL,
  `userName` varchar(128) NOT NULL,
  `tmId` bigint(20) NOT NULL,
  `type` smallint(6) NOT NULL,
  `arg` text DEFAULT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `tm3_events_ibfk_1` FOREIGN KEY (`tmId`) REFERENCES `TM3_TM` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=UTF8;

DROP TABLE IF EXISTS `TM3_ID`;
CREATE TABLE `TM3_ID` (
    `tableName` varchar(128) NOT NULL,
    `nextId` bigint(20) NOT NULL DEFAULT 0,
    PRIMARY KEY (`tableName`)
) ENGINE=MyISAM DEFAULT CHARSET=UTF8;


DROP TABLE IF EXISTS `LOCALE`;
CREATE TABLE `LOCALE` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `ISO_LANG_CODE` varchar(3) NOT NULL,
  `ISO_COUNTRY_CODE` varchar(3) DEFAULT NULL,
  `IS_UI_LOCALE` char(1) NOT NULL DEFAULT 'N',
  PRIMARY KEY (`ID`),
  KEY `FK_LOCALE_ISO_LANG_CODE` (`ISO_LANG_CODE`),
  KEY `INDEX_IS_UI_LOCALE` (`IS_UI_LOCALE`),
  KEY `INDEX_ISO_COUNTRY_LANG` (`ISO_COUNTRY_CODE`,`ISO_LANG_CODE`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `locale`
--

INSERT INTO `LOCALE` VALUES (1,'ar','AE','N'),(2,'ar','BH','N'),(3,'ar','DZ','N'),(4,'ar','EG','N'),(5,'ar','IQ','N'),(6,'ar','JO','N'),(7,'ar','KW','N'),(8,'ar','LB','N'),(9,'ar','LY','N'),(10,'ar','MA','N'),(11,'ar','OM','N'),(12,'ar','QA','N'),(13,'ar','SA','N'),(14,'ar','SD','N'),(15,'ar','SY','N'),(16,'ar','TN','N'),(17,'ar','YE','N'),(18,'be','BY','N'),(19,'bg','BG','N'),(20,'ca','ES','N'),(21,'cs','CZ','N'),(22,'da','DK','N'),(23,'de','AT','N'),(24,'de','CH','N'),(25,'de','DE','N'),(26,'el','GR','N'),(27,'en','AU','N'),(28,'en','CA','N'),(29,'en','GB','N'),(30,'en','IE','N'),(31,'en','NZ','N'),(32,'en','US','Y'),(33,'en','ZA','N'),(34,'es','AR','N'),(35,'es','BO','N'),(36,'es','CL','N'),(37,'es','CO','N'),(38,'es','CR','N'),(39,'es','DO','N'),(40,'es','EC','N'),(41,'es','ES','N'),(42,'es','GT','N'),(43,'es','HN','N'),(44,'es','MX','N'),(45,'es','NI','N'),(46,'es','PA','N'),(47,'es','PE','N'),(48,'es','PR','N'),(49,'es','PY','N'),(50,'es','SV','N'),(51,'es','UY','N'),(52,'es','VE','N'),(53,'et','EE','N'),(54,'fi','FI','N'),(55,'fr','CA','N'),(56,'fr','CH','N'),(57,'fr','FR','N'),(58,'he','IL','N'),(59,'hr','HR','N'),(60,'hu','HU','N'),(61,'id','ID','N'),(62,'is','IS','N'),(63,'it','CH','N'),(64,'it','IT','N'),(65,'ja','JP','N'),(66,'ko','KR','N'),(67,'lt','LT','N'),(68,'lv','LV','N'),(69,'mk','MK','N'),(70,'nl','BE','N'),(71,'nl','NL','N'),(72,'no','NO','N'),(73,'pl','PL','N'),(74,'pt','BR','N'),(75,'pt','PT','N'),(76,'ro','RO','N'),(77,'ru','RU','N'),(78,'sh','YU','N'),(79,'sk','SK','N'),(80,'sl','SI','N'),(81,'sq','AL','N'),(82,'sr','YU','N'),(83,'sv','SE','N'),(84,'th','TH','N'),(85,'tr','TR','N'),(86,'uk','UA','N'),(87,'zh','CN','N'),(88,'zh','TW','N'),(89,'zh','HK','N'),(90,'es','EI','N'),(91,'az','AZ','N'),(92,'ms','BN','N'),(93,'hi','IN','N'),(94,'es','LAS','N'),(95,'es','LX','N'),(96,'es','EM','N'),(97,'vi','VN','N'),(98,'mt','MT','N'),(99,'eu','ES','N');

-- Additional locales for TDA
insert into LOCALE (iso_lang_code, iso_country_code) values
    ('ar','AR'), ('cy','GB'), ('es','XL'), ('fa','IR'), ('fr','BE'),
    ('ht','HT'), ('nb','NO'), ('nn','NO');
