-- #676
ALTER TABLE project_tm_tu_l ADD COLUMN FROM_WORLD_SERVER CHAR(1) NOT NULL DEFAULT 'N' CHECK (AUTO_REPAIR IN('Y', 'N'));
ALTER TABLE project_tm_tu_t ADD COLUMN FROM_WORLD_SERVER CHAR(1) NOT NULL DEFAULT 'N' CHECK (AUTO_REPAIR IN('Y', 'N'));
ALTER TABLE TM_PROFILE ADD COLUMN AUTO_REPAIR CHAR(1) NOT NULL DEFAULT 'Y' CHECK (AUTO_REPAIR IN('Y', 'N'));
COMMIT;

