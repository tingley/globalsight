whenever sqlerror exit failure;

delete from tb_term;
delete from translation_unit_variant;
delete from translation_unit;
delete from source_page_leverage_group;
delete from leverage_group;
delete from project;
delete from translation_memory;
delete from source_page;

insert into source_page values(-1, 'dummy', 0, 'ascii', 'ACTIVE_JOB', '', '', '', '', -1, sysdate);

insert into translation_memory values(1, 'TM A', '', '', '', SYSDATE);
insert into translation_memory values(2, 'TM B', '', '', '', SYSDATE);

insert into project values(1, 'LingManager test', 'test', 'TEST', 1);

insert into leverage_group values(1);
insert into leverage_group values(2);
insert into leverage_group values(3);
insert into leverage_group values(4);
insert into leverage_group values(5);

insert into source_page_leverage_group values(1, -1);
insert into source_page_leverage_group values(2, -1);
insert into source_page_leverage_group values(3, -1);

--  id, tm_id, data_type, tu_type, localize_type, leverage_group_id
insert into translation_unit values(1, 1, 1, 'html', 'type-a', 'T', 1);
insert into translation_unit values(2, 1, 1, 'html', 'type-b', 'T', 1);
insert into translation_unit values(3, 1, 1, 'html', 'type-a', 'L', 1);
insert into translation_unit values(4, 1, 1, 'html', 'type-b', 'L', 1);

insert into translation_unit values(5, 1, 1, 'html', 'type-a', 'T', 2);
insert into translation_unit values(6, 1, 1, 'html', 'type-b', 'T', 2);
insert into translation_unit values(7, 1, 1, 'html', 'type-a', 'L', 2);
insert into translation_unit values(8, 1, 1, 'html', 'type-b', 'L', 2);

insert into translation_unit values(9, 1, 2, 'html', 'type-a', 'T', 3);
insert into translation_unit values(10, 1, 2, 'html', 'type-b', 'T', 3);
insert into translation_unit values(11, 1, 2, 'html', 'type-a', 'L', 3);
insert into translation_unit values(12, 1, 2, 'html', 'type-b', 'L', 3);

insert into translation_unit values(13, 1, 2, 'html', 'type-a', 'T', 4);
insert into translation_unit values(14, 1, 2, 'html', 'type-b', 'T', 4);
insert into translation_unit values(15, 1, 2, 'html', 'type-a', 'L', 4);
insert into translation_unit values(16, 1, 2, 'html', 'type-b', 'L', 4);

insert into translation_unit values(17, 1, 2, 'html', 'type-b', 'L', 5);

-- id, locale_id, tu_id, segment, word_count, exact_match_key, state, timestamp
insert into translation_unit_variant values(1, 1, 32, 1, 'N', '',
       'EN type-a T LOCALIZED 1', 0, 1, 'LOCALIZED', sysdate);
insert into translation_unit_variant values(2, 1, 57, 1, 'N', '',
       'FR type-a T LOCALIZED 1', 0, 1, 'LOCALIZED', sysdate);
insert into translation_unit_variant values(3, 1, 64, 1, 'N', '',
       'JA type-a T LOCALIZED 1', 0, 1, 'LOCALIZED', sysdate);
insert into translation_unit_variant values(4, 1, 57, 1, 'N', '',
       'FR type-a T NOT_LOCALIZED 1', 0, 1, 'NOT_LOCALIZED', sysdate);
insert into translation_unit_variant values(5, 1, 64, 1, 'N', '',
       'JA type-a T OUT_OF_DATE 1', 0, 1, 'OUT_OF_DATE', sysdate);

insert into translation_unit_variant values(6, 1, 32, 2, 'N', '',
       'EN type-b T COMPLETE 2', 0, 2, 'COMPLETE', sysdate);
insert into translation_unit_variant values(7, 1, 57, 2, 'N', '',
       'FR type-b T COMPLETE 2', 0, 2, 'COMPLETE', sysdate);
insert into translation_unit_variant values(8, 1, 64, 2, 'N', '',
       'JA type-b T COMPLETE 2', 0, 2, 'COMPLETE', sysdate);
