# 3220 : Code refactor for getting "SID", "ModifiedDate" etc for performance
##############################################
### migrate old data into new column
##############################################
# for tm3
##############################################
DELIMITER $$
DROP PROCEDURE IF EXISTS migratetm3LMData3220$$
CREATE PROCEDURE migratetm3LMData3220()
BEGIN
	DECLARE done INTEGER DEFAULT 0;
	DECLARE tm3tmIdLoop INTEGER DEFAULT 0;
	
	declare ucount integer default 0;
	declare tableCount integer default 0;
	declare currentDB varchar(200) default 'GlobalSight';
	declare tm3id INTEGER;
	declare companyId INTEGER;
	declare tm3tuTable varchar(128);
	declare tm3tuvTable varchar(128);
	-- hold companyIds that have been updated to avoid duplicate upgrading.
	DECLARE companyIds  VARCHAR(1000) DEFAULT "";

	-- cursor
	DECLARE tm3tmId_cur CURSOR FOR select id from project_tm where tm3_id > 0;

	-- error handler 
	DECLARE EXIT HANDLER FOR SQLSTATE '02000' SET done = 1;
	DECLARE CONTINUE HANDLER FOR SQLSTATE '42S02' BEGIN END;

	select database() into currentDB;
	-- log current database
	select currentDB as CurrentDatabaseName;
	
	OPEN tm3tmId_cur;
	tm3tmId_lable: LOOP
		FETCH tm3tmId_cur into tm3tmIdLoop;

		IF done=1 THEN
			LEAVE tm3tmId_lable;
		END IF;

		select tm3_id, company_id into tm3id, companyId from project_tm where id=tm3tmIdLoop;
		
		# LOG
		SELECT tm3id AS CURRENT_TM3_ID;

		set @comId = CONCAT(",", companyId, ",");
		set @exist = LOCATE(@comId, companyIds);
		-- select concat("companyIds:", companyIds, " <>currentComId:", @comId, " <>index:", @exist) as debug_info;

		-- this company has not been updated (every "companyId" only update one time).
		if ( @exist = 0) then
			if (LENGTH(companyIds) = 0) then
				set companyIds = CONCAT(",", companyId, ",");
			else
				set companyIds = CONCAT(companyIds, companyId, ",");
			end if;

			select tu_table, tuv_table into tm3tuTable, tm3tuvTable from tm3_tm where id=tm3id;
			set @lmtable = CONCAT("leverage_match_", companyId);

			select count(*) into ucount from company where SEPARATE_LM_TU_TUV_TABLES = 1 and id = companyId;
			if (ucount = 0) then
				set @lmtable = "leverage_match";
			end if;

			select count(*) into tableCount from information_schema.TABLES a where a.table_name = @lmtable and a.table_schema = currentDB;

			if tableCount = 1 then
				set @sql1 = CONCAT("update ", @lmtable, " lm, ", tm3tuTable, " tu, ", tm3tuvTable, " tuv, tm3_events tm3e1, tm3_events tm3e2 ");
				set @sql1 = CONCAT(@sql1, "set lm.SID = tu.SID, lm.CREATION_USER = tm3e1.userName, lm.MODIFY_DATE = tm3e2.time ");
				set @sql1 = CONCAT(@sql1, "where lm.MATCHED_TUV_ID=tuv.id and tuv.tuId = tu.id and tuv.firstEventId=tm3e1.id and tuv.lastEventId=tm3e2.id;");
				
				PREPARE stmt1 FROM @sql1;
				EXECUTE stmt1;
				DEALLOCATE PREPARE stmt1;
			end if;
		end if;

	END LOOP;
	CLOSE tm3tmId_cur;
    END$$

DELIMITER ;

call migratetm3LMData3220;
DROP PROCEDURE IF EXISTS migratetm3LMData3220;