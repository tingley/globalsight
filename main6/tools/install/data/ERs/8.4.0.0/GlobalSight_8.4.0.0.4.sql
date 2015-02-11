DELIMITER $$

DROP PROCEDURE IF EXISTS PROC_ALTER_REPETITION_COLUMNS_II$$

# For GBS-2899: Repetition/Repeated flag should be marked on segment TUV instead of TU
# Update "repetition_of_id" and "is_repeated" columns from TU to TUV tables.
CREATE PROCEDURE PROC_ALTER_REPETITION_COLUMNS_II()

    BEGIN
	DECLARE done INTEGER DEFAULT 0;
 	DECLARE column_not_found CONDITION FOR 1054;
 	DECLARE column_not_found2 CONDITION FOR 1243;

	DECLARE v_tuvTableName VARCHAR(50);
	DECLARE v_tuTableName VARCHAR(50);

	-- cursor
	DECLARE tuv_tables_cur CURSOR FOR
		SELECT a.TABLE_NAME FROM information_schema.TABLES a 
		WHERE a.TABLE_SCHEMA = DATABASE() 
		AND a.TABLE_NAME LIKE 'translation_unit_variant%'
		AND a.TABLE_NAME NOT LIKE '%_archived'
		AND a.TABLE_NAME != 'translation_unit_variant2';

	DECLARE tu_tables_cur CURSOR FOR
		SELECT a.TABLE_NAME FROM information_schema.TABLES a 
		WHERE a.TABLE_SCHEMA = DATABASE() 
		AND a.TABLE_NAME LIKE 'translation_unit%' 
		AND a.TABLE_NAME NOT LIKE 'translation_unit_variant%'
		AND a.TABLE_NAME NOT LIKE '%_archived'
		AND a.TABLE_NAME != 'translation_unit2';

	-- error handler 
	DECLARE CONTINUE HANDLER FOR SQLSTATE '02000' SET done = 1;
 	DECLARE CONTINUE HANDLER FOR column_not_found SET done = 2;
 	DECLARE CONTINUE HANDLER FOR column_not_found2 SET done = 2;

	OPEN tuv_tables_cur;
	    tuv_table_lable: LOOP

		FETCH tuv_tables_cur INTO v_tuvTableName;

		IF done = 1 THEN
			SELECT 'Update to all tuv tables is done, end loop.' AS MESSAGE;
			LEAVE tuv_table_lable;
		END IF;

		###### Update "repetition_of_id" and "is_repeated" columns from TU to TUV tables.
		SET @tu1 = SUBSTRING(v_tuvTableName, 1, LOCATE('variant', v_tuvTableName) - 2 );
		SET @tu2 = SUBSTRING(v_tuvTableName, LOCATE('variant', v_tuvTableName) + 7);
		IF LENGTH(@tu2) = 0 THEN
			SET @tuTableName = @tu1;
		ELSE
			SET @tuTableName = CONCAT(@tu1, @tu2);
		END IF;
		-- select @tuTableName as tuTableName;

                ###### Update "repetition_of_id" column first.
		SET @updateSql1 = CONCAT(
			'UPDATE ', v_tuvTableName, ' tuv1, ', @tuTableName, ' tu1, ', @tuTableName, ' tu2, ', v_tuvTableName, ' tuv2 ',
			'SET tuv1.REPETITION_OF_ID = tuv2.ID ',
			'WHERE tuv1.TU_ID = tu1.ID ',
			'AND tu1.REPETITION_OF_ID > 0 ',
			'AND tu1.REPETITION_OF_ID = tu2.ID ',
			'AND tu2.ID = tuv2.TU_ID ',
			'AND tuv1.LOCALE_ID = tuv2.LOCALE_ID ',
			'AND tuv1.STATE != \'OUT_OF_DATE\' ',
			'AND tuv1.STATE != \'COMPLETE\';'
		);
		-- select @updateSql1;
		SELECT CONCAT('Updating \'', v_tuvTableName, '\' from its TU table.') AS MESSAGE;
		PREPARE s FROM @updateSql1; EXECUTE s;
		COMMIT;
		IF done = 2 THEN
			SET done = 0;
			SELECT CONCAT('Error: probably columns have not been created in \'',v_tuvTableName, '\' table.') AS MESSAGE;
		ELSE
			###### Update "is_repeated" column secondly.
			SET @updateSql2 = CONCAT(
				'UPDATE ', v_tuvTableName, ' tuv1, ', v_tuvTableName, ' tuv2 ',
				'SET tuv1.is_repeated = \'Y\' '
				'WHERE tuv1.ID = tuv2.REPETITION_OF_ID;'
			);
			SELECT @updateSql2;
			PREPARE s FROM @updateSql2; EXECUTE s;
			COMMIT;
 		END IF;

	    END LOOP;
	CLOSE tuv_tables_cur;

	###### Drop "repetition_of_id" and "is_repeated" columns from TU tables.
	/**
	SET done = 0;
	OPEN tu_tables_cur;

	    tu_table_lable: LOOP
	
		FETCH tu_tables_cur INTO v_tuTableName;
		
		IF done = 1 THEN
			SELECT 'Update to all tu tables is done, end loop.' AS MESSAGE;
			LEAVE tu_table_lable;
		END IF;

		SET @dropSql1 = CONCAT('ALTER TABLE ', v_tuTableName, ' DROP COLUMN REPETITION_OF_ID;');
		PREPARE s FROM @dropSql1; EXECUTE s;

		SET @dropSql2 = CONCAT('ALTER TABLE ', v_tuTableName, ' DROP COLUMN IS_REPEATED;');
		PREPARE s FROM @dropSql2; EXECUTE s;

	    END LOOP;
	CLOSE tu_tables_cur;
	*/
    END$$

DELIMITER ;


CALL PROC_ALTER_REPETITION_COLUMNS_II;