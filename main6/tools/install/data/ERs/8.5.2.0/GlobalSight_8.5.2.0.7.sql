## GBS-3264: GlobalSight puts wrong TU timestamp in TMX causing OmegaT to pick the wrong match.
## Add "CREATION_DATE" and "MODIFY_USER" columns to all leverage match tables.

DELIMITER $$
DROP PROCEDURE IF EXISTS schemaUpgradeForGBS3264$$

CREATE PROCEDURE schemaUpgradeForGBS3264()
BEGIN
	DECLARE done INTEGER DEFAULT 0;
	DECLARE lmTableName VARCHAR(30);
	DECLARE counter INTEGER DEFAULT 0;
	
	-- cursor
	DECLARE lmTableName_cur CURSOR FOR 
		SELECT table_name FROM information_schema.TABLES
		WHERE table_schema = DATABASE()
		AND table_name LIKE 'leverage_match%';

	-- error handler 
	DECLARE EXIT HANDLER FOR SQLSTATE '02000' SET done = 1;
	DECLARE CONTINUE HANDLER FOR SQLSTATE '42S02' BEGIN END;

	OPEN lmTableName_cur;
	lmTableName_lable: LOOP
		FETCH lmTableName_cur INTO lmTableName;
		# logger
		SELECT lmTableName AS CURRENT_LEVERAGE_MATCH_TABLE_NAME;

		IF done=1 THEN
			LEAVE lmTableName_lable;
		END IF;

		## 1. Check if this table has been added the new columns. If counter < 1, add them.
		SELECT COUNT(*) INTO counter FROM information_schema.COLUMNS WHERE table_schema = DATABASE() AND table_name = lmTableName AND column_name = 'CREATION_DATE';
		-- logger
		SELECT counter AS COLUMNS_HAVE_BEEN_ADDED;
		IF counter < 1 THEN
			SET @sql1 = CONCAT(
				"ALTER TABLE ", lmTableName, " ADD COLUMN CREATION_DATE DATETIME DEFAULT NULL AFTER CREATION_USER, ADD COLUMN MODIFY_USER VARCHAR(80) DEFAULT NULL;"
			);
			-- logger
			SELECT @sql1 AS ADD_COLUMNS_SQL;

			SELECT CONCAT('Begin to add columns for ', lmTableName) AS BEGIN_TO_ADD_COLUMNS;
			PREPARE stmt1 FROM @sql1;
			EXECUTE stmt1;
			DEALLOCATE PREPARE stmt1;
			SELECT CONCAT('End to add columns for ', lmTableName) AS END_TO_ADD_COLUMNS;

			SET @sql2 = CONCAT("UPDATE ", lmTableName, " SET creation_date = modify_date WHERE creation_date IS NULL AND modify_date IS NOT NULL;");
			SELECT @sql2 AS UPDATE_MODIFY_DATE_AS_CREATION_DATE_SQL;
			PREPARE stmt2 FROM @sql2;
			EXECUTE stmt2;
			DEALLOCATE PREPARE stmt2;
		END IF;

		COMMIT;
		SET counter = 0;
	END LOOP;
	CLOSE lmTableName_cur;
    END$$

DELIMITER ;


CALL schemaUpgradeForGBS3264;

DROP PROCEDURE IF EXISTS schemaUpgradeForGBS3264;