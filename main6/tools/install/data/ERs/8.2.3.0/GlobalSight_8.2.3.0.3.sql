# For GBS-2451 CS5.5
INSERT INTO KNOWN_FORMAT_TYPE VALUES (
   52,'INDD (CS5.5)','InDesign INDD CS5.5','indd_cs5.5',
   'ADOBE_IMPORTED_EVENT', 'XML_LOCALIZED_EVENT'
);

insert into SYSTEM_PARAMETER (ID, NAME, VALUE, COMPANY_ID) 
VALUES (80, 'adobe.cs5.5.dir', '%%adobe_cs5.5_dir%%', '1');
