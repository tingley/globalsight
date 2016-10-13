# [GBS-3051] Adds "Not Translated" function to "Task Complete" button and segment filter list.

ALTER TABLE project add column CHECK_UNTRANSLATED_SEGMENTS tinyint(1) NOT NULL DEFAULT '0';