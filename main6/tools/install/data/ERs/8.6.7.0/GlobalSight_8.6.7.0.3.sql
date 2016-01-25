# GBS-4239 : Wholly Internal text segments always are written into storage TM

ALTER TABLE tm_profile ADD IS_WHOLLY_INTERNAL_TEXT_TM CHAR(1) NOT NULL CHECK (IS_WHOLLY_INTERNAL_TEXT_TM IN('Y', 'N'));

UPDATE tm_profile SET IS_WHOLLY_INTERNAL_TEXT_TM='N';

commit;