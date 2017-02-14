## GBS-4595 Blaise Connector Phase 2

ALTER TABLE connector_blaise ADD COLUMN IS_AUTOMATIC CHAR(1) DEFAULT 'N';
ALTER TABLE connector_blaise ADD COLUMN PULL_DAYS VARCHAR(30);
ALTER TABLE connector_blaise ADD COLUMN PULL_HOUR INTEGER DEFAULT 7;
ALTER TABLE connector_blaise ADD COLUMN DEFAULT_FILE_PROFILE_ID BIGINT(20) DEFAULT -1;
ALTER TABLE connector_blaise ADD COLUMN MIN_PROCEDURE_WORDS INTEGER DEFAULT 600;
ALTER TABLE connector_blaise ADD COLUMN JOB_ATTRIBUTE_GROUP_ID BIGINT(20) DEFAULT -1;
ALTER TABLE connector_blaise ADD COLUMN IS_COMBINED CHAR(1) DEFAULT 'Y';
ALTER TABLE connector_blaise ADD COLUMN LAST_MAX_ENTRY_ID BIGINT(20) DEFAULT -1;
ALTER TABLE connector_blaise ADD COLUMN LOGIN_USER VARCHAR(20);
ALTER TABLE connector_blaise ADD COLUMN QA_COUNT INTEGER DEFAULT 10;

DROP TABLE IF EXISTS  CONNECTOR_BLAISE_ATTRIBUTES CASCADE;
CREATE TABLE CONNECTOR_BLAISE_ATTRIBUTES (
  ID BIGINT(20) NOT NULL AUTO_INCREMENT,
  CONNECTOR_ID BIGINT(20) NOT NULL,
  ATTRIBUTE_ID BIGINT(20) NOT NULL,
  ATTRIBUTE_VALUE VARCHAR(30),
  ATTRIBUTE_TYPE VARCHAR(40),
  BLAISE_JOB_TYPE CHAR(1) NOT NULL,
  PRIMARY KEY (ID),
  CONSTRAINT FK_CONNECTOR_BLAISE_ATTRIBUTES_CONNECTOR FOREIGN KEY (CONNECTOR_ID) REFERENCES connector_blaise (ID)
) ENGINE=INNODB;