insert into translation_unit_variant values(9, 1, 57, 2, 'N', '',
       'FR type-b T NOT_LOCALIZED 2', 0, 2, 'NOT_LOCALIZED', sysdate);
insert into translation_unit_variant values(10,1,  64, 2, 'N', '',
       'JA type-b T OUT_OF_DATE 2', 0, 2, 'OUT_OF_DATE', sysdate);

insert into translation_unit_variant values(11, 1, 32, 3, 'N', '',
       'EN type-a L LOCALIZED 2', 0, 1, 'LOCALIZED', sysdate);
insert into translation_unit_variant values(12, 1, 57, 3, 'N', '',
       'FR type-a L LOCALIZED 2', 0, 1, 'LOCALIZED', sysdate);
insert into translation_unit_variant values(13, 1, 64, 3, 'N', '',
       'JA type-a L LOCALIZED 2', 0, 1, 'LOCALIZED', sysdate);
insert into translation_unit_variant values(14, 1, 57, 3, 'N', '',
       'FR type-a L NOT_LOCALIZED 2', 0, 1, 'NOT_LOCALIZED', sysdate);
insert into translation_unit_variant values(15, 1, 64, 3, 'N', '',
       'JA type-a L OUT_OF_DATE 2', 0, 1, 'OUT_OF_DATE', sysdate);

insert into translation_unit_variant values(16, 1, 32, 4, 'N', '',
       'EN type-b L COMPLETE 2', 0, 2, 'COMPLETE', sysdate);
insert into translation_unit_variant values(17, 1, 57, 4, 'N', '',
       'FR type-b L COMPLETE 2', 0, 2, 'COMPLETE', sysdate);
insert into translation_unit_variant values(18, 1, 64, 4, 'N', '',
       'JA type-b L COMPLETE 2', 0, 2, 'COMPLETE', sysdate);
insert into translation_unit_variant values(19, 1, 57, 4, 'N', '',
       'FR type-b L NOT_LOCALIZED 2', 0, 2, 'NOT_LOCALIZED', sysdate);
insert into translation_unit_variant values(20, 1, 64, 4, 'N', '',
       'JA type-b L OUT_OF_DATE 2', 0, 2, 'OUT_OF_DATE', sysdate);

insert into translation_unit_variant values(21, 1, 32, 5, 'N', '',
       'EN type-a T LOCALIZED 1', 0, 1, 'LOCALIZED', sysdate);
insert into translation_unit_variant values(22, 1, 57, 5, 'N', '',
       'FR type-a T LOCALIZED 1', 0, 1, 'LOCALIZED', sysdate);
insert into translation_unit_variant values(23, 1, 64, 5, 'N', '',
       'JA type-a T LOCALIZED 1', 0, 1, 'LOCALIZED', sysdate);
insert into translation_unit_variant values(24, 1, 57, 5, 'N', '',
       'FR type-a T NOT_LOCALIZED 1', 0, 1, 'NOT_LOCALIZED', sysdate);
insert into translation_unit_variant values(25, 1, 64, 5, 'N', '',
       'JA type-a T OUT_OF_DATE 1', 0, 1, 'OUT_OF_DATE', sysdate);

insert into translation_unit_variant values(26, 1, 32, 6, 'N', '',
       'EN type-b T COMPLETE 2', 0, 2, 'COMPLETE', sysdate);
insert into translation_unit_variant values(27, 1, 57, 6, 'N', '',
       'FR type-b T COMPLETE 2', 0, 2, 'COMPLETE', sysdate);
insert into translation_unit_variant values(28, 1, 64, 6, 'N', '',
       'JA type-b T COMPLETE 2', 0, 2, 'COMPLETE', sysdate);
insert into translation_unit_variant values(29, 1, 57, 6, 'N', '',
       'FR type-b T NOT_LOCALIZED 2', 0, 2, 'NOT_LOCALIZED', sysdate);
insert into translation_unit_variant values(30, 1, 64, 6, 'N', '',
       'JA type-b T OUT_OF_DATE 2', 0, 2, 'OUT_OF_DATE', sysdate);

