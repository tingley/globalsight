## GBS-3528: SID support: char limit = 255. Longer resnames fail at job creation.
## create "translation_tu_tuv_attr_xx" table for every company.
## create "leverage_match_attr_xx" table for every compnay.
## create tm3 "tm3_tu_tuv_attr_shared_xx" tables for every company.

DELIMITER $$
DROP PROCEDURE IF EXISTS upgradeForGBS3528$$

CREATE PROCEDURE upgradeForGBS3528()
BEGIN
	DECLARE done INTEGER DEFAULT 0;
	DECLARE companyId BIGINT(20) DEFAULT 0;

	-- cursor
	DECLARE company_id_cur CURSOR FOR 
		SELECT id FROM company 
		WHERE IS_ACTIVE = 'Y';

	-- error handler 
	DECLARE EXIT HANDLER FOR SQLSTATE '02000' SET done = 1;
	DECLARE CONTINUE HANDLER FOR SQLSTATE '42S02' BEGIN END;

	OPEN company_id_cur;
	companyId_lable: LOOP
		FETCH company_id_cur INTO companyId;
		# logger
		SELECT companyId AS CURRENT_COMPANY_ID;

		IF done=1 THEN
			LEAVE companyId_lable;
		END IF;

		## create "translation_tu_tuv_attr_xx" table for every company.
		SET @sql1 = CONCAT(
			"CREATE TABLE TRANSLATION_TU_TUV_ATTR_", companyId, " (",
			"ID BIGINT(20) NOT NULL AUTO_INCREMENT, ",
			"OBJECT_ID BIGINT(20) DEFAULT NULL, ",
			"OBJECT_TYPE VARCHAR(20) DEFAULT NULL, ",
			"NAME VARCHAR(100) NOT NULL, ",
			"VARCHAR_VALUE VARCHAR(512) DEFAULT NULL, ",
			"TEXT_VALUE TEXT, ",
			"LONG_VALUE BIGINT(20) DEFAULT NULL, ",
			"DATE_VALUE DATETIME DEFAULT NULL, ",
			"PRIMARY KEY (ID), ",
			"KEY IDX_OBJECT_ID_TYPE (OBJECT_ID, OBJECT_TYPE, NAME) ",
			") ENGINE=INNODB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;"
		);
		## logger
		SELECT @sql1 AS SQL1;

		PREPARE stmt1 FROM @sql1;
		EXECUTE stmt1;
		DEALLOCATE PREPARE stmt1;

		## create "leverage_match_attr_xx" tables for every company.
		SET @sql2 = CONCAT(
			"CREATE TABLE LEVERAGE_MATCH_ATTR_", companyId, " (",
			"ID BIGINT(20) NOT NULL AUTO_INCREMENT, ",
			"SOURCE_PAGE_ID INT(11) DEFAULT NULL, ",
			"ORIGINAL_SOURCE_TUV_ID BIGINT(20) DEFAULT NULL, ",
			"SUB_ID VARCHAR(40) DEFAULT NULL, ",
			"TARGET_LOCALE_ID BIGINT(20) DEFAULT NULL, ",
			"ORDER_NUM SMALLINT(6) DEFAULT NULL, ",
			"NAME VARCHAR(100) NOT NULL, ",
			"VARCHAR_VALUE VARCHAR(512) DEFAULT NULL, ",
			"TEXT_VALUE TEXT, ",
			"LONG_VALUE BIGINT(20) DEFAULT NULL, ",
			"DATE_VALUE DATETIME DEFAULT NULL, ",
			"PRIMARY KEY (ID), ",
			"KEY IDX_4_UNIQUE_KEY (ORIGINAL_SOURCE_TUV_ID,SUB_ID,TARGET_LOCALE_ID,ORDER_NUM), ",
			"KEY IDX_SPID_TRGLOCID (SOURCE_PAGE_ID,TARGET_LOCALE_ID) ",
			") ENGINE=INNODB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;"
		);
		## logger
		SELECT @sql2 AS SQL2;

		PREPARE stmt2 FROM @sql2;
		EXECUTE stmt2;
		DEALLOCATE PREPARE stmt2;

		## create tm3 "tm3_tu_tuv_attr_shared_xx" tables for every company.
		SET @sql3 = CONCAT(
			"CREATE TABLE TM3_TU_TUV_ATTR_SHARED_", companyId, " (",
			"ID BIGINT(20) NOT NULL AUTO_INCREMENT, ",
			"TM_ID BIGINT(20) DEFAULT NULL, ",
			"OBJECT_ID BIGINT(20) DEFAULT NULL, ",
			"OBJECT_TYPE VARCHAR(20) DEFAULT NULL, ",
			"NAME VARCHAR(100) NOT NULL, ",
			"VARCHAR_VALUE VARCHAR(512) DEFAULT NULL, ",
			"TEXT_VALUE TEXT, ",
			"LONG_VALUE BIGINT(20) DEFAULT NULL, ",
			"DATE_VALUE DATETIME DEFAULT NULL, ",
			"PRIMARY KEY (ID), ",
			"KEY IDX_OBJECT_ID_TYPE_NAME (OBJECT_ID, OBJECT_TYPE, NAME), ",
			"KEY IDX_TM_ID (TM_ID) ",
			") ENGINE=INNODB AUTO_INCREMENT = 1;"
		);
		## logger
		SELECT @sql3 AS SQL3;

		PREPARE stmt3 FROM @sql3;
		EXECUTE stmt3;
		DEALLOCATE PREPARE stmt3;

	END LOOP;
	CLOSE company_id_cur;
    END$$

DELIMITER ;


CALL upgradeForGBS3528;
DROP PROCEDURE IF EXISTS upgradeForGBS3528;