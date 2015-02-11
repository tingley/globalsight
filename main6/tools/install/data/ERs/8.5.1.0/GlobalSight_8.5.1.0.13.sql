# 3220 : Code refactor for getting "SID", "ModifiedDate" etc for performance
##############################################
### alter table for LEVERAGE_MATCH_<compnayId>
##############################################
DELIMITER $$

DROP PROCEDURE IF EXISTS alertLMTables3220$$

CREATE PROCEDURE alertLMTables3220()
BEGIN
	DECLARE done INTEGER DEFAULT 0;
	DECLARE companyIdLoop INTEGER DEFAULT 0;
	DECLARE ucount INTEGER DEFAULT 0;
	DECLARE tableCount INTEGER DEFAULT 0;
	DECLARE columnCount INTEGER DEFAULT 0;
	DECLARE currentTableName VARCHAR(50);
	
	-- cursor 
	DECLARE companyId_cur CURSOR FOR SELECT id FROM company WHERE id != 1 AND SEPARATE_LM_TU_TUV_TABLES = 1;

	-- error handler 
	DECLARE EXIT HANDLER FOR SQLSTATE '02000' SET done = 1;
	DECLARE CONTINUE HANDLER FOR SQLSTATE '42S02' BEGIN END;
	
	OPEN companyId_cur;
	companyId_lable: LOOP
		FETCH companyId_cur INTO companyIdLoop;

		SELECT COUNT(*) INTO ucount FROM company WHERE SEPARATE_LM_TU_TUV_TABLES = 1 AND id = companyIdLoop;
		
		IF (ucount > 0) THEN
	
			IF done=1 THEN
				LEAVE companyId_lable;
			END IF;
			
			-- check if this table exists
			SET currentTableName = CONCAT("LEVERAGE_MATCH_", companyIdLoop);
			-- logger
			SELECT currentTableName AS CURRENT_TABLE_NAME;

			SELECT COUNT(*) INTO tableCount FROM information_schema.TABLES a WHERE a.table_name = currentTableName AND a.table_schema = DATABASE();
			-- check if these columns exist
			SELECT COUNT(*) INTO columnCount FROM information_schema.COLUMNS b WHERE b.table_name = currentTableName AND b.table_schema = DATABASE() AND b.column_name = 'SID';
			IF (tableCount = 1 && columnCount = 0) THEN
				SET @sql1 = CONCAT("ALTER TABLE ", currentTableName, " ADD COLUMN SID varchar(255) DEFAULT NULL, ");
				SET @sql1 = CONCAT(@sql1, "ADD COLUMN CREATION_USER varchar(80) DEFAULT NULL, ");
				SET @sql1 = CONCAT(@sql1, "ADD COLUMN MODIFY_DATE datetime NOT NULL; ");
				PREPARE stmt1 FROM @sql1;
				EXECUTE stmt1;
				DEALLOCATE PREPARE stmt1;
			END IF;

			-- check if this table exists
			SET currentTableName = CONCAT("LEVERAGE_MATCH_", companyIdLoop, "_ARCHIVED");
			SELECT COUNT(*) INTO tableCount FROM information_schema.TABLES a WHERE a.table_name = currentTableName AND a.table_schema = DATABASE();
			-- check if these columns exist
			SELECT COUNT(*) INTO columnCount FROM information_schema.COLUMNS b WHERE b.table_name = currentTableName AND b.table_schema = DATABASE() AND b.column_name = 'SID';
	
			IF (tableCount = 1 && columnCount = 0) THEN
				SET @sql2 = CONCAT("ALTER TABLE ", currentTableName, " ADD COLUMN SID varchar(255) DEFAULT NULL, ");
				SET @sql2 = CONCAT(@sql2, "ADD COLUMN CREATION_USER varchar(80) DEFAULT NULL, ");
				SET @sql2 = CONCAT(@sql2, "ADD COLUMN MODIFY_DATE datetime NOT NULL; ");
				PREPARE stmt2 FROM @sql2;
				EXECUTE stmt2;
				DEALLOCATE PREPARE stmt2;
			END IF;
		END IF;
	END LOOP;
	CLOSE companyId_cur;
    END$$

DELIMITER ;

CALL alertLMTables3220;

DROP PROCEDURE IF EXISTS alertLMTables3220;