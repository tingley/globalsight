# GBS-2947: XLIFF files for Idiom - target not populated when segment is not touched offline

ALTER TABLE FILE_PROFILE ADD COLUMN XLF_SOURCE_AS_UNTRANSLATED_TARGET INT(1) DEFAULT 0;
