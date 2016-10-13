DELIMITER $$

DROP PROCEDURE IF EXISTS PROC_MIGRATE_ARCHIVED_JOBS$$

# For GBS-2914: Move "ARCHIVED" jobs data to separate tables.
# This procedure is used to migrate archived jobs which are in "archived" state but not migrated.
# This procedure can be run separately AFTER upgrade to 8.4 version, 
# and it can be run multiple times until all un-migrated "archived" jobs are done.
CREATE PROCEDURE PROC_MIGRATE_ARCHIVED_JOBS()

    BEGIN
	DECLARE done INTEGER DEFAULT 0;
	
	DECLARE jobID INTEGER DEFAULT 0;
	DECLARE companyID INTEGER DEFAULT 0;
	DECLARE companyFlag INTEGER DEFAULT 0;

	DECLARE lmWorkingTable VARCHAR(50);
	DECLARE lmArchiveTable VARCHAR(50);
	DECLARE tuWorkingTable VARCHAR(50);
	DECLARE tuArchiveTable VARCHAR(50);
	DECLARE tuvWorkingTable VARCHAR(50);
	DECLARE tuvArchiveTable VARCHAR(50);

	DECLARE tmpTableName VARCHAR(50);

	-- cursor
	DECLARE jobs_to_be_migrated_cur CURSOR FOR
		SELECT ID, COMPANY_ID FROM job WHERE job.STATE = 'ARCHIVED' AND job.IS_MIGRATED = 'N';

	-- error handler 
	DECLARE CONTINUE HANDLER FOR SQLSTATE '02000' SET done = 1;

	OPEN jobs_to_be_migrated_cur;

	    -- Loop every jobs to be migrated.
	    archived_jobs_lable: LOOP

		FETCH jobs_to_be_migrated_cur INTO jobID, companyID;

		IF done = 1 THEN
		        -- logger
			SELECT 'All archived jobs are migrated now, end loop.' AS MESSAGE;
			LEAVE archived_jobs_lable;
		END IF;

		-- logger
		SELECT CONCAT('Migrating job \'', jobID, '\' from company \'', companyID, '\'...') AS MESSAGE;

		###### determine the archive table names
	    	SELECT SEPARATE_LM_TU_TUV_TABLES INTO companyFlag FROM company AS com WHERE com.ID = companyID;
	    	IF companyFlag=1 THEN
	    	    SET lmWorkingTable = CONCAT('leverage_match_', companyID);
	    	    SET lmArchiveTable = CONCAT('leverage_match_', companyID, '_archived');
	    	    SET tuWorkingTable = CONCAT('translation_unit_', companyID);
	    	    SET tuArchiveTable = CONCAT('translation_unit_', companyID, '_archived');
	    	    SET tuvWorkingTable = CONCAT('translation_unit_variant_', companyID);
	    	    SET tuvArchiveTable = CONCAT('translation_unit_variant_', companyID, '_archived');	    	    
	    	ELSE
		    SET lmWorkingTable = 'leverage_match';
	    	    SET lmArchiveTable = 'leverage_match_archived';
	    	    SET tuWorkingTable = 'translation_unit';
    	    	    SET tuArchiveTable = 'translation_unit_archived';
    	    	    SET tuvWorkingTable = 'translation_unit_variant';
    	    	    SET tuvArchiveTable = 'translation_unit_variant_archived';
	    	END IF;
		-- logger
		-- SELECT CONCAT('LM table : ', lmArchiveTable) as LM_ARCHIVED_TABLE;
		-- SELECT CONCAT('TU table : ', tuArchiveTable) AS TU_ARCHIVED_TABLE;
		-- SELECT CONCAT('TUV table : ', tuvArchiveTable) AS TUV_ARCHIVED_TABLE;

		###### check leverage match table, if not exist, create it.
		SELECT a.TABLE_NAME INTO tmpTableName FROM information_schema.TABLES a WHERE a.TABLE_SCHEMA = DATABASE() AND a.TABLE_NAME = lmArchiveTable;
		IF tmpTableName IS NULL THEN
			SELECT CONCAT('LM table \'', lmArchiveTable, '\' does not exist in DB, creating it...') AS MESSAGE;
			SET @v = CONCAT(
				'CREATE TABLE IF NOT EXISTS ', lmArchiveTable,'(',
				'SOURCE_PAGE_ID INT,',
				'ORIGINAL_SOURCE_TUV_ID BIGINT,',
				'SUB_ID VARCHAR(40),',
				'MATCHED_TEXT_STRING TEXT,',
				'MATCHED_TEXT_CLOB MEDIUMTEXT,',
				'TARGET_LOCALE_ID BIGINT,',
				'MATCH_TYPE VARCHAR(80),',
				'ORDER_NUM SMALLINT,',
				'SCORE_NUM DECIMAL(8, 4) DEFAULT 0.00,',
				'MATCHED_TUV_ID INT,',
				'MATCHED_TABLE_TYPE SMALLINT DEFAULT 0,',
				'PROJECT_TM_INDEX INT(4) DEFAULT -1,',
				'TM_ID BIGINT(20) DEFAULT 0,',
				'TM_PROFILE_ID BIGINT(20) DEFAULT 0,',
				'MT_NAME VARCHAR(40),',
				'MATCHED_ORIGINAL_SOURCE MEDIUMTEXT,',
				'JOB_DATA_TU_ID BIGINT DEFAULT -1,',
				'PRIMARY KEY (ORIGINAL_SOURCE_TUV_ID, SUB_ID, TARGET_LOCALE_ID, ORDER_NUM)',
				');');
			PREPARE s FROM @v; EXECUTE s;

			SET @v = CONCAT('CREATE INDEX INDEX_ORIG_LEV_ORD ON ', lmArchiveTable, ' (ORIGINAL_SOURCE_TUV_ID);'); PREPARE s FROM @v; EXECUTE s;
			SET @v = CONCAT('CREATE INDEX IDX_LM_ORDER_ORIGSOURCETUV ON ', lmArchiveTable, ' (ORDER_NUM, ORIGINAL_SOURCE_TUV_ID);'); PREPARE s FROM @v; EXECUTE s;
			SET @v = CONCAT('CREATE INDEX IDX_LM_SRCPGID_TGTLOCID_ORDNUM ON ', lmArchiveTable, ' (SOURCE_PAGE_ID, TARGET_LOCALE_ID,ORDER_NUM);'); PREPARE s FROM @v; EXECUTE s;
			SET @v = CONCAT('CREATE INDEX IDX_LM_ORIGSRCTUV_TGTLOCID ON ', lmArchiveTable, ' (ORIGINAL_SOURCE_TUV_ID, TARGET_LOCALE_ID);'); PREPARE s FROM @v; EXECUTE s;
		END IF;
		SET tmpTableName = NULL;

		###### check TU table, if not exist, create it.
		SELECT a.TABLE_NAME INTO tmpTableName FROM information_schema.TABLES a WHERE a.TABLE_SCHEMA = DATABASE() AND a.TABLE_NAME = tuArchiveTable;
		IF tmpTableName IS NULL THEN
			SELECT CONCAT('TU table \'', tuArchiveTable, '\' does not exist in DB, creating it...') AS MESSAGE;
			SET @v = CONCAT(
				'CREATE TABLE IF NOT EXISTS ', tuArchiveTable,'(',
				'ID BIGINT PRIMARY KEY,',
				'ORDER_NUM INT NOT NULL,',
				'TM_ID INT,',
				'DATA_TYPE VARCHAR(20),',
				'TU_TYPE VARCHAR(50),',
				'LOCALIZE_TYPE CHAR(1) NOT NULL CHECK (LOCALIZE_TYPE IN (\'L\',\'T\')),',
				'LEVERAGE_GROUP_ID BIGINT NOT NULL,',
				'PID INT NOT NULL,',
				'SOURCE_TM_NAME VARCHAR(60),',
				'XLIFF_TRANSLATION_TYPE VARCHAR(60),',
				'XLIFF_LOCKED CHAR(1) NOT NULL DEFAULT \'N\' CHECK (XLIFF_LOCKED IN (\'Y\', \'N\')),',
				'IWS_SCORE VARCHAR(50),',
				'XLIFF_TARGET_SEGMENT MEDIUMTEXT,',
				'XLIFF_TARGET_LANGUAGE VARCHAR(30) DEFAULT NULL,',
				'GENERATE_FROM VARCHAR(50) DEFAULT NULL,',
				'SOURCE_CONTENT VARCHAR(30) DEFAULT NULL,',
				'PASSOLO_STATE VARCHAR(60) DEFAULT NULL,',
				'TRANSLATE VARCHAR(12) DEFAULT NULL',
				');');
			PREPARE s FROM @v; EXECUTE s;

			SET @v = CONCAT('CREATE INDEX INDEX_ID_LG ON ',tuArchiveTable,'(ID, LEVERAGE_GROUP_ID);'); PREPARE s FROM @v; EXECUTE s;
			SET @v = CONCAT('CREATE INDEX IDX_TU_LG_ID_ORDER ON ',tuArchiveTable,'(LEVERAGE_GROUP_ID, ID, ORDER_NUM);'); PREPARE s FROM @v; EXECUTE s;
			SET @v = CONCAT('CREATE INDEX INDEX_IDLT_TU_TM ON ', tuArchiveTable,'(ID, LOCALIZE_TYPE, TU_TYPE, TM_ID);'); PREPARE s FROM @v; EXECUTE s;
			SET @v = CONCAT('CREATE INDEX IDX_TU_TYPE_ID ON ', tuArchiveTable,' (TU_TYPE, ID);'); PREPARE s FROM @v; EXECUTE s;
		END IF;
		SET tmpTableName = NULL;

		###### check TUV table, if not exist, create it
		SELECT a.TABLE_NAME INTO tmpTableName FROM information_schema.TABLES a WHERE a.TABLE_SCHEMA = DATABASE() AND a.TABLE_NAME = tuvArchiveTable;
		IF tmpTableName IS NULL THEN
			SELECT CONCAT('TUV table \'', tuvArchiveTable, '\' does not exist in DB, creating it...') AS MESSAGE;
			SET @v = CONCAT(
				'CREATE TABLE IF NOT EXISTS ',tuvArchiveTable,'(',
				'ID BIGINT PRIMARY KEY,',
				'ORDER_NUM BIGINT NOT NULL,',
				'LOCALE_ID BIGINT NOT NULL,',
				'TU_ID BIGINT NOT NULL,',
				'IS_INDEXED CHAR(1) NOT NULL CHECK (IS_INDEXED IN (\'Y\', \'N\')),',
				'SEGMENT_CLOB MEDIUMTEXT,',
				'SEGMENT_STRING TEXT,',
				'WORD_COUNT INT(10),',
				'EXACT_MATCH_KEY BIGINT,',
				'STATE VARCHAR(40) NOT NULL CHECK (STATE IN (\'NOT_LOCALIZED\',\'LOCALIZED\',\'OUT_OF_DATE\',\'COMPLETE\',\'LEVERAGE_GROUP_EXACT_MATCH_LOCALIZED\',\'EXACT_MATCH_LOCALIZED\',\'ALIGNMENT_LOCALIZED\',\'UNVERIFIED_EXACT_MATCH\')),',
				'MERGE_STATE VARCHAR(20) NOT NULL CHECK (MERGE_STATE IN (\'NOT_MERGED\',\'MERGE_START\',\'MERGE_MIDDLE\',\'MERGE_END\')),',
				'TIMESTAMP DATETIME NOT NULL,',
				'LAST_MODIFIED DATETIME NOT NULL,',
				'MODIFY_USER VARCHAR(80),',
				'CREATION_DATE DATETIME,',
				'CREATION_USER VARCHAR(80),',
				'UPDATED_BY_PROJECT VARCHAR(40),',
				'SID VARCHAR(255),',
				'SRC_COMMENT TEXT,',
				'REPETITION_OF_ID BIGINT DEFAULT NULL,',
				'IS_REPEATED CHAR(1) DEFAULT \'N\' CHECK (IS_REPEATED IN (\'Y\', \'N\')),',
				'KEY REPETITION_OF_ID (REPETITION_OF_ID)',
				');');
			PREPARE s FROM @v; EXECUTE s;

			SET @v = CONCAT('CREATE INDEX INDEX_ID_LOCALE_STATE ON ', tuvArchiveTable,' (ID, LOCALE_ID, STATE);'); PREPARE s FROM @v; EXECUTE s;
			SET @v = CONCAT('CREATE INDEX INDEX_TU_LOC_STATE ON ', tuvArchiveTable,' (TU_ID, LOCALE_ID, STATE);'); PREPARE s FROM @v; EXECUTE s;
			SET @v = CONCAT('CREATE INDEX IDX_TUV_EMKEY_LOC_TU ON ', tuvArchiveTable,' (EXACT_MATCH_KEY, LOCALE_ID, TU_ID);'); PREPARE s FROM @v; EXECUTE s;
			SET @v = CONCAT('CREATE INDEX INDEX_TUV_TUID_STATE ON ', tuvArchiveTable,'(TU_ID, STATE);'); PREPARE s FROM @v; EXECUTE s;
			SET @v = CONCAT('CREATE UNIQUE INDEX IDX_TUV_ID_TU ON ', tuvArchiveTable,'(ID, TU_ID);'); PREPARE s FROM @v; EXECUTE s;
			SET @v = CONCAT('CREATE INDEX IDX_TUV_LOC_TU_ORDER_ID ON ', tuvArchiveTable,' (LOCALE_ID, TU_ID, ORDER_NUM, ID);'); PREPARE s FROM @v; EXECUTE s;
		END IF;
		SET tmpTableName = NULL;

		###### Migrate LM data
		SET @v = CONCAT(
			'INSERT INTO ', lmArchiveTable,' (',
				'SOURCE_PAGE_ID, ORIGINAL_SOURCE_TUV_ID, SUB_ID, MATCHED_TEXT_STRING, MATCHED_TEXT_CLOB, ',
				'TARGET_LOCALE_ID, MATCH_TYPE, ORDER_NUM, SCORE_NUM, MATCHED_TUV_ID, ',
				'MATCHED_TABLE_TYPE, PROJECT_TM_INDEX, TM_ID, TM_PROFILE_ID, MT_NAME, ',
				'MATCHED_ORIGINAL_SOURCE, JOB_DATA_TU_ID) ',
			'SELECT lm.SOURCE_PAGE_ID, lm.ORIGINAL_SOURCE_TUV_ID, lm.SUB_ID, lm.MATCHED_TEXT_STRING, lm.MATCHED_TEXT_CLOB, ',
				'lm.TARGET_LOCALE_ID, lm.MATCH_TYPE, lm.ORDER_NUM, lm.SCORE_NUM, lm.MATCHED_TUV_ID, ',
				'lm.MATCHED_TABLE_TYPE, lm.PROJECT_TM_INDEX, lm.TM_ID, lm.TM_PROFILE_ID, lm.MT_NAME, ',
				'lm.MATCHED_ORIGINAL_SOURCE, lm.JOB_DATA_TU_ID ',
			'FROM ', lmWorkingTable, ' lm, request req ',
			'WHERE lm.source_page_id = req.PAGE_ID ',
			'AND req.JOB_ID = ', jobID
		);
		PREPARE s FROM @v; EXECUTE s;

		SET @v = CONCAT(
			'DELETE lm FROM ', lmWorkingTable, ' lm, request req ',
			'WHERE lm.source_page_id = req.page_id ',
			'AND req.job_id = ', jobID);
		PREPARE s FROM @v; EXECUTE s;

		###### Migrate TUV data prior to migrate TU data
		SET @v = CONCAT(
			'INSERT INTO ', tuvArchiveTable, ' (',
				'ID, ORDER_NUM, LOCALE_ID, TU_ID, IS_INDEXED, ',
				'SEGMENT_CLOB, SEGMENT_STRING, WORD_COUNT, EXACT_MATCH_KEY, STATE, ',
				'MERGE_STATE, TIMESTAMP, LAST_MODIFIED, MODIFY_USER, CREATION_DATE, ',
				'CREATION_USER, UPDATED_BY_PROJECT, SID, SRC_COMMENT, REPETITION_OF_ID, IS_REPEATED) ',
			'SELECT tuv.ID, tuv.ORDER_NUM, tuv.LOCALE_ID, tuv.TU_ID, tuv.IS_INDEXED, ',
				'tuv.SEGMENT_CLOB, tuv.SEGMENT_STRING, tuv.WORD_COUNT, tuv.EXACT_MATCH_KEY, tuv.STATE, ',
				'tuv.MERGE_STATE, tuv.TIMESTAMP, tuv.LAST_MODIFIED, tuv.MODIFY_USER, tuv.CREATION_DATE, ',
				'tuv.CREATION_USER, tuv.UPDATED_BY_PROJECT, tuv.SID, tuv.SRC_COMMENT, tuv.REPETITION_OF_ID, tuv.IS_REPEATED ',
			'FROM ', tuvWorkingTable, ' tuv, ', tuWorkingTable, ' tu, source_page_leverage_group splg, request req ',
			'WHERE tuv.TU_ID = tu.ID '
			'AND tu.leverage_group_id = splg.lg_id '
			'AND splg.sp_id = req.page_id '
			'AND req.job_id = ', jobID
		);
		PREPARE s FROM @v; EXECUTE s;

		SET @v = CONCAT(
			'DELETE tuv FROM ', tuvWorkingTable, ' tuv,', tuWorkingTable, ' tu, source_page_leverage_group splg, request req ',
			'WHERE tuv.TU_ID = tu.ID ',
			'AND tu.leverage_group_id = splg.lg_id ',
			'AND splg.sp_id = req.page_id ',
			'AND req.job_id = ', jobID
		);
		PREPARE s FROM @v; EXECUTE s;

		######  Migrate TU data
		SET @v = CONCAT(
			'INSERT INTO ', tuArchiveTable, ' (',
				'ID, ORDER_NUM, TM_ID, DATA_TYPE, TU_TYPE, ',
				'LOCALIZE_TYPE, LEVERAGE_GROUP_ID, PID, SOURCE_TM_NAME, XLIFF_TRANSLATION_TYPE, ',
				'XLIFF_LOCKED, IWS_SCORE, XLIFF_TARGET_SEGMENT, XLIFF_TARGET_LANGUAGE, GENERATE_FROM, ',
				'SOURCE_CONTENT, PASSOLO_STATE, TRANSLATE) ',
			'SELECT tu.ID, tu.ORDER_NUM, tu.TM_ID, tu.DATA_TYPE, tu.TU_TYPE, ',
				'tu.LOCALIZE_TYPE, tu.LEVERAGE_GROUP_ID, tu.PID, tu.SOURCE_TM_NAME, tu.XLIFF_TRANSLATION_TYPE, ',
				'tu.XLIFF_LOCKED, tu.IWS_SCORE, tu.XLIFF_TARGET_SEGMENT, tu.XLIFF_TARGET_LANGUAGE, tu.GENERATE_FROM, ',
				'tu.SOURCE_CONTENT, tu.PASSOLO_STATE, tu.TRANSLATE ',
			'FROM ', tuWorkingTable, ' tu, source_page_leverage_group splg, request req ',
			'WHERE tu.leverage_group_id = splg.lg_id ',
			'AND splg.sp_id = req.page_id ',
			'AND req.job_id = ', jobID
		);
		PREPARE s FROM @v; EXECUTE s;

		SET @v = CONCAT(
			'DELETE tu FROM ', tuWorkingTable, ' tu, source_page_leverage_group splg, request req ',
			'WHERE tu.leverage_group_id = splg.lg_id ',
			'AND splg.sp_id = req.page_id ',
			'AND req.job_id = ', jobID
		);
		PREPARE s FROM @v; EXECUTE s;

		###### Migrate templatePart data
		SET @v = CONCAT(
			'INSERT INTO TEMPLATE_PART_ARCHIVED (ID, TEMPLATE_ID, ORDER_NUM, SKELETON_CLOB, SKELETON_STRING, TU_ID) ',
			'SELECT part.ID, part.TEMPLATE_ID, part.ORDER_NUM, part.SKELETON_CLOB, part.SKELETON_STRING, part.TU_ID ',
			'FROM template_part part, template tem, request req ',
			'WHERE part.TEMPLATE_ID = tem.ID ',
			'AND tem.SOURCE_PAGE_ID = req.PAGE_ID ',
			'AND req.JOB_ID = ', jobID
		);
		PREPARE s FROM @v; EXECUTE s;
		
		SET @v = CONCAT(
			'DELETE part FROM template_part part, template tem, request req ',
			'WHERE part.TEMPLATE_ID = tem.ID ',
			'AND tem.SOURCE_PAGE_ID = req.PAGE_ID ',
			'AND req.JOB_ID = ', jobID
		);
		PREPARE s FROM @v; EXECUTE s;

		###### update job
		UPDATE job SET IS_MIGRATED = 'Y' WHERE id = jobID;
		SET done = 0;

		-- commit changes per job
		COMMIT;

	    END LOOP;
	    
	CLOSE jobs_to_be_migrated_cur;
    END$$

DELIMITER ;
