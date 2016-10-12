## Clear Corpus feature.

-- drop foreign keys first
ALTER TABLE source_page DROP FOREIGN KEY FK_SOURCE_PAGE_CUV_ID;

ALTER TABLE target_page DROP FOREIGN KEY FK_TARGET_PAGE_CUV_ID;

-- drop tables
DROP TABLE IF EXISTS corpus_map;

DROP TABLE IF EXISTS corpus_unit_variant;

DROP TABLE IF EXISTS corpus_unit;

-- drop foreign key column to corpus_unit_variant.id
ALTER TABLE source_page DROP COLUMN cuv_id;

ALTER TABLE target_page DROP COLUMN cuv_id;
