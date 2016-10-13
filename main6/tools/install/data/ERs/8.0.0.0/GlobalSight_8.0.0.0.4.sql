INSERT INTO KNOWN_FORMAT_TYPE VALUES ( 
47, 'INDD (CS5)','InDesign INDD CS5','indd_cs5',
   'ADOBE_IMPORTED_EVENT',
   'XML_LOCALIZED_EVENT'
);

insert into SYSTEM_PARAMETER (ID, NAME, VALUE, COMPANY_ID) 
VALUES (77, 'adobe.cs5.dir', '%%adobe_cs5_dir%%', '1');

commit;