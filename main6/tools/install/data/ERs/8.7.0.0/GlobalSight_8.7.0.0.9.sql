###############################################################################################
## Clean "tm3_events" relevants codes
## 1. drop foreign key and the column itself for "firstEventId" of "tm3_tuv_shared_xx" tables.
###############################################################################################

DELIMITER $$
DROP PROCEDURE IF EXISTS TM3_EVENTS_CLEAN_1$$

CREATE PROCEDURE TM3_EVENTS_CLEAN_1()
BEGIN
	DECLARE done INTEGER DEFAULT 0;

	DECLARE tm3TuvTable VARCHAR(60);
	DECLARE foreignKeyName VARCHAR(60);

	-- cursor
	DECLARE tm3_tuv_table_cur CURSOR FOR 
		SELECT table_name, constraint_name FROM information_schema.KEY_COLUMN_USAGE
		WHERE table_schema = DATABASE()
		AND table_name LIKE 'tm3_tuv_shared_%' 
		AND column_name = 'firstEventId';

	-- error handler 
	DECLARE EXIT HANDLER FOR SQLSTATE '02000' SET done = 1;

	OPEN tm3_tuv_table_cur;
	tm3_tuv_table_lable: LOOP
		FETCH tm3_tuv_table_cur INTO tm3TuvTable, foreignKeyName;
		# logger
		SELECT tm3TuvTable AS CURRENT_TM3_TUV_TABLE;
		SELECT foreignKeyName AS CURRENT_FK_NAME;

		IF done = 1 THEN
			LEAVE tm3_tuv_table_lable;
		END IF;

		-- drop foreign key for "firstEventId" column
		SET @sql1 = CONCAT("ALTER TABLE ", tm3TuvTable, " DROP FOREIGN KEY ", foreignKeyName);
		## logger
		SELECT @sql1 AS drop_firstEventId_fk_sql;

		PREPARE stmt FROM @sql1;
		EXECUTE stmt;
		DEALLOCATE PREPARE stmt;
		COMMIT;

		-- drop "firstEventId" column
		-- SET @sql2 = CONCAT("ALTER TABLE ", tm3TuvTable, " DROP COLUMN firstEventId; ");
		## logger
		-- SELECT @sql2 AS drop_firstEventId_sql;

		-- PREPARE stmt FROM @sql2;
		-- EXECUTE stmt;
		-- DEALLOCATE PREPARE stmt;
		-- COMMIT;

	END LOOP;
	CLOSE tm3_tuv_table_cur;

    END$$

DELIMITER ;

CALL TM3_EVENTS_CLEAN_1;
DROP PROCEDURE IF EXISTS TM3_EVENTS_CLEAN_1;


###############################################################################################
## Clean "tm3_events" relevants codes
## 2. drop foreign key and the column itself for "lastEventId" of "tm3_tuv_shared_xx" tables.
###############################################################################################

DELIMITER $$
DROP PROCEDURE IF EXISTS TM3_EVENTS_CLEAN_2$$

CREATE PROCEDURE TM3_EVENTS_CLEAN_2()
BEGIN
	DECLARE done INTEGER DEFAULT 0;

	DECLARE tm3TuvTable VARCHAR(60);
	DECLARE foreignKeyName VARCHAR(60);

	-- cursor
	DECLARE tm3_tuv_table_cur CURSOR FOR 
		SELECT table_name, constraint_name FROM information_schema.KEY_COLUMN_USAGE
		WHERE table_schema = DATABASE()
		AND table_name LIKE 'tm3_tuv_shared_%' 
		AND column_name = 'lastEventId';

	-- error handler 
	DECLARE EXIT HANDLER FOR SQLSTATE '02000' SET done = 1;

	OPEN tm3_tuv_table_cur;
	tm3_tuv_table_lable: LOOP
		FETCH tm3_tuv_table_cur INTO tm3TuvTable, foreignKeyName;
		# logger
		SELECT tm3TuvTable AS CURRENT_TM3_TUV_TABLE;
		SELECT foreignKeyName AS CURRENT_FK_NAME;

		IF done = 1 THEN
			LEAVE tm3_tuv_table_lable;
		END IF;

		-- drop foreign key for "lastEventId" column
		SET @sql1 = CONCAT("ALTER TABLE ", tm3TuvTable, " DROP FOREIGN KEY ", foreignKeyName);
		## logger
		SELECT @sql1 AS drop_lastEventId_fk_sql;

		PREPARE stmt FROM @sql1;
		EXECUTE stmt;
		DEALLOCATE PREPARE stmt;
		COMMIT;

		-- drop "lastEventId" column
		-- SET @sql2 = CONCAT("ALTER TABLE ", tm3TuvTable, " DROP COLUMN lastEventId; ");
		## logger
		-- SELECT @sql2 AS drop_lastEventId_sql;

		-- PREPARE stmt FROM @sql2;
		-- EXECUTE stmt;
		-- DEALLOCATE PREPARE stmt;
		-- COMMIT;

	END LOOP;
	CLOSE tm3_tuv_table_cur;

    END$$

DELIMITER ;

CALL TM3_EVENTS_CLEAN_2;
DROP PROCEDURE IF EXISTS TM3_EVENTS_CLEAN_2;


###############################################################################################
## Clean "tm3_events" relevants codes
## 3. Drop "tm3_events" table as it is abandoned already.
###############################################################################################

DROP TABLE IF EXISTS tm3_events;
COMMIT;
