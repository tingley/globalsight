# Clean GS edtion codes

DROP TABLE IF EXISTS AUTOACTION;

DROP TABLE IF EXISTS GS_EDITION_ACTIVITY;

DROP TABLE IF EXISTS JOB_GSEDITION_INFO;

DROP TABLE IF EXISTS ISSUE_EDITION_RELATION;

ALTER TABLE activity DROP COLUMN AUTOMATIC_ACTION_ID;
ALTER TABLE activity DROP COLUMN GSEDITION_ACTION_ID;

ALTER TABLE comments DROP COLUMN ORIGINAL_ID;
ALTER TABLE comments DROP COLUMN WSDL_URL;