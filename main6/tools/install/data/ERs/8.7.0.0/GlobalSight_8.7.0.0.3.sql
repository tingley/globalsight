## GBS-4364: poor import performance between 8.6.2 and later.
## Add index for "tmId" column of "tm3_tu_shared_xx" tables.

DELIMITER $$
DROP PROCEDURE IF EXISTS GBS_4364_UPGRADE$$

CREATE PROCEDURE GBS_4364_UPGRADE()
BEGIN
	DECLARE done INTEGER DEFAULT 0;

	DECLARE tm3TuTable VARCHAR(60);

	-- cursor
	DECLARE tm3_tu_table_cur CURSOR FOR 
		SELECT TABLE_NAME FROM information_schema.TABLES WHERE table_schema = DATABASE() AND table_name LIKE 'tm3_tu_shared_%';

	-- error handler 
	DECLARE EXIT HANDLER FOR SQLSTATE '02000' SET done = 1;
	-- duplicate key name
	DECLARE CONTINUE HANDLER FOR SQLSTATE '42000' BEGIN END;

	OPEN tm3_tu_table_cur;
	tm3_tu_table_lable: LOOP
		FETCH tm3_tu_table_cur INTO tm3TuTable;
		# logger
		SELECT tm3TuTable AS CURRENT_TM3_TU_TABLE;

		IF done = 1 THEN
			LEAVE tm3_tu_table_lable;
		END IF;

		## Add index for every "tm3_tu_shared_xx" tables.
		SET @sql = CONCAT("CREATE INDEX tmId ON ", tm3TuTable, "(tmId); ");
		## logger
		SELECT @sql AS SQL1;

		PREPARE stmt FROM @sql;
		EXECUTE stmt;
		DEALLOCATE PREPARE stmt;
		COMMIT;

	END LOOP;
	CLOSE tm3_tu_table_cur;
    END$$

DELIMITER ;

CALL GBS_4364_UPGRADE;
DROP PROCEDURE IF EXISTS GBS_4364_UPGRADE;
