-- This script is a sample CREATE INDEX statement
-- that creates a basic index on the FUZZY_INDEX.TUV_ID
-- column. The ANALYZE statement then notifies Oracle
-- to use the new index.

CREATE INDEX tmp_fix_fuzzy_index
   ON FUZZY_INDEX
   (TUV_ID)
   NOLOGGING
   INITRANS 2
   MAXTRANS 255
   TABLESPACE indx
   PCTFREE 10
   STORAGE (
            INITIAL 1M
            NEXT 1M
            PCTINCREASE 0
            MINEXTENTS 1
            MAXEXTENTS 1024);

ANALYZE TABLE fuzzy_index compute statistics;
