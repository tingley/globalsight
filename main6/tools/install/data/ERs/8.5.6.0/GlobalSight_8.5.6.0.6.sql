# [GBS-3336] GlobalSight IDML filter updates required. - Conditional Text

ALTER TABLE indd_filter ADD COLUMN TRANSLATE_HIDDEN_CONDTEXT CHAR(1) NOT NULL DEFAULT 'Y' AFTER TRANSLATE_HYPERLINKS;
ALTER TABLE indd_filter ADD COLUMN SKIP_TRACKING_KERNING CHAR(1) NOT NULL DEFAULT 'N' AFTER TRANSLATE_HIDDEN_CONDTEXT;