# GBS-4026 : Newly added available categories are lost

RENAME TABLE category TO category_segment_comment;
ALTER TABLE category_segment_comment ADD COLUMN IS_ACTIVE CHAR(1) NOT NULL DEFAULT 'Y';

RENAME TABLE post_review_category TO category_post_review;
ALTER TABLE category_post_review ADD COLUMN IS_ACTIVE CHAR(1) NOT NULL DEFAULT 'Y';

RENAME TABLE scorecard_category TO category_scorecard;
ALTER TABLE category_scorecard ADD COLUMN IS_ACTIVE CHAR(1) NOT NULL DEFAULT 'Y';