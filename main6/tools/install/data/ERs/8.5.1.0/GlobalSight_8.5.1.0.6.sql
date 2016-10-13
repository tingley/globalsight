## GBS-3204: Restore missing table indexes removed during GBS-2384.

# template_part
CREATE INDEX IDX_TU_ID ON TEMPLATE_PART(TU_ID);

# template_part_archived
CREATE INDEX IDX_TU_ID ON TEMPLATE_PART_ARCHIVED(TU_ID);

# removed_tag
CREATE INDEX IDX_TU_ID ON REMOVED_TAG(TU_ID);

# removed_prefix_tag
CREATE INDEX IDX_TU_ID ON REMOVED_PREFIX_TAG(TU_ID);

# removed_suffix_tag
CREATE INDEX IDX_TU_ID ON REMOVED_SUFFIX_TAG(TU_ID);

# ip_tm_trg_t
CREATE INDEX IDX_TU_ID ON IP_TM_TRG_T(TU_ID);
