## GBS-3885 : Exporting TM takes long time.

## For all the "tm3_tuv_shared_xx" table "localeId" add index

DELIMITER $$
DROP PROCEDURE IF EXISTS createIdxFor3885$$

CREATE PROCEDURE createIdxFor3885()
BEGIN
	DECLARE done INTEGER DEFAULT 0;
	DECLARE duplicate_key CONDITION FOR 1061;
	DECLARE tm3TuvTable VARCHAR(60);
	-- cursor
	DECLARE tm3_tuv_shared_table_cur CURSOR FOR 
		SELECT table_name FROM information_schema.TABLES 
		WHERE TABLE_SCHEMA = DATABASE() 
		AND table_name LIKE 'tm3_tuv_shared_%';

	-- error handler
	DECLARE CONTINUE HANDLER FOR SQLSTATE '02000' SET done = 1;
	DECLARE CONTINUE HANDLER FOR duplicate_key SET done = 2;
	
	OPEN tm3_tuv_shared_table_cur;
	    tm3TuvTableName_lable: LOOP

		FETCH tm3_tuv_shared_table_cur INTO tm3TuvTable;

		IF done = 1 THEN
			SELECT 'Finished to add INDEX_LOCALE_ID to all tuv tables, end loop.' AS MESSAGE;
			LEAVE tm3TuvTableName_lable;
		END IF;

		SELECT CONCAT('Trying to add INDEX_LOCALE_ID to \'', tm3TuvTable, '\'.') AS MESSAGE;
		SET @a = CONCAT("ALTER TABLE ", tm3TuvTable, " ADD INDEX INDEX_LOCALE_ID(localeId)");
		PREPARE s FROM @a; EXECUTE s;

		IF done = 2 THEN
			SET done = 0;
			SELECT CONCAT('Table \'', tm3TuvTable, '\' has been added the INDEX_LOCALE_ID, ignore...') AS MESSAGE;
		END IF;

	    END LOOP;
	CLOSE tm3_tuv_shared_table_cur;

    END$$

DELIMITER ;

CALL createIdxFor3885;
DROP PROCEDURE IF EXISTS createIdxFor3885;