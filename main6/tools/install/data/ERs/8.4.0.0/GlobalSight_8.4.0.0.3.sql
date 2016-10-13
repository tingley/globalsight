DELIMITER $$

DROP PROCEDURE IF EXISTS PROC_ALTER_REPETITION_COLUMNS_I$$

# For GBS-2899: Repetition/Repeated flag should be marked on segment TUV instead of TU
# Add "repetition_of_id" and "is_repeated" columns to all TUV tables.
CREATE PROCEDURE PROC_ALTER_REPETITION_COLUMNS_I()

    BEGIN
	DECLARE done INTEGER DEFAULT 0;
	DECLARE duplicate_key CONDITION FOR 1060;

	DECLARE v_tuvTableName VARCHAR(50);
	DECLARE v_tuTableName VARCHAR(50);

	-- cursor
	DECLARE tuv_tables_cur CURSOR FOR
		SELECT a.TABLE_NAME FROM information_schema.TABLES a 
		WHERE a.TABLE_SCHEMA = DATABASE() 
		AND a.TABLE_NAME LIKE 'translation_unit_variant%'
		AND a.TABLE_NAME NOT LIKE '%_archived'
		AND a.TABLE_NAME != 'translation_unit_variant2';

	-- error handler
	DECLARE CONTINUE HANDLER FOR SQLSTATE '02000' SET done = 1;
	DECLARE CONTINUE HANDLER FOR duplicate_key SET done = 2;

	###### Add "repetition_of_id" and "is_repeated" columns to TUV tables.
	OPEN tuv_tables_cur;
	    tuv_table_lable: LOOP

		FETCH tuv_tables_cur INTO v_tuvTableName;

		IF done = 1 THEN
			SELECT 'Finished to add 2 columns to all tuv tables, end loop.' AS MESSAGE;
			LEAVE tuv_table_lable;
		END IF;

		SELECT CONCAT('Trying to add 2 columns to \'', v_tuvTableName, '\'.') AS MESSAGE;
		SET @a = CONCAT('ALTER TABLE ', v_tuvTableName, ' ADD COLUMN REPETITION_OF_ID BIGINT DEFAULT 0, ADD COLUMN IS_REPEATED CHAR(1) DEFAULT \'N\';');
		PREPARE s FROM @a; EXECUTE s;

		IF done = 2 THEN
			SET done = 0;
			SELECT CONCAT('Table \'', v_tuvTableName, '\' has been added the 2 columns, ignore...') AS MESSAGE;
		END IF;

	    END LOOP;
	CLOSE tuv_tables_cur;

    END$$

DELIMITER ;


CALL PROC_ALTER_REPETITION_COLUMNS_I;