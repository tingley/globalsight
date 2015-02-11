whenever sqlerror exit failure;

delete from leverage_match;
delete from fuzzy_index;
delete from translation_unit_variant;
delete from translation_unit;
delete from leverage_group;
delete from translation_memory;
delete from project;

insert into project values(1, 'Leverager Test', 'test', 'TEST');

insert into leverage_group values(1);
insert into leverage_group values(2);

insert into translation_memory values(1, 'TM', 1);

--  id, tm_id, data_type, tu_type, localize_type, leverage_group_id
insert into translation_unit values(1, 1, 'html', 'type-a', 'T', 1);
insert into translation_unit values(2, 1, 'html', 'type-a', 'T', 1);
insert into translation_unit values(3, 1, 'html', 'type-a', 'T', 1);

insert into translation_unit values(5, 1, 'html', 'type-a', 'T', 2);


-- id, locale_id, tu_id, segment, word_count, exact_match_key, state, timestamp

--- lev group 1
-- One exact
insert into translation_unit_variant values(1, 32, 1,
       'An English test one two three four five', 3, 0, 'LOCALIZED', sysdate);
insert into translation_unit_variant values(2, 57, 1,
       'A French test', 3, 0, 'LOCALIZED', sysdate);

-- two fuzzies
insert into translation_unit_variant values(5, 32, 2,
       'An English test one two three four ', 3, 0, 'LOCALIZED', sysdate);
insert into translation_unit_variant values(6, 57, 2,
       'A French test', 3, 0, 'LOCALIZED', sysdate);

insert into translation_unit_variant values(7, 32, 3,
       'An English test two three four five', 3, 0, 'LOCALIZED', sysdate);
insert into translation_unit_variant values(8, 57, 3,
       'A French test', 3, 0, 'LOCALIZED', sysdate);

--- lev group 2
insert into translation_unit_variant values(3, 32, 5,
       'An English test one two three four five', 3, 0, 'NOT_LOCALIZED', sysdate);
insert into translation_unit_variant values(4, 57, 5,
       'A French test', 3, 0, 'NOT_LOCALIZED', sysdate);

commit;

exit;
