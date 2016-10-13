INSERT INTO KNOWN_FORMAT_TYPE VALUES ( 
49, 'MIF','Adobe Framemaker9','mif',
   'MIF_IMPORTED_EVENT',
   'MIF_LOCALIZED_EVENT'
);

INSERT INTO EXTENSION VALUES (
   40, 'mif', 1, 'Y'
);

INSERT INTO EXTENSION (NAME, COMPANY_ID, IS_ACTIVE) (
   select 'mif', c.id, c.is_active from company c where c.id not in 
     (select distinct COMPANY_ID from extension where name = 'mif'));