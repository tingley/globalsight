## GBS-3650: context matching needs review.

DELIMITER $$
DROP PROCEDURE IF EXISTS upgradeForGBS3650$$

CREATE PROCEDURE upgradeForGBS3650()
BEGIN
	DECLARE done INTEGER DEFAULT 0;
	DECLARE companyId BIGINT(20) DEFAULT 0;

	DECLARE lmExtTableCount INTEGER DEFAULT 0;
	DECLARE lmAttrTableCount INTEGER DEFAULT 0;

	DECLARE lmExtTable VARCHAR(60);
	DECLARE lmExtArchiveTable VARCHAR(60);
	DECLARE lmAttrTable VARCHAR(60);

	-- cursor
	DECLARE company_id_cur CURSOR FOR 
		SELECT id FROM company WHERE IS_ACTIVE = 'Y';

	-- error handler 
	DECLARE EXIT HANDLER FOR SQLSTATE '02000' SET done = 1;
	DECLARE CONTINUE HANDLER FOR SQLSTATE '42S02' BEGIN END;

	OPEN company_id_cur;
	companyId_lable: LOOP
		FETCH company_id_cur INTO companyId;
		# logger
		SELECT companyId AS CURRENT_COMPANY_ID;

		IF done = 1 THEN
			LEAVE companyId_lable;
		END IF;

		SET lmExtTable = CONCAT("leverage_match_ext_", companyId);
		SET lmExtArchiveTable = CONCAT("leverage_match_ext_", companyId, "_archived");
		SET lmAttrTable = CONCAT("leverage_match_attr_", companyId);

		## Create "leverage_match_ext_xx" tables to store extended attributes for leverage match storage.
		SELECT COUNT(*) INTO lmExtTableCount FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = lmExtTable;
		SELECT lmExtTableCount AS IS_CURRENT_LM_EXT_TABLE_EXISTED;
		IF lmExtTableCount = 0 THEN
			SET @sql1 = CONCAT(
				"CREATE TABLE ", lmExtTable, " (",
				"SOURCE_PAGE_ID INT(11) DEFAULT NULL, ",
				"ORIGINAL_SOURCE_TUV_ID BIGINT(20) DEFAULT NULL, ",
				"SUB_ID VARCHAR(40) DEFAULT NULL, ",
				"TARGET_LOCALE_ID BIGINT(20) DEFAULT NULL, ",
				"ORDER_NUM SMALLINT(6) DEFAULT NULL, ",
				"LAST_USAGE_DATE DATETIME DEFAULT NULL, ",
				"JOB_ID BIGINT(20) DEFAULT -1, ",
				"JOB_NAME VARCHAR(320) DEFAULT NULL, ",
				"PREVIOUS_HASH BIGINT(20) DEFAULT -1, ",
				"NEXT_HASH BIGINT(20) DEFAULT -1, ",
				"SID TEXT DEFAULT NULL, ",
				"varchar1 VARCHAR(512), ",
				"varchar2 VARCHAR(512), ",
				"varchar3 VARCHAR(512), ",
				"varchar4 VARCHAR(512), ",
				"text1 TEXT, ",
				"text2 TEXT, ",
				"long1 BIGINT(20), ",
				"long2 BIGINT(20), ",
				"date1 DATETIME, ",
				"date2 DATETIME, ",
				"UNIQUE KEY IDX_4_UNIQUE_KEY (ORIGINAL_SOURCE_TUV_ID, SUB_ID, TARGET_LOCALE_ID, ORDER_NUM), ",
				"KEY IDX_SPID_TRGLOCID (SOURCE_PAGE_ID, TARGET_LOCALE_ID) ",
				") ENGINE=INNODB; "
			);
			## logger
			SELECT @sql1 AS SQL1;

			PREPARE stmt1 FROM @sql1;
			EXECUTE stmt1;
			DEALLOCATE PREPARE stmt1;
			commit;
		END IF;
		SET lmExtTableCount = 0;

		## Create "leverage_match_ext_xx_archived" tables to store extended attributes for leverage match storage.
		SELECT COUNT(*) INTO lmExtTableCount FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = lmExtArchiveTable;
		SELECT lmExtTableCount AS IS_CURRENT_LM_EXT_ARCHIVE_TABLE_EXISTED;
		IF lmExtTableCount = 0 THEN
			SET @sql2 = CONCAT(
				"CREATE TABLE ", lmExtArchiveTable, " (",
				"SOURCE_PAGE_ID INT(11) DEFAULT NULL, ",
				"ORIGINAL_SOURCE_TUV_ID BIGINT(20) DEFAULT NULL, ",
				"SUB_ID VARCHAR(40) DEFAULT NULL, ",
				"TARGET_LOCALE_ID BIGINT(20) DEFAULT NULL, ",
				"ORDER_NUM SMALLINT(6) DEFAULT NULL, ",
				"LAST_USAGE_DATE DATETIME DEFAULT NULL, ",
				"JOB_ID BIGINT(20) DEFAULT -1, ",
				"JOB_NAME VARCHAR(320) DEFAULT NULL, ",
				"PREVIOUS_HASH BIGINT(20) DEFAULT -1, ",
				"NEXT_HASH BIGINT(20) DEFAULT -1, ",
				"SID TEXT DEFAULT NULL, ",
				"varchar1 VARCHAR(512), ",
				"varchar2 VARCHAR(512), ",
				"varchar3 VARCHAR(512), ",
				"varchar4 VARCHAR(512), ",
				"text1 TEXT, ",
				"text2 TEXT, ",
				"long1 BIGINT(20), ",
				"long2 BIGINT(20), ",
				"date1 DATETIME, ",
				"date2 DATETIME, ",
				"UNIQUE KEY IDX_4_UNIQUE_KEY (ORIGINAL_SOURCE_TUV_ID, SUB_ID, TARGET_LOCALE_ID, ORDER_NUM), ",
				"KEY IDX_SPID_TRGLOCID (SOURCE_PAGE_ID, TARGET_LOCALE_ID) ",
				") ENGINE=INNODB; "
			);
			## logger
			SELECT @sql2 AS SQL2;

			PREPARE stmt2 FROM @sql2;
			EXECUTE stmt2;
			DEALLOCATE PREPARE stmt2;
			commit;
		END IF;
		SET lmExtTableCount = 0;

		## Update data in "leverage_match_attr_xx" into "leverage_match_ext_xx"(for un-archived jobs)
		SELECT COUNT(*) INTO lmAttrTableCount FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = lmAttrTable;
		SELECT lmAttrTableCount AS IS_CURRENT_LM_ATTR_TABLE_EXISTED;
		IF lmAttrTableCount = 1 THEN
			SET @sql3 = CONCAT(
				"REPLACE INTO ", lmExtTable, " (SOURCE_PAGE_ID, ORIGINAL_SOURCE_TUV_ID, SUB_ID, TARGET_LOCALE_ID, ORDER_NUM, LAST_USAGE_DATE, JOB_ID, JOB_NAME, PREVIOUS_HASH,NEXT_HASH, SID) ",
				"SELECT SOURCE_PAGE_ID, ORIGINAL_SOURCE_TUV_ID, SUB_ID, TARGET_LOCALE_ID, ORDER_NUM, NULL, -1,	NULL, -1, -1, TEXT_VALUE FROM ", lmAttrTable, " attr, request, job ",
				"WHERE attr.SOURCE_PAGE_ID = request.PAGE_ID ",
				"AND request.JOB_ID = job.ID ",
				"AND job.IS_MIGRATED = 'N' ",
				"AND attr.TEXT_VALUE IS NOT NULL; "
			);
			## logger
			SELECT @sql3 AS SQL3;
			PREPARE stmt3 FROM @sql3;
			EXECUTE stmt3;
			DEALLOCATE PREPARE stmt3;
			commit;
		END IF;
		SET lmAttrTableCount = 0;

		SELECT COUNT(*) INTO lmAttrTableCount FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = lmAttrTable;
		SELECT lmAttrTableCount AS IS_CURRENT_LM_ATTR_ARCHIVE_TABLE_EXISTED;
		IF lmAttrTableCount = 1 THEN
			SET @sql4 = CONCAT(
				"REPLACE INTO ", lmExtArchiveTable, " (SOURCE_PAGE_ID, ORIGINAL_SOURCE_TUV_ID, SUB_ID, TARGET_LOCALE_ID, ORDER_NUM, LAST_USAGE_DATE, JOB_ID, JOB_NAME, PREVIOUS_HASH,NEXT_HASH, SID) ",
				"SELECT SOURCE_PAGE_ID, ORIGINAL_SOURCE_TUV_ID, SUB_ID, TARGET_LOCALE_ID, ORDER_NUM, NULL, -1,	NULL, -1, -1, TEXT_VALUE FROM ", lmAttrTable, " attr, request, job ",
				"WHERE attr.SOURCE_PAGE_ID = request.PAGE_ID ",
				"AND request.JOB_ID = job.ID ",
				"AND job.IS_MIGRATED = 'Y' ",
				"AND attr.TEXT_VALUE IS NOT NULL; "
			);
			## logger
			SELECT @sql4 AS SQL4;
			PREPARE stmt4 FROM @sql4;
			EXECUTE stmt4;
			DEALLOCATE PREPARE stmt4;
			commit;
		END IF;
		SET lmAttrTableCount = 0;

	END LOOP;
	CLOSE company_id_cur;
    END$$

DELIMITER ;


CALL upgradeForGBS3650;
DROP PROCEDURE IF EXISTS upgradeForGBS3650;