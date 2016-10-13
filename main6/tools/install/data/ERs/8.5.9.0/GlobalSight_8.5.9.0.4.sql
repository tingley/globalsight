# GBS-3697 & GBS-3704: McAfee SaaS: QA Checker.
## Upgrade "company_backup" table if it exists already.

DELIMITER $$
DROP PROCEDURE IF EXISTS dataUpgradeForGBS3697$$

CREATE PROCEDURE dataUpgradeForGBS3697()
BEGIN
	DECLARE counter1 INTEGER DEFAULT 0;
	DECLARE counter2 INTEGER DEFAULT 0;

	## Upgrade "company_backup" table if it exists already.
	SELECT COUNT(*) INTO counter1 FROM information_schema.TABLES WHERE table_schema = DATABASE() AND table_name = 'company_backup';
	SELECT COUNT(*) INTO counter2 FROM information_schema.COLUMNS WHERE table_schema = DATABASE() AND table_name = 'company_backup' AND COLUMN_NAME = 'ENABLE_QA_CHECKS';

	SELECT counter1 AS "TABLE COMPANY_BACKUP EXIST?";
	SELECT counter2 AS "COLUMN ENABLE_QA_CHECKS EXIST?";

	# "company_backup" exists and "ENABLE_QA_CHECKS" column is not added yet.
	IF (counter1 > 0 AND counter2 = 0) THEN
		ALTER TABLE company_backup ADD COLUMN ENABLE_QA_CHECKS CHAR(1) DEFAULT 'N';
		ALTER TABLE company_backup ADD COLUMN ENABLE_DITA_CHECKS CHAR(1) DEFAULT 'N';
		COMMIT;
	END IF;
END$$

DELIMITER ;


CALL dataUpgradeForGBS3697;
DROP PROCEDURE IF EXISTS dataUpgradeForGBS3697;