insert into translation_unit_variant values(31, 1, 32, 7, 'N', '',
       'EN type-a L LOCALIZED 1', 0, 1, 'LOCALIZED', sysdate);
insert into translation_unit_variant values(32, 1, 57, 7, 'N', '',
       'FR type-a L LOCALIZED 1', 0, 1, 'LOCALIZED', sysdate);
insert into translation_unit_variant values(33, 1, 64, 7, 'N', '',
       'JA type-a L LOCALIZED 1', 0, 1, 'LOCALIZED', sysdate);
insert into translation_unit_variant values(34, 1, 57, 7, 'N', '',
       'FR type-a L NOT_LOCALIZED 1', 0, 1, 'NOT_LOCALIZED', sysdate);
insert into translation_unit_variant values(35, 1, 64, 7, 'N', '',
       'JA type-a L OUT_OF_DATE 1', 0, 1, 'OUT_OF_DATE', sysdate);

insert into translation_unit_variant values(36, 1, 32, 8, 'N', '',
       'EN type-b L COMPLETE 2', 0, 2, 'COMPLETE', sysdate);
insert into translation_unit_variant values(37, 1, 57, 8, 'N', '',
       'FR type-b L COMPLETE 2', 0, 2, 'COMPLETE', sysdate);
insert into translation_unit_variant values(38, 1, 64, 8, 'N', '',
       'JA type-b L COMPLETE 2', 0, 2, 'COMPLETE', sysdate);
insert into translation_unit_variant values(39, 1, 57, 8, 'N', '',
       'FR type-b L NOT_LOCALIZED 2', 0, 2, 'NOT_LOCALIZED', sysdate);
insert into translation_unit_variant values(40, 1, 64, 8, 'N', '',
       'JA type-b L OUT_OF_DATE 2', 0, 2, 'OUT_OF_DATE', sysdate);

insert into translation_unit_variant values(41, 1, 32, 9, 'N', '',
       'EN type-a T LOCALIZED 1', 0, 1, 'LOCALIZED', sysdate);
insert into translation_unit_variant values(42, 1, 57, 9, 'N', '',
       'FR type-a T LOCALIZED 1', 0, 1, 'LOCALIZED', sysdate);
insert into translation_unit_variant values(43, 1, 64, 9, 'N', '',
       'JA type-a T LOCALIZED 1', 0, 1, 'LOCALIZED', sysdate);
insert into translation_unit_variant values(44, 1, 25, 9, 'N', '',
       'DE type-a T LOCALIZED 1', 0, 1, 'LOCALIZED', sysdate);
insert into translation_unit_variant values(45, 1, 63, 9, 'N', '',
       'IT type-a T LOCALIZED 1', 0, 1, 'LOCALIZED', sysdate);

insert into translation_unit_variant values(46, 1, 32, 10, 'N', '',
       'EN type-b T COMPLETE 2', 0, 2, 'COMPLETE', sysdate);
insert into translation_unit_variant values(47, 1, 57, 10, 'N', '',
       'FR type-b T COMPLETE 2', 0, 2, 'COMPLETE', sysdate);
insert into translation_unit_variant values(48, 1, 64, 10, 'N', '',
       'JA type-b T COMPLETE 2', 0, 2, 'COMPLETE', sysdate);
insert into translation_unit_variant values(49, 1, 25, 10, 'N', '',
       'DE type-b T COMPLETE 2', 0, 2, 'COMPLETE', sysdate);
insert into translation_unit_variant values(50, 1, 63, 10, 'N', '',
       'IT type-b T COMPLETE 2', 0, 2, 'COMPLETE', sysdate);

insert into translation_unit_variant values(51, 1, 32, 11, 'N', '',
       'EN type-a L LOCALIZED 2', 0, 1, 'LOCALIZED', sysdate);
insert into translation_unit_variant values(52, 1, 57, 11, 'N', '',
       'FR type-a L LOCALIZED 2', 0, 1, 'LOCALIZED', sysdate);
