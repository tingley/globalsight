# For GBS-1304
INSERT INTO EXTENSION (NAME, COMPANY_ID) (
   select 'rc', c.id from company c where c.id not in 
     (select distinct COMPANY_ID from extension where name = 'rc'));

INSERT INTO KNOWN_FORMAT_TYPE VALUES (
   44,'Resource Compiler','Resource Compiler','rc',
   'HTML_IMPORTED_EVENT',
   'HTML_LOCALIZED_EVENT'
);