#for GBS-974:GS Edition
alter table comments add column ORIGINAL_ID bigint;
alter table comments add column WSDL_URL varchar(100);