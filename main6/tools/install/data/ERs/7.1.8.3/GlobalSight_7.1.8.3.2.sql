# For GBS-1549 : XLIFF tkit export: <source> in <alt-trans> should come from the TM, not the segment.
ALTER TABLE `leverage_match` ADD COLUMN `MATCHED_ORIGINAL_SOURCE` text DEFAULT NULL;