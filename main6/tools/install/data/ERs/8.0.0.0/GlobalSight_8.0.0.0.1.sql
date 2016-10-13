# For TM3 

CREATE TABLE `TM3_TM` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `type` smallint(6) NOT NULL,
  `tu_table` varchar(128) DEFAULT NULL,
  `tuv_table` varchar(128) DEFAULT NULL,
  `fuzzy_table` varchar(128) DEFAULT NULL,
  `attr_val_table` varchar(128) DEFAULT NULL,
  `event_table` varchar(128) DEFAULT NULL,
  `srcLocaleId` bigint(20),
  `tgtLocaleId` bigint(20),
  `sharedStorageId` bigint(20),
  PRIMARY KEY (`id`),
  KEY(`sharedStorageId`)
) ENGINE=InnoDB DEFAULT CHARSET=UTF8;

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


CREATE TABLE `TM3_EVENTS` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `time` datetime NOT NULL,
  `userName` varchar(128) NOT NULL,
  `tmId` bigint(20) NOT NULL,
  `type` smallint(6) NOT NULL,
  `arg` varchar(512) DEFAULT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `tm3_events_ibfk_1` FOREIGN KEY (`tmId`) REFERENCES `TM3_TM` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=UTF8;

CREATE TABLE `TM3_ID` (
    `tableName` varchar(128) NOT NULL,
    `nextId` bigint(20) NOT NULL DEFAULT 0,
    PRIMARY KEY (`tableName`)
) ENGINE=MyISAM DEFAULT CHARSET=UTF8;

--
-- Changes to other tables to depend on tm3
--

alter table PROJECT_TM
    add column `TM3_ID`  bigint;

INSERT INTO SYSTEM_PARAMETER (name, value, company_id) 
        VALUES ('tm.segmenttm', 'tm2', 1);

alter table CORPUS_MAP
    ADD COLUMN TM_ID bigint(20),
    ADD KEY (TM_ID);
