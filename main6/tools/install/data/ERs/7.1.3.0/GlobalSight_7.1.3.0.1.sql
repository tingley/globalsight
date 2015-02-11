-- for GBS-420
ALTER TABLE l10n_profile_wftemplate_info ADD COLUMN IS_ACTIVE CHAR(1) DEFAULT 'Y';

commit;