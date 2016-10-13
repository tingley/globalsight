# [GBS-3389] import option - try again
# When create job, if it has multiple source files, it should have same number request objects in "request" table. Only when all request objects are created, this can be "Y".
# When try to re-create job, if this flag is "N", do not allow to re-create.

ALTER TABLE job ADD COLUMN IS_ALL_REQUEST_GENERATED CHAR(1) DEFAULT 'N';

# For existed jobs, set it to "Y".
UPDATE job SET IS_ALL_REQUEST_GENERATED = 'Y';