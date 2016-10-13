# Drop FK "FK_XML_RULE_ID" from "file_profile" table if exists.
# Since GBS-815, old servers may have this FK that will fail to start system after 8.5.1.
##############################################
DELIMITER $$

DROP PROCEDURE IF EXISTS PROCEDURE_DROP_FK_XML_RULE_ID$$

CREATE PROCEDURE PROCEDURE_DROP_FK_XML_RULE_ID()
BEGIN
	DECLARE counter INTEGER DEFAULT 0;

	SELECT COUNT(*) INTO counter FROM information_schema.TABLE_CONSTRAINTS a
	WHERE a.CONSTRAINT_NAME = 'FK_XML_RULE_ID'
	AND a.TABLE_NAME = 'file_profile';
	-- SELECT counter AS counter;

	IF counter = 1 THEN
		ALTER TABLE file_profile DROP FOREIGN KEY FK_XML_RULE_ID;
	END IF;
    END$$

DELIMITER ;

CALL PROCEDURE_DROP_FK_XML_RULE_ID;

DROP PROCEDURE IF EXISTS PROCEDURE_DROP_FK_XML_RULE_ID;