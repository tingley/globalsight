# GBS-2571 & GBS-1860

DELIMITER $$

DROP PROCEDURE IF EXISTS updateContentColumnForTm3Tuv$$

CREATE PROCEDURE updateContentColumnForTm3Tuv()
BEGIN
	DECLARE done INTEGER DEFAULT 0;
	DECLARE companyIdLoop INTEGER DEFAULT 0;
	
	-- cursor 
	DECLARE companyId_cur CURSOR FOR select id from company where id != 1;

	-- error handler 
	DECLARE EXIT HANDLER FOR SQLSTATE '02000' SET done = 1;
	DECLARE CONTINUE HANDLER FOR SQLSTATE '42S02' BEGIN END;

	OPEN companyId_cur;
	companyId_lable: LOOP
		FETCH companyId_cur into companyIdLoop;

		# LOG
		SELECT CONCAT("Current table name : ", "tm3_tuv_shared_", companyIdLoop) AS CURRENT_TABLE_NAME;

		IF done=1 THEN
			LEAVE companyId_lable;
		END IF;

		SET @sql1 = CONCAT("ALTER TABLE ", "tm3_tuv_shared_", companyIdLoop, " MODIFY COLUMN content MEDIUMTEXT;");
		PREPARE stmt FROM @sql1;
		EXECUTE stmt;
		DEALLOCATE PREPARE stmt;

	END LOOP;
	CLOSE companyId_cur;
    END$$

DELIMITER ;

call updateContentColumnForTm3Tuv;

DROP PROCEDURE IF EXISTS updateContentColumnForTm3Tuv;
