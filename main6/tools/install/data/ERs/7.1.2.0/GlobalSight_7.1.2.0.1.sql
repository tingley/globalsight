-- for GBS-380
alter table source_page modify EXTERNAL_PAGE_ID varchar(4000) NOT NULL;
alter table source_page modify STORAGE_PATH varchar(4000);
alter table corpus_unit modify NAME varchar(4000);
alter table page_tm drop index PAGE_NAME;
alter table page_tm modify PAGE_NAME varchar(4000) NOT NULL;
alter table reserved_time modify SUBJECT varchar(4200) NOT NULL;

commit;