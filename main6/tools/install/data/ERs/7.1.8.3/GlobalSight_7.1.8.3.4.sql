# For GBS-1549 : XLIFF tkit export: <source> in <alt-trans> should come from the TM, not the segment.
ALTER TABLE `xliff_alt` ADD COLUMN `SOURCE_SEGMENT` text DEFAULT NULL;