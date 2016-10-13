# For GBS-1688
alter table COMPANY add column ENABLE_TM_ACCESS_CONTROL char(1) default 'N';
alter table COMPANY add column ENABLE_TB_ACCESS_CONTROL char(1) default 'N';