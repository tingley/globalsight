# GBS-3404 segment order for excel 2010 configurable
alter table office2010_filter 
   add column EXCEL_ORDER char(1) DEFAULT 'n' NOT NULL;