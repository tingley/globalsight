# GBS-2772: Change column "no_use_exact_match_word_count" to "total_exact_match_word_count".

ALTER TABLE workflow CHANGE NO_USE_EXACT_MATCH_WORD_COUNT TOTAL_EXACT_MATCH_WORD_COUNT INT(10);

ALTER TABLE target_page CHANGE NO_USE_EXACT_MATCH_WORD_COUNT TOTAL_EXACT_MATCH_WORD_COUNT INT(10);
