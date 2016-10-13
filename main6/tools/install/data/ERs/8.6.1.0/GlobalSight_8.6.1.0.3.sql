## GBS-3528: SID support: char limit = 255. Longer resnames fail at job creation.

ALTER TABLE tm3_tm ADD COLUMN tu_tuv_attr_table VARCHAR(128) AFTER attr_val_table;
UPDATE tm3_tm SET tu_tuv_attr_table = CONCAT('TM3_TU_TUV_ATTR_SHARED_', sharedStorageId);
