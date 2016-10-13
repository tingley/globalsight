#for GBS-900
Alter TABLE java_properties_filter ADD COLUMN INTERNAL_TEXTS text NOT NULL;
update java_properties_filter set internal_texts = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?><propertiesInternalText><items><content>\\{[^{]*?\\}</content><isRegex>true</isRegex></items></propertiesInternalText>';