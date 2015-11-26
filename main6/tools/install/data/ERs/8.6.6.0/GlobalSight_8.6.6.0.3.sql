## GBS-3885 : Exporting TM takes long time.

## For all the "tm3_tuv_shared_xx" table "localeId" add index

DELIMITER $$
DROP PROCEDURE IF EXISTS createIdxFor3885$$

CREATE PROCEDURE createIdxFor3885()
BEGIN
	DECLARE done INTEGER DEFAULT 0;
	DECLARE tm3TuvTable VARCHAR(60);

	-- cursor
	DECLARE tm3_tuv_shared_table_cur CURSOR FOR 
		SELECT table_name FROM information_schema.TABLES 
		WHERE TABLE_SCHEMA = DATABASE() 
		AND table_name LIKE 'tm3_tuv_shared_%';

	-- error handler 
	DECLARE EXIT HANDLER FOR SQLSTATE '02000' SET done = 1;

	OPEN tm3_tuv_shared_table_cur;
	tm3TuvTableName_lable: LOOP
		FETCH tm3_tuv_shared_table_cur INTO tm3TuvTable;
		# logger
		SELECT tm3TuvTable AS CURRENT_TM3_TUV_TABLE_NAME;

		IF done = 1 THEN
			LEAVE tm3TuvTableName_lable;
		END IF;

		SET @sql1 = CONCAT(
			"SELECT COUNT(*) INTO @indexNum FROM information_schema.innodb_sys_indexes AS idx, ",
			"(SELECT table_id FROM information_schema.innodb_sys_tables WHERE NAME LIKE ", "'", DATABASE(),"/", tm3TuvTable, "') AS tableId ",
			"WHERE idx.table_id = tableId.table_id AND idx.name = 'INDEX_LOCALE_ID'");
		# logger
		# select @sql1 as SQL1;
		
		PREPARE stmt1 FROM @sql1;
		EXECUTE stmt1;
		DEALLOCATE PREPARE stmt1;
		# logger
		SELECT @indexNum AS INDEX_IS_CREATED;

		IF @indexNum = 0 THEN
			SET @createIdx = CONCAT("ALTER TABLE ", tm3TuvTable, " ADD INDEX INDEX_LOCALE_ID(localeId)");
			PREPARE stmt2 FROM @createIdx;
			EXECUTE stmt2;
			DEALLOCATE PREPARE stmt2;
		END IF;

	END LOOP;
	CLOSE tm3_tuv_shared_table_cur;
    END$$

DELIMITER ;


CALL createIdxFor3885;
DROP PROCEDURE IF EXISTS createIdxFor3885;