## GBS-4541 password complexity requirements
## add new column in company to switch strong password on/off
ALTER TABLE company ADD COLUMN ENABLE_STRONG_PASSWORD char(1) DEFAULT 'N';
## drop unused columns which were created for GBS-4517
ALTER TABLE company DROP COLUMN DEFAULT_FLUENCY;
ALTER TABLE company DROP COLUMN DEFAULT_ADEQUACY;

## add new column in user to mark times which user need to change his password
ALTER TABLE user ADD COLUMN RESET_PASSWORD_TIMES INTEGER DEFAULT -1;

