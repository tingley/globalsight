alter table indd_filter
    add column `EXTRACT_LINE_BREAK` char(1) NOT NULL DEFAULT 'N';
    
alter table indd_filter
    add column `REPLACE_NONBREAKING_SPACE` char(1) NOT NULL DEFAULT 'N';