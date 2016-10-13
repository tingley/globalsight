# Add "Include Compact Tags" option in project level.

ALTER TABLE project add column 
REVIEW_REPORT_INCLUDE_COMPACT_TAGS tinyint(1) NOT NULL DEFAULT '0' AFTER REVIEWONLYAUTOSEND
;