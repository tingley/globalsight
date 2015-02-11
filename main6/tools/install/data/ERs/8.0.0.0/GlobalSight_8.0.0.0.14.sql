# For export deadlock(this is from GlobalSight_8.0.0.0.1.sql)

# On some installs, the foreign keys have already been dropped.  
# This awful technique for dropping foreign keys if they exist comes from
# http://forums.mysql.com/read.php?97,218825,218955

DROP PROCEDURE IF EXISTS upgrade_corpus_map_drop_fks;

DELIMITER $$

CREATE PROCEDURE upgrade_corpus_map_drop_fks()
BEGIN
IF EXISTS (
  SELECT NULL FROM information_schema.TABLE_CONSTRAINTS
  WHERE 
        CONSTRAINT_SCHEMA = DATABASE() AND 
        CONSTRAINT_NAME = 'FK_CORPUS_MAP_PROJECT_TU_ID'
) THEN
  ALTER TABLE CORPUS_MAP DROP FOREIGN KEY FK_CORPUS_MAP_PROJECT_TU_ID;
  ALTER TABLE CORPUS_MAP DROP FOREIGN KEY FK_CORPUS_MAP_PROJECT_TUV_ID;
END IF;
END $$

DELIMITER ;

CALL upgrade_corpus_map_drop_fks();

DROP PROCEDURE upgrade_corpus_map_drop_fks;
