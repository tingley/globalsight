# GBS-3956: add last usage date to tm.
# GBS-3676: Record job name at TUV level in Translation Memory.

# drop all "tm3_tu_tuv_attr_shared_xx" tables
SET @v = (SELECT CONCAT('drop table ', GROUP_CONCAT(a.table_name)) FROM information_schema.tables a WHERE a.table_schema = DATABASE() AND a.table_name LIKE 'tm3_tu_tuv_attr_shared_%' );
SET @Y = (SELECT IF (@v IS NOT NULL, @v, 'select 1'));
PREPARE s FROM @Y;
EXECUTE s;