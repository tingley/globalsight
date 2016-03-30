# Blaise issue: log uploadXliff and complete failure.

ALTER TABLE connector_blaise_job ADD COLUMN UPLOAD_XLF_STATE VARCHAR(20);
ALTER TABLE connector_blaise_job ADD COLUMN COMPLETE_STATE VARCHAR(20);
