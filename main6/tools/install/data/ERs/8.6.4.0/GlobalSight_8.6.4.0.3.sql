# GBS-3956: add last usage date to tm.
# GBS-3676: Record job name at TUV level in Translation Memory.
# GBS-3650: context matching needs review.

DELIMITER $$
DROP PROCEDURE IF EXISTS upgradeForGBS3676$$

CREATE PROCEDURE upgradeForGBS3676()
BEGIN
	DECLARE done INTEGER DEFAULT 0;

	DECLARE companyId BIGINT(20) DEFAULT 0;
	DECLARE tuvExtTableCount INTEGER DEFAULT 0;
	DECLARE tuTuvAttrTableCount INTEGER DEFAULT 0;

	DECLARE tuTable VARCHAR(60);
	DECLARE tuvTable VARCHAR(60);
	DECLARE tuvExtTable VARCHAR(60);
	DECLARE tuTuvAttrTable VARCHAR(60);

	-- cursor
	DECLARE company_id_cur CURSOR FOR 
		SELECT id FROM company WHERE IS_ACTIVE = 'Y' AND id != 1;

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

		SET tuTable = CONCAT("tm3_tu_shared_", companyId);
		SET tuvTable = CONCAT("tm3_tuv_shared_", companyId);
		SET tuvExtTable = CONCAT("tm3_tuv_ext_shared_", companyId);
		SET tuTuvAttrTable = CONCAT("tm3_tu_tuv_attr_shared_", companyId);

		## Create "tm3_tuv_ext_shared_xx" tables to store extended attributes for TUV storage.
		SELECT COUNT(*) INTO tuvExtTableCount FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = tuvExtTable;
		SELECT tuvExtTableCount AS IS_CURRENT_TUV_EXT_TABLE_EXISTED;
		IF tuvExtTableCount = 0 THEN
			SET @sql1 = CONCAT(
				"CREATE TABLE ", tuvExtTable, " (",
				"tuvId BIGINT(20) NOT NULL, ",
				"tuId BIGINT(20) NOT NULL, ",
				"tmId BIGINT(20) NOT NULL, ",
				"lastUsageDate DATETIME DEFAULT NULL, ",
				"jobId BIGINT(20) DEFAULT -1, ",
				"jobName VARCHAR(320) DEFAULT NULL, ",
				"previousHash BIGINT(20) DEFAULT -1, ",
				"nextHash BIGINT(20) DEFAULT -1, ",
				"sid TEXT DEFAULT NULL, ",
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
				"UNIQUE KEY tuvId (tuvId), ",
				"KEY tuId (tuId), ",
				"KEY tmId (tmId)",
				") ENGINE=INNODB;"
			);
			## logger
			SELECT @sql1 AS SQL1;

			PREPARE stmt1 FROM @sql1;
			EXECUTE stmt1;
			DEALLOCATE PREPARE stmt1;
		END IF;
		SET tuvExtTableCount = 0;

		## update SID from "tm3_tu_tuv_attr_shared_xx" into "tm3_tuv_ext_shared_xx" table.
		SELECT COUNT(*) INTO tuTuvAttrTableCount FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = tuTuvAttrTable;
		SELECT tuTuvAttrTableCount AS IS_CURRENT_ATTR_TABLE_EXISTED;
		IF tuTuvAttrTableCount = 1 THEN
			SET @sql2 = CONCAT(
				"REPLACE INTO ", tuvExtTable, " (tuvId, tuId, tmId, lastUsageDate, jobId, jobName, previousHash, nextHash, sid) ",
				"SELECT tuv.id, attr.object_id, attr.tm_id, NULL, -1, NULL, -1, -1, attr.text_value FROM ", tuTuvAttrTable, " attr, ", tuvTable, " tuv "
				"WHERE attr.object_id = tuv.tuId ",
				"AND attr.name = 'SID' ",
				"AND attr.OBJECT_TYPE = 'TU' ",	
				"AND attr.text_value != 'N/A'; "
			);
			## logger
			SELECT @sql2 AS SQL2;
			PREPARE stmt2 FROM @sql2;
			EXECUTE stmt2;
			DEALLOCATE PREPARE stmt2;
		END IF;
		SET tuTuvAttrTableCount = 0;

		## update SID into "tm3_tuv_ext_shared_xx" from tm3_tu_shared_xx" table.
		SET @sql3 = CONCAT(
			"REPLACE INTO ", tuvExtTable, " (tuvId, tuId, tmId, lastUsageDate, jobId, jobName, previousHash, nextHash, sid) ",
			"SELECT tuv.id, tuv.tuId, tuv.tmId, NULL, -1, NULL, -1, -1, tu.sid FROM ", tuTable, " tu, ", tuvTable, " tuv ",
			"WHERE tu.id = tuv.tuId ",
			"AND tu.sid IS NOT NULL; "
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


CALL upgradeForGBS3676;
DROP PROCEDURE IF EXISTS upgradeForGBS3676;