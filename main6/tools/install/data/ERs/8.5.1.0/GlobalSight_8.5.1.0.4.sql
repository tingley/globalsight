## if job name is too long (>100), when delete job, "Data too long for column 'MESSAGE'" will happen.

ALTER TABLE system_log MODIFY COLUMN message VARCHAR(500);