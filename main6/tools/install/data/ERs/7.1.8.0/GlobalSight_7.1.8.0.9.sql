# for gbs-1288
ALTER TABLE CORPUS_UNIT_VARIANT MODIFY COLUMN NATIVE_FORMAT_PATH VARCHAR(4000);
ALTER TABLE TARGET_PAGE MODIFY COLUMN STORAGE_PATH VARCHAR(4000);
ALTER TABLE JOB MODIFY COLUMN UUID VARCHAR(320);