# For GBS-703 : Implement of XML filter
ALTER TABLE xml_rule_filter ADD COLUMN USE_XML_RULE char(1) NOT NULL DEFAULT 'Y';
ALTER TABLE xml_rule_filter ADD COLUMN CONFIG_XML text;