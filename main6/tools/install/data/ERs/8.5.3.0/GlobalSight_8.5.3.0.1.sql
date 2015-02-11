# GBS-3378: Db schema/Hibernate mapping enhancement for "source locale".

ALTER TABLE job ADD COLUMN SOURCE_LOCALE_ID BIGINT NOT NULL AFTER ORIGINAL_STATE;

UPDATE job, l10n_profile
SET job.source_locale_id = l10n_profile.SOURCE_LOCALE_ID
WHERE job.L10N_PROFILE_ID = l10n_profile.ID
AND job.SOURCE_LOCALE_ID < 1;

# If current job has no l10n_profile_id value...
UPDATE job j, request req, l10n_profile lp
SET j.SOURCE_LOCALE_ID = lp.SOURCE_LOCALE_ID
WHERE j.ID = req.JOB_ID
AND req.L10N_PROFILE_ID = lp.id
AND j.SOURCE_LOCALE_ID < 1;