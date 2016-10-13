# For GBS-1740 (This column is used to store original tuId for in progress TM match)

ALTER TABLE `LEVERAGE_MATCH` ADD COLUMN `JOB_DATA_TU_ID` BIGINT(20) DEFAULT -1;