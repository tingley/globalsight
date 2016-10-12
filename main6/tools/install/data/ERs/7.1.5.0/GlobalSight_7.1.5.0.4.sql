-- for GBS-527
ALTER TABLE target_page ADD COLUMN IS_DEFAULT_CONTEXT_MATCH CHAR(1) DEFAULT 'N';
ALTER TABLE cost ADD COLUMN DEFAULT_CONTEXT_ESTIMATED_COST FLOAT NOT NULL DEFAULT 0;
ALTER TABLE cost_by_word_count ADD COLUMN DEFAULT_CONTEXT_EXACT_COST FLOAT NOT NULL DEFAULT 0;
commit;
