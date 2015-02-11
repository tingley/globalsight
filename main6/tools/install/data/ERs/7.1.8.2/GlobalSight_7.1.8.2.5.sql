#1
INSERT INTO EXTENSION (NAME, COMPANY_ID) (
   select 'resx', c.id from company c where c.id not in 
     (select distinct COMPANY_ID from extension where name = 'resx'));

#2
insert into known_format_type (ID, NAME, DESCRIPTION, FORMAT_TYPE, PRE_EXTRACT_EVENT, PRE_MERGE_EVENT)
VALUES (45, 'RESX', 'resx', 'xml', 'XML_IMPORTED_EVENT', 'XML_LOCALIZED_EVENT');

#3
update filter_configuration set known_format_id = '|7|15|16|17|25|45|' where NAME = 'XML Filter';

commit;