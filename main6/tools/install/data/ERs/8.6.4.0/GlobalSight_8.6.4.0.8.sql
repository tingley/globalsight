## GBS-3650: context matching needs review.

# drop all "leverage_match_attr_xx" tables

DELIMITER $$
DROP PROCEDURE IF EXISTS dropLmAttrTables$$

CREATE PROCEDURE dropLmAttrTables()
BEGIN
	DECLARE done INTEGER DEFAULT 0;
	DECLARE lmAttrTable VARCHAR(60);

	-- cursor
	DECLARE lm_attr_table_cur CURSOR FOR 
		SELECT table_name FROM information_schema.TABLES 
		WHERE TABLE_SCHEMA = DATABASE() 
		AND table_name LIKE 'leverage_match_attr_%';

	-- error handler 
	DECLARE EXIT HANDLER FOR SQLSTATE '02000' SET done = 1;
	DECLARE CONTINUE HANDLER FOR SQLSTATE '42S02' BEGIN END;

	OPEN lm_attr_table_cur;
	lmAttrTableName_lable: LOOP
		FETCH lm_attr_table_cur INTO lmAttrTable;
		# logger
		SELECT lmAttrTable AS CURRENT_LM_ATTR_TABLE_TO_DELETE;

		IF done = 1 THEN
			LEAVE lmAttrTableName_lable;
		END IF;

		SET @sql1 = CONCAT("DROP TABLE IF EXISTS ", lmAttrTable);
		PREPARE stmt1 FROM @sql1;
		EXECUTE stmt1;
		DEALLOCATE PREPARE stmt1;

	END LOOP;
	CLOSE lm_attr_table_cur;
    END$$

DELIMITER ;


CALL dropLmAttrTables;
DROP PROCEDURE IF EXISTS dropLmAttrTables;
