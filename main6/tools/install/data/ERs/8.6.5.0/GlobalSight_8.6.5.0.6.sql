## GBS-3650: context matching needs review.

ALTER TABLE tm_profile ADD COLUMN ICE_PROMOTION_RULES INT(1) NOT NULL DEFAULT 3 AFTER IS_CONTEXT_MATCH;

## If use "Leverage Default Matches" originally, change to enable "Leverage in-context matches".
UPDATE tm_profile SET IS_CONTEXT_MATCH = 'Y' WHERE IS_EXACT_MATCH_LEVERAGING = 'N' AND IS_CONTEXT_MATCH = 'N';
