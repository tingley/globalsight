# for gbs-2539
ALTER TABLE TM_PROFILE ADD COLUMN MS_MT_CLIENTID VARCHAR(100) DEFAULT NULL;
ALTER TABLE TM_PROFILE ADD COLUMN MS_MT_CLIENT_SECRET VARCHAR(100) DEFAULT NULL;