insert into translation_unit_variant values(53, 1, 64, 11, 'N', '',
       'JA type-a L LOCALIZED 2', 0, 1, 'LOCALIZED', sysdate);
insert into translation_unit_variant values(54, 1, 25, 11, 'N', '',
       'DE type-a L LOCALIZED 2', 0, 1, 'LOCALIZED', sysdate);
insert into translation_unit_variant values(55, 1, 63, 11, 'N', '',
       'IT type-a L LOCALIZED 2', 0, 1, 'LOCALIZED', sysdate);

insert into translation_unit_variant values(56, 1, 32, 12, 'N', '',
       'EN type-b L COMPLETE 2', 0, 2, 'COMPLETE', sysdate);
insert into translation_unit_variant values(57, 1, 57, 12, 'N', '',
       'FR type-b L COMPLETE 2', 0, 2, 'COMPLETE', sysdate);
insert into translation_unit_variant values(58, 1, 64, 12, 'N', '',
       'JA type-b L COMPLETE 2', 0, 2, 'COMPLETE', sysdate);
insert into translation_unit_variant values(59, 1, 25, 12, 'N', '',
       'DE type-b L COMPLETE 2', 0, 2, 'COMPLETE', sysdate);
insert into translation_unit_variant values(60, 1, 63, 12, 'N', '',
       'IT type-b L COMPLETE 2', 0, 2, 'COMPLETE', sysdate);

insert into translation_unit_variant values(61, 1, 32, 13, 'N', '',
       'EN type-a T LOCALIZED 1', 0, 1, 'LOCALIZED', sysdate);
insert into translation_unit_variant values(62, 1, 57, 13, 'N', '',
       'FR type-a T LOCALIZED 1', 0, 1, 'LOCALIZED', sysdate);
insert into translation_unit_variant values(63, 1, 64, 13, 'N', '',
       'JA type-a T LOCALIZED 1', 0, 1, 'LOCALIZED', sysdate);
insert into translation_unit_variant values(64, 1, 25, 13, 'N', '',
       'DE type-a T LOCALIZED 1', 0, 1, 'LOCALIZED', sysdate);
insert into translation_unit_variant values(65, 1, 63, 13, 'N', '',
       'IT type-a T LOCALIZED 1', 0, 1, 'LOCALIZED', sysdate);

insert into translation_unit_variant values(66, 1, 32, 14, 'N', '',
       'EN type-b T COMPLETE 2', 0, 2, 'COMPLETE', sysdate);
insert into translation_unit_variant values(67, 1, 57, 14, 'N', '',
       'FR type-b T COMPLETE 2', 0, 2, 'COMPLETE', sysdate);
insert into translation_unit_variant values(68, 1, 64, 14, 'N', '',
       'JA type-b T COMPLETE 2', 0, 2, 'COMPLETE', sysdate);
insert into translation_unit_variant values(69, 1, 25, 14, 'N', '',
       'DE type-b T COMPLETE 2', 0, 2, 'COMPLETE', sysdate);
insert into translation_unit_variant values(70, 1, 63, 14, 'N', '',
       'IT type-b T COMPLETE 2', 0, 2, 'COMPLETE', sysdate);

insert into translation_unit_variant values(71, 1, 32, 15, 'N', '',
       'EN type-a L LOCALIZED 1', 0, 1, 'LOCALIZED', sysdate);
insert into translation_unit_variant values(72, 1, 57, 15, 'N', '',
       'FR type-a L LOCALIZED 1', 0, 1, 'LOCALIZED', sysdate);
insert into translation_unit_variant values(73, 1, 64, 15, 'N', '',
       'JA type-a L LOCALIZED 1', 0, 1, 'LOCALIZED', sysdate);
insert into translation_unit_variant values(74, 1, 25, 15, 'N', '',
       'DE type-a L LOCALIZED 1', 0, 1, 'LOCALIZED', sysdate);
insert into translation_unit_variant values(75, 1, 63, 15, 'N', '',
       'IT type-a L LOCALIZED 1', 0, 1, 'LOCALIZED', sysdate);

