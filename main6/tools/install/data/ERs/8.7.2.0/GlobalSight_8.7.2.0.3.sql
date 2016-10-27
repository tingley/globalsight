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

## Add columns for DQF fields
ALTER TABLE COMPANY ADD COLUMN DEFAULT_FLUENCY VARCHAR(200);
ALTER TABLE COMPANY ADD COLUMN DEFAULT_ADEQUACY VARCHAR(200);

## Add columns for Severity field
ALTER TABLE ISSUE ADD COLUMN SEVERITY VARCHAR(200);

## Add columns for DQF fields
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

## Add new DQF categories for old company
INSERT INTO categories (NAME, TYPE, is_available, company_id) SELECT 'lb_dqf_fluency_incomprehensible',5,'Y', id FROM company where is_active='Y' and id>1;
INSERT INTO categories (NAME, TYPE, is_available, company_id) SELECT 'lb_dqf_fluency_disfluent',5,'Y', id FROM company where is_active='Y' and id>1;
INSERT INTO categories (NAME, TYPE, is_available, company_id) SELECT 'lb_dqf_fluency_good',5,'Y', id FROM company where is_active='Y' and id>1;
INSERT INTO categories (NAME, TYPE, is_available, company_id) SELECT 'lb_dqf_fluency_flawless',5,'Y', id FROM company where is_active='Y' and id>1;

INSERT INTO categories (NAME, TYPE, is_available, company_id) SELECT 'lb_none',6,'Y', id FROM company where is_active='Y' and id>1;
INSERT INTO categories (NAME, TYPE, is_available, company_id) SELECT 'lb_dqf_adequacy_little',6,'Y', id FROM company where is_active='Y' and id>1;
INSERT INTO categories (NAME, TYPE, is_available, company_id) SELECT 'lb_dqf_adequacy_most',6,'Y', id FROM company where is_active='Y' and id>1;
INSERT INTO categories (NAME, TYPE, is_available, company_id) SELECT 'lb_dqf_adequacy_everything',6,'Y', id FROM company where is_active='Y' and id>1;

INSERT INTO categories (NAME, TYPE, is_available, company_id) SELECT 'lb_dqf_severity_critical',7,'Y', id FROM company where is_active='Y' and id>1;
INSERT INTO categories (NAME, TYPE, is_available, company_id) SELECT 'lb_dqf_severity_major',7,'Y', id FROM company where is_active='Y' and id>1;
INSERT INTO categories (NAME, TYPE, is_available, company_id) SELECT 'lb_dqf_severity_minor',7,'Y', id FROM company where is_active='Y' and id>1;
INSERT INTO categories (NAME, TYPE, is_available, company_id) SELECT 'lb_dqf_severity_neutral',7,'Y', id FROM company where is_active='Y' and id>1;
INSERT INTO categories (NAME, TYPE, is_available, company_id) SELECT 'lb_dqf_severity_positive',7,'Y', id FROM company where is_active='Y' and id>1;
INSERT INTO categories (NAME, TYPE, is_available, company_id) SELECT 'lb_dqf_severity_invalid',7,'Y', id FROM company where is_active='Y' and id>1;

