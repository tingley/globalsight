# GBS-3534 xml filter does not use secondary filter any more
ALTER TABLE xml_rule_filter DROP COLUMN SECOND_FILTER_ID;
ALTER TABLE xml_rule_filter DROP COLUMN SECOND_FILTER_TABLE_NAME;