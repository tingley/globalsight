#for GBS-648
alter table workflow_template modify column NAME varchar(60);
COMMIT;  