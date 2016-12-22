## GBS-4495 perplexity score on MT
DELIMITER $$

DROP PROCEDURE IF EXISTS PROC_ALTER_REPETITION_COLUMNS_I$$

CREATE PROCEDURE PROC_ALTER_REPETITION_COLUMNS_I()

    BEGIN
	DECLARE done INTEGER DEFAULT 0;
	DECLARE duplicate_key CONDITION FOR 1060;
	DECLARE companyId VARCHAR(50);

	-- cursor
	DECLARE company_cur CURSOR FOR
		SELECT id FROM company;

	-- error handler
	DECLARE CONTINUE HANDLER FOR SQLSTATE '02000' SET done = 1;
	DECLARE CONTINUE HANDLER FOR duplicate_key SET done = 2;

	OPEN company_cur;
	    companyLoop: LOOP

		FETCH company_cur INTO companyId;

		IF done = 1 THEN
			SELECT 'Finished to create perplexity tables, end loop.' AS MESSAGE;
			LEAVE companyLoop;
		END IF;

		SELECT CONCAT('Trying to create table translation_unit_variant_perplexity_', companyId, '.') AS MESSAGE;
		SET @a = CONCAT('create TABLE translation_unit_variant_perplexity_', companyId, ' (ID BIGINT AUTO_INCREMENT PRIMARY KEY, tuv_id BIGINT, source_score double DEFAULT "-1", target_score double DEFAULT "-1",  result char(1) DEFAULT "N")ENGINE=INNODB;');
		PREPARE s FROM @a; EXECUTE s;

		IF done = 2 THEN
			SET done = 0;
			SELECT CONCAT('Table \'translation_unit_variant_perplexity_', companyId, '\' has been created, ignore...') AS MESSAGE;
		END IF;

	    END LOOP;
	CLOSE company_cur;

    END$$

DELIMITER ;


CALL PROC_ALTER_REPETITION_COLUMNS_I;