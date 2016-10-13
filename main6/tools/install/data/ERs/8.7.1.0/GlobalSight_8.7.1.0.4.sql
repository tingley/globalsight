## GBS-4485: slow export speed
## Add extra column for "target_page" table to record the tm updating status. 
## This column has 3 available values:
## 1: EXPORTED_FILE_AVAILABLE
## 2: EXPORTED_TM_UPDATING
## 3: EXPORTED_TM_UPDATING_DONE

ALTER TABLE target_page ADD COLUMN EXPORTED_SUB_STATE SMALLINT DEFAULT 1 AFTER STATE;
