# Clean default leverage options relevant codes
# "Leverage Default Matches" option had been removed from TM profile UI since 8.6.5.

UPDATE job SET leverage_option = 'in-context' WHERE leverage_option = 'default';

ALTER TABLE target_page DROP COLUMN IS_DEFAULT_CONTEXT_MATCH;

commit;
