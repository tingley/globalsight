# GBS-2970 pahse 1.

# "target_page" table
# If mt confidence score is 100, MT_TOTAL_WORD_COUNT = MT_EXACT_MATCH_WORD_COUNT;
# If mt confidence score is less than 100, MT_TOTAL_WORD_COUNT = MT_FUZZY_NO_MATCH_WORD_COUNT + MT_REPETITIONS, while MT_EXACT_MATCH_WORD_COUNT = 0 always.
ALTER TABLE target_page ADD COLUMN MT_TOTAL_WORD_COUNT INT DEFAULT 0;
ALTER TABLE target_page ADD COLUMN MT_FUZZY_NO_MATCH_WORD_COUNT INT DEFAULT 0;
ALTER TABLE target_page ADD COLUMN MT_REPETITIONS INT DEFAULT 0;

# In original implementation,only MT exact match word count was stored into DB.
# So if "MT_EXACT_MATCH_WORD_COUNT" does not equals to 0, it should be all MT translations.
UPDATE target_page 
SET MT_TOTAL_WORD_COUNT = MT_EXACT_MATCH_WORD_COUNT 
WHERE MT_EXACT_MATCH_WORD_COUNT != 0;

# "workflow" table
ALTER TABLE workflow ADD COLUMN MT_TOTAL_WORD_COUNT INT DEFAULT 0;
ALTER TABLE workflow ADD COLUMN MT_FUZZY_NO_MATCH_WORD_COUNT INT DEFAULT 0;
ALTER TABLE workflow ADD COLUMN MT_REPETITIONS INT DEFAULT 0;
ALTER TABLE workflow ADD COLUMN USE_MT CHAR(1) DEFAULT 'N';
ALTER TABLE workflow ADD COLUMN MT_CONFIDENCE_SCORE INT DEFAULT 0;

# This update is not accurate, at least it can update this new column for 100 MT confidence score workflows.
# In original implementation,only MT exact match word count was stored into DB.
UPDATE workflow 
SET MT_TOTAL_WORD_COUNT = MT_EXACT_MATCH_WORD_COUNT, workflow.USE_MT = 'Y', workflow.MT_CONFIDENCE_SCORE = 100
WHERE MT_EXACT_MATCH_WORD_COUNT != 0;
