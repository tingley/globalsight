# GBS-3458 Add "Translate Comment" for office2010 filter
alter table office2010_filter 
   add column IS_COMMENT_TRANSLATE char(1) DEFAULT 'N' NOT NULL AFTER IS_TOC_TRANSLATE;