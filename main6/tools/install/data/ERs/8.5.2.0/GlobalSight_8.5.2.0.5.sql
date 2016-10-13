## GBS-3305: TM Profile option to save target=source segments to storage TM without saving exact matched segments to storage TM
ALTER TABLE tm_profile ADD COLUMN IS_APPROVED_SEG_SAVED_TO_PROJ_TM CHAR(1) DEFAULT 'N';
ALTER TABLE tm_profile ADD COLUMN IS_EXACT_MATCH_SEG_SAVED_TO_PROJ_TM CHAR(1) DEFAULT 'Y';