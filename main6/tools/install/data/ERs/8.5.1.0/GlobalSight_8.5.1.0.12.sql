# 3220 : Code refactor for getting "SID", "ModifiedDate" etc for performance
##############################################
### alter table for LEVERAGE_MATCH_1
##############################################
DELIMITER $$

DROP PROCEDURE IF EXISTS alertLM1Tables3220$$

CREATE PROCEDURE alertLM1Tables3220()
	BEGIN
		declare tableCount integer default 0;
		declare columnCount integer default 0;
		
		SELECT COUNT(*) INTO tableCount FROM information_schema.TABLES a WHERE a.table_name = 'LEVERAGE_MATCH_1' AND a.table_schema = DATABASE();
		SELECT COUNT(*) INTO columnCount FROM information_schema.COLUMNS b WHERE b.table_name = 'LEVERAGE_MATCH_1' AND b.table_schema = DATABASE() AND b.column_name = 'SID';
		
		if (tableCount > 0 && columnCount = 0) then
			ALTER TABLE LEVERAGE_MATCH_1 ADD COLUMN SID varchar(255) DEFAULT NULL, 
			ADD COLUMN CREATION_USER varchar(80) DEFAULT NULL, 
			ADD COLUMN MODIFY_DATE datetime NOT NULL;
		end if;
		
		SELECT COUNT(*) INTO tableCount FROM information_schema.TABLES a WHERE a.table_name = 'LEVERAGE_MATCH_1_ARCHIVED' AND a.table_schema = DATABASE();
		SELECT COUNT(*) INTO columnCount FROM information_schema.COLUMNS b WHERE b.table_name = 'LEVERAGE_MATCH_1_ARCHIVED' AND b.table_schema = DATABASE() AND b.column_name = 'SID';
		if (tableCount > 0 && columnCount = 0) then
			ALTER TABLE LEVERAGE_MATCH_1_ARCHIVED ADD COLUMN SID varchar(255) DEFAULT NULL, 
			ADD COLUMN CREATION_USER varchar(80) DEFAULT NULL, 
			ADD COLUMN MODIFY_DATE datetime NOT NULL;
		end if;
		
    END$$

DELIMITER ;

call alertLM1Tables3220;

DROP PROCEDURE IF EXISTS alertLM1Tables3220;