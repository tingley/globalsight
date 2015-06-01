# GBS-3610: Creating and deleting lots of jobs produces DB Deadlocks
ALTER TABLE ADD INDEX IDX_WORKFLOW_ID ON (WORKFLOW_ID);
