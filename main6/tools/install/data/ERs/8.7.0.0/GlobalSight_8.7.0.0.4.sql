## GBS-4392: fuzzy matching lost matching percentage.
## Update "score_num" to 60 and "match_type" to "MACHINE_TRANSLATION" in all "leverage_match_xx" tables.

DELIMITER $$
DROP PROCEDURE IF EXISTS GBS_4392_UPGRADE$$

CREATE PROCEDURE GBS_4392_UPGRADE()
BEGIN
	DECLARE done INTEGER DEFAULT 0;
	DECLARE leverageMatchTable VARCHAR(60);

	-- cursor
	DECLARE leverage_match_table_cur CURSOR FOR 
		SELECT TABLE_NAME FROM information_schema.TABLES 
		WHERE table_schema = DATABASE() 
		AND table_name LIKE 'leverage_match_%' 
		AND table_name NOT LIKE 'leverage_match_ext_%' 
		AND table_name NOT LIKE 'leverage_match_attr_%';

	-- error handler 
	DECLARE EXIT HANDLER FOR SQLSTATE '02000' SET done = 1;
	
	OPEN leverage_match_table_cur;
	leverage_match_table_lable: LOOP
		FETCH leverage_match_table_cur INTO leverageMatchTable;
		# logger
		SELECT leverageMatchTable AS CURRENT_LEVERAGE_MATCH_TABLE;

		IF done = 1 THEN
			LEAVE leverage_match_table_lable;
		END IF;

		## update "score_num" and "match_type" for every "leverage_match_xx" tables.
		SET @sql1 = CONCAT("UPDATE ", leverageMatchTable, " SET score_num = 60, match_type = 'MACHINE_TRANSLATION' WHERE order_num = 301 OR order_num = 302 and score_num != 60; ");
		## logger
		SELECT @sql1 AS SQL1;

		PREPARE stmt FROM @sql1;
		EXECUTE stmt;
		DEALLOCATE PREPARE stmt;
		COMMIT;

	END LOOP;
	CLOSE leverage_match_table_cur;
    END$$

DELIMITER ;

 CALL GBS_4392_UPGRADE;
 DROP PROCEDURE IF EXISTS GBS_4392_UPGRADE;

