## GBS-3301: Minor performance enhancement: add/delete index on "job" table.
CREATE INDEX INDEX_JOB_NAME ON JOB(NAME);
ALTER TABLE JOB DROP INDEX INDEX_STATE;
