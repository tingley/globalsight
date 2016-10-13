# [GBS-3491]  Can we create User access logs for Jboss
## Exchange old data in "event_type" and "object_type" columns as they were inserted into each other wrongly before.
## Modify the 2 columns length.

DELIMITER $$
DROP PROCEDURE IF EXISTS dataUpgradeForGBS3491$$

CREATE PROCEDURE dataUpgradeForGBS3491()
BEGIN
	DECLARE v_dataType VARCHAR(60);

	SELECT DATA_TYPE INTO v_dataType FROM information_schema.COLUMNS WHERE TABLE_NAME = 'system_log' AND COLUMN_NAME = 'event_type' AND TABLE_SCHEMA = DATABASE();
	SELECT v_dataType AS "EVENT_TYPE COLUMN DATA TYPE";

	IF (v_dataType = 'char') THEN
		ALTER TABLE system_log CHANGE COLUMN EVENT_TYPE OBJECT_TYPE2 VARCHAR(60);
		COMMIT;

		ALTER TABLE system_log CHANGE COLUMN OBJECT_TYPE EVENT_TYPE VARCHAR(20);
		COMMIT;

		ALTER TABLE system_log CHANGE COLUMN OBJECT_TYPE2 OBJECT_TYPE VARCHAR(60);
		COMMIT;
	END IF;
END$$

DELIMITER ;


CALL dataUpgradeForGBS3491;
DROP PROCEDURE IF EXISTS dataUpgradeForGBS3491;
