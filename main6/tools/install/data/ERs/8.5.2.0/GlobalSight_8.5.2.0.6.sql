## GBS-3324: GlobalSight changes TUV creationdate/changedate during TMX/G-TMX import.
## Update "creationUser", "creationDate", "modifyUser", "modifyDate" according to "firstEventId" and "lastEventId".
## The first event "time" and "userName" are for "creationDate" and "creationUser".
## The last event "time" and "userName" are for "modifyDate" and "modifyUser".

DELIMITER $$
DROP PROCEDURE IF EXISTS dataUpgradeForGBS3324$$

CREATE PROCEDURE dataUpgradeForGBS3324()
BEGIN
	DECLARE done INTEGER DEFAULT 0;
	DECLARE tm3TuvTableName VARCHAR(30);
	DECLARE counter INTEGER DEFAULT 0;
	
	-- cursor
	DECLARE tm3TuvTableName_cur CURSOR FOR 
		SELECT table_name FROM information_schema.TABLES
		WHERE table_schema = DATABASE()
		AND table_name LIKE 'tm3_tuv_shared_%';

	-- error handler 
	DECLARE EXIT HANDLER FOR SQLSTATE '02000' SET done = 1;
	DECLARE CONTINUE HANDLER FOR SQLSTATE '42S02' BEGIN END;

	OPEN tm3TuvTableName_cur;
	tm3TuvTableName_lable: LOOP
		FETCH tm3TuvTableName_cur INTO tm3TuvTableName;
		# logger
		SELECT tm3TuvTableName AS CURRENT_TM3_TUV_TABLE_NAME;

		IF done=1 THEN
			LEAVE tm3TuvTableName_lable;
		END IF;

		## 1. Check if this table has been added the new columns. If counter < 1, add them.
		SELECT COUNT(*) INTO counter FROM information_schema.COLUMNS WHERE table_schema = DATABASE() AND table_name = tm3TuvTableName AND column_name = 'creationUser';
		-- logger
		SELECT counter AS COLUMNS_HAVE_BEEN_ADDED;
		IF counter < 1 THEN
			SET @sql1 = CONCAT(
				"ALTER TABLE ", tm3TuvTableName, " ",
				"ADD COLUMN creationUser VARCHAR(80) DEFAULT NULL, ",
				"ADD COLUMN creationDate DATETIME NOT NULL, ",
				"ADD COLUMN modifyUser VARCHAR(80) DEFAULT NULL, ",
				"ADD COLUMN modifyDate DATETIME NOT NULL;"
			);
			-- logger
			SELECT @sql1 AS ADD_COLUMNS_SQL;

			SELECT CONCAT('Begin to add columns for ', tm3TuvTableName) AS BEGIN_TO_ADD_COLUMNS;
			PREPARE stmt1 FROM @sql1;
			EXECUTE stmt1;
			DEALLOCATE PREPARE stmt1;
			SELECT CONCAT('End to add columns for ', tm3TuvTableName) AS END_TO_ADD_COLUMNS;
		END IF;

		## 2. Update "modifyUser" and "modifyDate" by "lastEventId" FIRST!!!
		SET @sql2 = CONCAT(
			"UPDATE ", tm3TuvTableName, " tuv, ",
			"(SELECT tuv.id AS tuvId, event.id AS eventId, event.time AS eventTime, event.userName AS eventUserName FROM ", tm3TuvTableName, " tuv, tm3_events event WHERE tuv.lastEventId = event.id AND tuv.creationUser IS NULL) AS tuv2 ",
			"SET tuv.modifyUser = tuv2.eventUserName, tuv.modifyDate = tuv2.eventTime ",
			"WHERE tuv.id = tuv2.tuvId;"
		);
		-- logger
		-- SELECT @sql2 AS UPDATE_MODIFY_COLUMNS_SQL;

		-- logger
		SELECT CONCAT('Begin to update modifyUser and modifyDate for ', tm3TuvTableName) AS BEGIN_TO_UPDATE_MODIFY_INFO;
		PREPARE stmt2 FROM @sql2;
		EXECUTE stmt2;
		DEALLOCATE PREPARE stmt2;
		-- logger
		SELECT CONCAT('Finished to update modifyUser and modifyDate for ', tm3TuvTableName) AS END_TO_UPDATE_MODIFY_INFO;

		## 3. Update "creationUser" and "creationDate" by "firstEventId".
		SET @sql3 = CONCAT(
			"UPDATE ", tm3TuvTableName, " tuv, ",
			"(SELECT tuv.id AS tuvId, event.id AS eventId, event.time AS eventTime, event.userName AS eventUserName FROM ", tm3TuvTableName, " tuv, tm3_events event WHERE tuv.firstEventId = event.id AND tuv.creationUser IS NULL) AS tuv2 ",
			"SET tuv.creationUser = tuv2.eventUserName, tuv.creationDate = tuv2.eventTime ",
			"WHERE tuv.id = tuv2.tuvId;"
		);
		-- logger
		-- SELECT @sql3 AS UPDATE_CREATION_COLUMNS_SQL;

		-- logger
		SELECT CONCAT('Begin to update creationUser and creationDate for ', tm3TuvTableName) AS BEGIN_TO_UPDATE_CREATION_INFO;
		PREPARE stmt3 FROM @sql3;
		EXECUTE stmt3;
		DEALLOCATE PREPARE stmt3;
		-- logger
		SELECT CONCAT('Finished to update creationUser and creationDate for ', tm3TuvTableName) AS END_TO_UPDATE_CREATION_INFO;

		COMMIT;
		SET counter = 0;
	END LOOP;
	CLOSE tm3TuvTableName_cur;
    END$$

DELIMITER ;


CALL dataUpgradeForGBS3324;
DROP PROCEDURE IF EXISTS dataUpgradeForGBS3324;