insert into translation_unit_variant values(76, 1, 32, 16, 'N', '',
       'EN type-b L COMPLETE 2', 0, 2, 'COMPLETE', sysdate);
insert into translation_unit_variant values(77, 1, 57, 16, 'N', '',
       'FR type-b L COMPLETE 2', 0, 2, 'COMPLETE', sysdate);
insert into translation_unit_variant values(78, 1, 64, 16, 'N', '',
       'JA type-b L COMPLETE 2', 0, 2, 'COMPLETE', sysdate);
insert into translation_unit_variant values(79, 1, 25, 16, 'N', '',
       'DE type-b L COMPLETE 2', 0, 2, 'COMPLETE', sysdate);
insert into translation_unit_variant values(80, 1, 63, 16, 'N', '',
       'IT type-b L COMPLETE 2', 0, 2, 'COMPLETE', sysdate);

insert into translation_unit_variant values(81, 1, 32, 17, 'N', '',
       'EN type-b L COMPLETE 2', 0, 3, 'COMPLETE', sysdate);


insert into tb_term values(1, 1, 1, 1, 'en', 'en 1', ' ', ' ', '1', '', sysdate, '', sysdate, '');
insert into tb_term values(1, 2, 1, 2, 'en', 'en 2', ' ', ' ', '1', '', sysdate, '', sysdate, '');
insert into tb_term values(1, 3, 1, 3, 'en', 'en 3', ' ', ' ', '1', '', sysdate, '', sysdate, '');
insert into tb_term values(1, 4, 1, 4, 'en', 'en 4', ' ', ' ', '1', '', sysdate, '', sysdate, '');
insert into tb_term values(1, 5, 1, 5, 'en', 'en 5', ' ', ' ', '1', '', sysdate, '', sysdate, '');
insert into tb_term values(1, 6, 1, 6, 'en', 'en 6', ' ', ' ', '1', '', sysdate, '', sysdate, '');
insert into tb_term values(1, 7, 1, 7, 'en', 'en 7', ' ', ' ', '1', '', sysdate, '', sysdate, '');
insert into tb_term values(1, 8, 1, 8, 'en', 'en 8', ' ', ' ', '1', '', sysdate, '', sysdate, '');
insert into tb_term values(1, 9, 1, 9, 'en', 'en 9', ' ', ' ', '1', '', sysdate, '', sysdate, '');
insert into tb_term values(1, 10, 1, 10, 'en', 'en 10', ' ', ' ', '1', '', sysdate, '', sysdate, '');

insert into tb_term values(1, 11, 1, 1, 'fr', 'fr 1', ' ', ' ', '1', '', sysdate, '', sysdate, '');
insert into tb_term values(1, 12, 1, 2, 'fr', 'fr 2', ' ', ' ', '1', '', sysdate, '', sysdate, '');
insert into tb_term values(1, 13, 1, 3, 'fr', 'fr 3', ' ', ' ', '1', '', sysdate, '', sysdate, '');
insert into tb_term values(1, 14, 1, 4, 'fr', 'fr 4', ' ', ' ', '1', '', sysdate, '', sysdate, '');
insert into tb_term values(1, 15, 1, 5, 'fr', 'fr 5', ' ', ' ', '1', '', sysdate, '', sysdate, '');
insert into tb_term values(1, 16, 1, 6, 'fr', 'fr 6', ' ', ' ', '1', '', sysdate, '', sysdate, '');
insert into tb_term values(1, 17, 1, 7, 'fr', 'fr 7', ' ', ' ', '1', '', sysdate, '', sysdate, '');
insert into tb_term values(1, 18, 1, 8, 'fr', 'fr 8', ' ', ' ', '1', '', sysdate, '', sysdate, '');
insert into tb_term values(1, 19, 1, 9, 'fr', 'fr 9', ' ', ' ', '1', '', sysdate, '', sysdate, '');
insert into tb_term values(1, 20, 1, 10, 'fr', 'fr 10', ' ', ' ', '1', '', sysdate, '', sysdate, '');

exit;
