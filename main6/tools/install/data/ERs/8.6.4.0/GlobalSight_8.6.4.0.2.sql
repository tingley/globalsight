# GBS-3956: add last usage date to tm.
# GBS-3676: Record job name at TUV level in Translation Memory.

ALTER TABLE tm3_tm ADD COLUMN tuv_ext_table VARCHAR(128) AFTER tuv_table;
UPDATE tm3_tm SET tuv_ext_table = CONCAT('TM3_TUV_EXT_SHARED_', sharedStorageId);

ALTER TABLE tm3_tm DROP COLUMN tu_tuv_attr_table;