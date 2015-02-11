#For GBS-1793: PowerPoint 2007 - Ability to exclude Notes from Extraction
ALTER TABLE `ms_office_ppt_filter` ADD COLUMN `IS_NOTES_TRANSLATE` CHAR(1) NOT NULL DEFAULT 'N' AFTER `IS_ALT_TRANSLATE`;