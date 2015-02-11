# GBS-3474: big data operations
## Upgrade "company_backup" table if it exists already.
## Upgrade "job_backup table if it exists already.

DELIMITER $$
DROP PROCEDURE IF EXISTS dataUpgradeForGBS3474$$

CREATE PROCEDURE dataUpgradeForGBS3474()
BEGIN
	DECLARE counter1 INTEGER DEFAULT 0;
	DECLARE counter2 INTEGER DEFAULT 0;

	## Upgrade "company_backup" table if it exists already.
	SELECT COUNT(*) INTO counter1 FROM information_schema.TABLES WHERE table_schema = DATABASE() AND table_name = 'company_backup';
	SELECT COUNT(*) INTO counter2 FROM information_schema.COLUMNS WHERE table_schema = DATABASE() AND table_name = 'company_backup' AND COLUMN_NAME = 'BIG_DATA_STORE_LEVEL';
	SELECT counter1 AS "TABLE COMPANY_BACKUP EXIST?";
	SELECT counter2 AS "COLUMN BIG_DATA_STORE_LEVEL EXIST?";

	# "company_backup" exists and "BIG_DATA_STORE_LEVEL" column is not added yet.
	IF (counter1 > 0 AND counter2 = 0) THEN
		ALTER TABLE company_backup ADD COLUMN BIG_DATA_STORE_LEVEL SMALLINT(1) DEFAULT 1;
		COMMIT;
		UPDATE company_backup SET BIG_DATA_STORE_LEVEL = SEPARATE_LM_TU_TUV_TABLES;
		COMMIT;
	END IF;

	## Upgrade "job_backup" table if it exists already.
	SELECT COUNT(*) INTO counter1 FROM information_schema.TABLES WHERE table_schema = DATABASE() AND table_name = 'job_backup';
	SELECT COUNT(*) INTO counter2 FROM information_schema.COLUMNS WHERE table_schema = DATABASE() AND table_name = 'job_backup' AND COLUMN_NAME = 'TU_TABLE';
	SELECT counter1 AS "TABLE JOB_BACKUP EXIST?";
	SELECT counter2 AS "COLUMN TU_TABLE EXIST?";

	# "job_backup" exists and "TU_TABLE" etc columns are not added yet.
	IF (counter1 > 0 AND counter2 = 0) THEN
		ALTER TABLE job_backup
		ADD COLUMN TU_TABLE VARCHAR(128), 
		ADD COLUMN TU_ARCHIVE_TABLE VARCHAR(128), 
		ADD COLUMN TUV_TABLE VARCHAR(128), 
		ADD COLUMN TUV_ARCHIVE_TABLE VARCHAR(128),
		ADD COLUMN LM_TABLE VARCHAR(128), 
		ADD COLUMN LM_ARCHIVE_TABLE VARCHAR(128);
		COMMIT;

		UPDATE job_backup, company
		SET job_backup.TU_TABLE = 'TRANSLATION_UNIT',
		job_backup.TU_ARCHIVE_TABLE = 'TRANSLATION_UNIT_ARCHIVED',
		job_backup.TUV_TABLE = 'TRANSLATION_UNIT_VARIANT',
		job_backup.TUV_ARCHIVE_TABLE = 'TRANSLATION_UNIT_VARIANT_ARCHIVED',
		job_backup.LM_TABLE = 'LEVERAGE_MATCH',
		job_backup.LM_ARCHIVE_TABLE = 'LEVERAGE_MATCH_ARCHIVED'
		WHERE job_backup.COMPANY_ID = company.ID
		AND company.SEPARATE_LM_TU_TUV_TABLES = 0;
		COMMIT;

		UPDATE job_backup, company
		SET job_backup.TU_TABLE = CONCAT('TRANSLATION_UNIT_', job_backup.COMPANY_ID),
		job_backup.TU_ARCHIVE_TABLE = CONCAT('TRANSLATION_UNIT_', job_backup.COMPANY_ID, '_ARCHIVED'),
		job_backup.TUV_TABLE = CONCAT('TRANSLATION_UNIT_VARIANT_', job_backup.COMPANY_ID),
		job_backup.TUV_ARCHIVE_TABLE = CONCAT('TRANSLATION_UNIT_VARIANT_', job_backup.COMPANY_ID, '_ARCHIVED'),
		job_backup.LM_TABLE = CONCAT('LEVERAGE_MATCH_', job_backup.COMPANY_ID),
		job_backup.LM_ARCHIVE_TABLE = CONCAT('LEVERAGE_MATCH_', job_backup.COMPANY_ID, '_ARCHIVED')
		WHERE job_backup.COMPANY_ID = company.ID
		AND company.SEPARATE_LM_TU_TUV_TABLES = 1;
		COMMIT;
	END IF;
END$$

DELIMITER ;


CALL dataUpgradeForGBS3474;
DROP PROCEDURE IF EXISTS dataUpgradeForGBS3474;