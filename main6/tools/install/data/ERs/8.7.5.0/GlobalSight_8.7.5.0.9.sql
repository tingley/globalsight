#GBS-4846 Terminology: mixed Chinese terms in job
ALTER TABLE TERM_LEVERAGE_MATCH MODIFY COLUMN TARGET_PAGE_LOCALE VARCHAR(10) NOT NULL;