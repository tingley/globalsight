#GBS-2035, BOM not exported
#Vincent Yan, 2011/08/03
ALTER TABLE FILE_PROFILE ADD COLUMN BOM_TYPE SMALLINT DEFAULT 0;
ALTER TABLE SOURCE_PAGE ADD COLUMN BOM_TYPE SMALLINT DEFAULT 0;
