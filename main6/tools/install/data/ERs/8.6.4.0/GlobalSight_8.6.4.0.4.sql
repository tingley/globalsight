# GBS-3956: add last usage date to tm.
# GBS-3676: Record job name at TUV level in Translation Memory.

# drop all "tm3_tu_tuv_attr_shared_xx" tables

DELIMITER $$
DROP PROCEDURE IF EXISTS dropTm3AttrTables$$

CREATE PROCEDURE dropTm3AttrTables()
BEGIN
	DECLARE done INTEGER DEFAULT 0;
	DECLARE tm3AttrTable VARCHAR(60);

	-- cursor
	DECLARE tm3_attr_table_cur CURSOR FOR 
		SELECT table_name FROM information_schema.TABLES 
		WHERE TABLE_SCHEMA = DATABASE() 
		AND table_name LIKE 'tm3_tu_tuv_attr_shared_%';

	-- error handler 
	DECLARE EXIT HANDLER FOR SQLSTATE '02000' SET done = 1;
	DECLARE CONTINUE HANDLER FOR SQLSTATE '42S02' BEGIN END;

	OPEN tm3_attr_table_cur;
	tm3AttrTableName_lable: LOOP
		FETCH tm3_attr_table_cur INTO tm3AttrTable;
		# logger
		SELECT tm3AttrTable AS CURRENT_TM3_ATTR_TABLE_TO_DELETE;

		IF done = 1 THEN
			LEAVE tm3AttrTableName_lable;
		END IF;

		SET @sql1 = CONCAT("DROP TABLE IF EXISTS ", tm3AttrTable);
		PREPARE stmt1 FROM @sql1;
		EXECUTE stmt1;
		DEALLOCATE PREPARE stmt1;

	END LOOP;
	CLOSE tm3_attr_table_cur;
    END$$

DELIMITER ;


CALL dropTm3AttrTables;
DROP PROCEDURE IF EXISTS dropTm3AttrTables;
