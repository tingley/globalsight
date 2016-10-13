ALTER TABLE TM3_ATTR ADD COLUMN `affectsIdentity` char(1) NOT NULL DEFAULT 'Y'; 

UPDATE TM3_ATTR SET affectsIdentity = 'N' WHERE 
    name = '.from_ws' or name = '.project' or name = '.format' or 
    name = '.translatable'; 
