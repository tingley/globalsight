# GBS-4016 : GIT Connector: Ability to specify/edit user.name &user.email config parameters

ALTER TABLE connector_git ADD COLUMN EMAIL VARCHAR(300) DEFAULT '';