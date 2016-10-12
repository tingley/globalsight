## Rename "mt_confidence_score" to "mt_threshold" for "mt_profile" table.

ALTER TABLE mt_profile CHANGE COLUMN MT_CONFIDENCE_SCORE MT_THRESHOLD BIGINT(20) DEFAULT 0;

ALTER TABLE workflow CHANGE COLUMN MT_CONFIDENCE_SCORE MT_THRESHOLD INT DEFAULT 0;
