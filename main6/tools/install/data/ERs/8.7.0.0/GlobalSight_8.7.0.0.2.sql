## GBS-4409: MT count in word counts.
## Flag to indicate a job is created before version 8.7 or since 8.7.
## For jobs that were created before version 8.7, this will be 'N'.

ALTER TABLE workflow ADD COLUMN IS_SINCE_8_7 CHAR(1) DEFAULT 'Y';

UPDATE workflow SET IS_SINCE_8_7 = 'N';
