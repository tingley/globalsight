# For GBS-1740 "matched_table_type" may be null in original implementation.
UPDATE LEVERAGE_MATCH SET MATCHED_TABLE_TYPE = 1 WHERE MATCHED_TABLE_TYPE IS NULL;