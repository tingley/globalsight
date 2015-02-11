# [GBS-3599] MS Hub, category field needs to allow for more characters 

ALTER TABLE mt_profile MODIFY COLUMN USERNAME VARCHAR(100);

ALTER TABLE mt_profile MODIFY COLUMN CATEGORY VARCHAR(128);