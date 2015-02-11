# For GBS-2079, GBS-2062

alter table tb_language add index IDX_LANGUAGE_CID (CID);

alter table tb_term add index IDX_TERM_CID (CID);

alter table tb_term add index IDX_TERM_LID (LID);
