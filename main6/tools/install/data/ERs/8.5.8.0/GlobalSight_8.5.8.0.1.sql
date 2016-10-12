# [GBS-3454] Add MT profile column to a *NEW* Word count report for MT.

ALTER TABLE target_page ADD COLUMN MT_ENGINE_WORD_COUNT INT(10) DEFAULT 0 AFTER MT_REPETITIONS;

ALTER TABLE workflow ADD COLUMN MT_PROFILE_NAME VARCHAR(60) DEFAULT NULL AFTER USE_MT;
ALTER TABLE workflow ADD COLUMN MT_ENGINE_WORD_COUNT INT(10) DEFAULT 0 AFTER MT_REPETITIONS;

COMMIT;