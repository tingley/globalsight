# GBS-3088: Add "company_id" column in "tm_profile" and "target_page" tables.
# This can improve performance potentially.

# Add "company_id" column in "tm_profile" table.
ALTER TABLE tm_profile ADD COLUMN COMPANY_ID BIGINT DEFAULT NULL;

UPDATE tm_profile tmp, project_tm ptm
SET tmp.company_id = ptm.company_id
WHERE tmp.project_tm_id_for_save = ptm.id;

# Add "company_id" column in "target_page" table.
ALTER TABLE target_page ADD COLUMN COMPANY_ID BIGINT DEFAULT NULL;

UPDATE target_page tp, source_page sp
SET tp.company_id = sp.company_id
WHERE tp.source_page_id = sp.id;
