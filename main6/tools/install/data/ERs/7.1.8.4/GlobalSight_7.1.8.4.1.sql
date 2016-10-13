# For GBS-1681.Export returns system error (when source file profile is removed)
alter table extension drop index name;
ALTER TABLE `EXTENSION` ADD COLUMN `IS_ACTIVE` CHAR(1) NOT NULL DEFAULT 'Y';