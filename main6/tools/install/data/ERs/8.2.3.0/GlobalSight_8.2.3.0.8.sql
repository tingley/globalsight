# For GBS-2516 dll and exe
INSERT INTO EXTENSION (NAME, COMPANY_ID, IS_ACTIVE) (
   select 'exe', c.id, 'Y' from company c where c.id not in 
     (select distinct COMPANY_ID from extension where name = 'exe'));
     
INSERT INTO EXTENSION (NAME, COMPANY_ID, IS_ACTIVE) (
   select 'dll', c.id, 'Y' from company c where c.id not in 
     (select distinct COMPANY_ID from extension where name = 'dll'));

INSERT INTO KNOWN_FORMAT_TYPE VALUES (
   53,'Windows Portable Executable','Windows Portable Executable','windows_pe',
   'WINPE_IMPORTED_EVENT', 'XML_LOCALIZED_EVENT'
);

insert into SYSTEM_PARAMETER (ID, NAME, VALUE, COMPANY_ID) VALUES (81, 'winpe.installKey', 'false', '1');
insert into SYSTEM_PARAMETER (ID, NAME, VALUE, COMPANY_ID) VALUES (82, 'winpe.dir', '', '1');