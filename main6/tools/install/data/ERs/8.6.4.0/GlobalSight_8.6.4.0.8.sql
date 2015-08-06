## GBS-3650: context matching needs review.

# drop all "leverage_match_attr_xx" tables
SET @v = (SELECT CONCAT('drop table ', GROUP_CONCAT(a.table_name)) FROM information_schema.tables a WHERE a.table_schema = DATABASE() AND a.table_name LIKE 'leverage_match_attr_%' );
SET @Y = (SELECT IF (@v IS NOT NULL, @v, 'select 1'));
PREPARE s FROM @Y;
EXECUTE s;