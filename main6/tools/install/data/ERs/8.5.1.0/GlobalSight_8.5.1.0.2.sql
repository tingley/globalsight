# A late DB cleaning for GBS-3016 (MT Profile).
# If the 3 tables exist, company removal may be blocked for foreign key constraint to "tm_profile".

DROP TABLE IF EXISTS tm_profile_ao_info;
DROP TABLE IF EXISTS tm_profile_mt_info;
DROP TABLE IF EXISTS tm_profile_promt_info;
