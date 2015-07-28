# GBS-3945: Workflow Notifications & Listener URL.
## Upgrade "company_backup" table if it exists already.

DELIMITER $$
DROP PROCEDURE IF EXISTS dataUpgradeForGBS3945$$

CREATE PROCEDURE dataUpgradeForGBS3945()
BEGIN
	DECLARE counter1 INTEGER DEFAULT 0;
	DECLARE counter2 INTEGER DEFAULT 0;

	## Upgrade "company_backup" table if it exists already.
	SELECT COUNT(*) INTO counter1 FROM information_schema.TABLES WHERE table_schema = DATABASE() AND table_name = 'company_backup';
	SELECT COUNT(*) INTO counter2 FROM information_schema.COLUMNS WHERE table_schema = DATABASE() AND table_name = 'company_backup' AND COLUMN_NAME = 'ENABLE_WORKFLOW_STATE_POSTS';

	SELECT counter1 AS "TABLE COMPANY_BACKUP EXIST?";
	SELECT counter2 AS "COLUMN ENABLE_WORKFLOW_STATE_POSTS EXIST?";

	# "company_backup" exists and "ENABLE_WORKFLOW_STATE_POSTS" column is not added yet.
	IF (counter1 > 0 AND counter2 = 0) THEN
		ALTER TABLE company_backup ADD COLUMN ENABLE_WORKFLOW_STATE_POSTS CHAR(1) DEFAULT 'N';
		COMMIT;
	END IF;
END$$

DELIMITER ;


CALL dataUpgradeForGBS3945;
DROP PROCEDURE IF EXISTS dataUpgradeForGBS3945;