# GBS-2747, Falcon connector - WebService APIs
# start_date -- Start date of job
# completed_date -- Completed date of job

ALTER TABLE JOB ADD COLUMN START_DATE DATETIME;
ALTER TABLE JOB ADD COLUMN COMPLETED_DATE DATETIME;