## GBS-3073: TM Profile option to tell GlobalSight to search no additional TMs once a 100% match is found.

ALTER TABLE tm_profile ADD COLUMN GET_UNIQUE_FROM_MULT_TRANS CHAR(1) DEFAULT 'N' AFTER REF_TMS;