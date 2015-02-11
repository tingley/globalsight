# For GBS-818
alter table TRANSLATION_UNIT_VARIANT modify column SEGMENT_STRING TEXT;
alter table LEVERAGE_MATCH modify column MATCHED_TEXT_STRING TEXT;
alter table IP_TM_SRC_T modify column SEGMENT_STRING TEXT;
alter table IP_TM_SRC_L modify column SEGMENT_STRING TEXT;
alter table IP_TM_TRG_T modify column SEGMENT_STRING TEXT;
alter table IP_TM_TRG_L modify column SEGMENT_STRING TEXT;