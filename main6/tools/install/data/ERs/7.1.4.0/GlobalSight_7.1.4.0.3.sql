-- for GBS-514
alter table TM_PROFILE add column IS_REF_TM char(1) DEFAULT 'N';
alter table TM_PROFILE add column REF_TM_PENALTY INTEGER DEFAULT 0;
alter table TM_PROFILE add column REF_TMS char(255);

commit;