## GBS-4517 DQF Fields required in GlobalSight

## Create new table for all categories
CREATE TABLE CATEGORIES (
    ID BIGINT(20) NOT NULL AUTO_INCREMENT,
    NAME VARCHAR(200) NOT NULL,
    MEMO VARCHAR(200) DEFAULT NULL,
    TYPE INT DEFAULT 0,
    COMPANY_ID  BIGINT(20) NOT NULL,
    IS_AVAILABLE CHAR(1) NOT NULL DEFAULT 'Y',
    IS_ACTIVE CHAR(1) NOT NULL DEFAULT 'Y',
    PRIMARY KEY (ID)
) ENGINE=INNODB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

ALTER TABLE COMPANY ADD COLUMN DEFAULT_FLUENCY VARCHAR(200);
ALTER TABLE COMPANY ADD COLUMN DEFAULT_ADEQUACY VARCHAR(200);

ALTER TABLE ISSUE ADD COLUMN SEVERITY VARCHAR(200);

ALTER TABLE WORKFLOW ADD COLUMN FLUENCY_SCORE VARCHAR(200);
ALTER TABLE WORKFLOW ADD COLUMN ADEQUACY_SCORE VARCHAR(200);
ALTER TABLE WORKFLOW ADD COLUMN DQF_COMMENT VARCHAR(200);

## Copy all old categories into table categories
INSERT INTO categories (NAME, TYPE, is_available, company_id) SELECT category_name, 3, 'Y', company_id FROM category_post_review WHERE category_type='Q' AND is_active='Y';
INSERT INTO categories (NAME, TYPE, is_available, company_id) SELECT category_name, 3, 'N', company_id FROM category_post_review WHERE category_type='Q' AND is_active='N';

INSERT INTO categories (NAME, TYPE, is_available, company_id) SELECT category_name, 4, 'Y', company_id FROM category_post_review WHERE category_type='M' AND is_active='Y';
INSERT INTO categories (NAME, TYPE, is_available, company_id) SELECT category_name, 4, 'N', company_id FROM category_post_review WHERE category_type='M' AND is_active='N';

INSERT INTO categories (NAME, TYPE, is_available, company_id) SELECT scorecard_category, 2, 'Y', company_id FROM category_scorecard WHERE is_active='Y';
INSERT INTO categories (NAME, TYPE, is_available, company_id) SELECT scorecard_category, 2, 'N', company_id FROM category_scorecard WHERE is_active='N';

INSERT INTO categories (NAME, TYPE, is_available, company_id) SELECT category, 1, 'Y', company_id FROM category_segment_comment WHERE is_active='Y';
INSERT INTO categories (NAME, TYPE, is_available, company_id) SELECT category, 1, 'N', company_id FROM category_segment_comment WHERE is_active='N';

## Rename the name of categories
UPDATE categories SET NAME='Conflicts with glossary or style guide' WHERE NAME='lb_conflicts_glossary_guide';
UPDATE categories SET NAME='Formatting error' WHERE NAME='lb_formatting_error';
UPDATE categories SET NAME='Mistranslated' WHERE NAME='lb_mistranslated';
UPDATE categories SET NAME='Omission of text' WHERE NAME='lb_omission_of_text';
UPDATE categories SET NAME='Spelling or grammar or punctuation error' WHERE NAME='lb_spelling_grammar_punctuation_error';
UPDATE categories SET NAME='Spelling and grammar' WHERE NAME='lb_spelling_grammar';
UPDATE categories SET NAME='Consistency' WHERE NAME='lb_consistency';
UPDATE categories SET NAME='Style' WHERE NAME='lb_style';
UPDATE categories SET NAME='Terminology' WHERE NAME='lb_terminology';
UPDATE categories SET NAME='Good' WHERE NAME='lb_good';
UPDATE categories SET NAME='Acceptable' WHERE NAME='lb_acceptable';
UPDATE categories SET NAME='Poor' WHERE NAME='lb_poor';
UPDATE categories SET NAME='Suitable & Fluent' WHERE NAME='lb_suitable_fluent';
UPDATE categories SET NAME='Literal at Times' WHERE NAME='lb_literal_at_times';
UPDATE categories SET NAME='Unsuitable' WHERE NAME='lb_unsuitable';
UPDATE categories SET NAME='None' WHERE NAME='lb_none';
