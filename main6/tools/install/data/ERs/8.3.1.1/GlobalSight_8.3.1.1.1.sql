-- GBS-2957: Code optimization to speed up word count calculation.

-- In original implementation, in "workflow" table, "REPETITION_WORD_COUNT" column stores repetitions of no match.
-- Now, it stores all repetitions word count.
UPDATE workflow
SET workflow.REPETITION_WORD_COUNT = 
    workflow.REPETITION_WORD_COUNT + 
    workflow.SUB_LEV_REPETITION_WORD_COUNT + 
    workflow.FUZZY_MED_REPETITION_WORD_COUNT + 
    workflow.FUZZY_MED_HI_REPETITION_WORD_COUNT + 
    workflow.FUZZY_HI_REPETITION_WORD_COUNT;

-- In original implementation, in "workflow" table, "FUZZY_LOW_WORD_COUNT" stores sum of "SUB_LEV_REPETITION_WORD_COUNT" and "SUB_LEV_MATCH_WORD_COUNT".
-- Now, it stores only "low fuzzy" word count.
UPDATE workflow
SET workflow.FUZZY_LOW_WORD_COUNT = 
    workflow.FUZZY_LOW_WORD_COUNT - 
    workflow.SUB_LEV_REPETITION_WORD_COUNT;

-- Drop the 5 useless columns from "workflow" table.
ALTER TABLE workflow DROP COLUMN SUB_LEV_MATCH_WORD_COUNT;
ALTER TABLE workflow DROP COLUMN SUB_LEV_REPETITION_WORD_COUNT;
ALTER TABLE workflow DROP COLUMN FUZZY_MED_REPETITION_WORD_COUNT;
ALTER TABLE workflow DROP COLUMN FUZZY_MED_HI_REPETITION_WORD_COUNT;
ALTER TABLE workflow DROP COLUMN FUZZY_HI_REPETITION_WORD_COUNT;

-- Drop 2 useless columns from "target_page" table.
ALTER TABLE target_page DROP COLUMN SUB_LEV_MATCH_WORD_COUNT;
ALTER TABLE target_page DROP COLUMN SUB_LEV_REPETITION_WORD_COUNT;
