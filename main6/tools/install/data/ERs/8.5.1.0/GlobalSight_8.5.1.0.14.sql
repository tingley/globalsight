# 3220 : Code refactor for getting "SID", "ModifiedDate" etc for performance
##############################################
### migrate old data into new column
##############################################
# for tm2
##############################################
DELIMITER $$
DROP PROCEDURE IF EXISTS migratetm2LMData3220$$
CREATE PROCEDURE migratetm2LMData3220()
BEGIN
	DECLARE done INTEGER DEFAULT 0;
	DECLARE companyIdLoop INTEGER DEFAULT 0;
	declare ucount integer default 0;
	declare tableCount integer default 0;
	declare currentDB varchar(200) default 'GlobalSight';
	
	-- cursor 
	DECLARE companyId_cur CURSOR FOR select id from company where id != 1;

	-- error handler 
	DECLARE EXIT HANDLER FOR SQLSTATE '02000' SET done = 1;
	DECLARE CONTINUE HANDLER FOR SQLSTATE '42S02' BEGIN END;

	select database() into currentDB;
	-- log current database
	select currentDB as CurrentDatabaseName;
	
	OPEN companyId_cur;
	companyId_lable: LOOP
		FETCH companyId_cur into companyIdLoop;

		# LOG
		SELECT companyIdLoop AS CURRENT_COMPANY_ID;

		IF done=1 THEN
			LEAVE companyId_lable;
		END IF;
		
		set @lmtable = CONCAT("leverage_match_", companyIdLoop);
		select count(*) into ucount from company where SEPARATE_LM_TU_TUV_TABLES = 1 and id = companyIdLoop;
		if (ucount = 0) then
			set @lmtable = "leverage_match";
		end if;
		
		select count(*) into tableCount from information_schema.TABLES a where a.table_name = @lmtable and a.table_schema = currentDB;
		
		if tableCount = 1 then
			set @sql1 = CONCAT("update ", @lmtable, " lm, project_tm_tuv_t ptuv ");
			set @sql1 = CONCAT(@sql1, "set lm.SID = ptuv.SID, lm.CREATION_USER = ptuv.CREATION_USER, lm.MODIFY_DATE = ptuv.MODIFY_DATE ");
			set @sql1 = CONCAT(@sql1, "where ptuv.ID = lm.MATCHED_TUV_ID;");
			
			PREPARE stmt1 FROM @sql1;
			EXECUTE stmt1;
			DEALLOCATE PREPARE stmt1;
		end if;

	END LOOP;
	CLOSE companyId_cur;
    END$$

DELIMITER ;
call migratetm2LMData3220;
DROP PROCEDURE IF EXISTS migratetm2LMData3220;