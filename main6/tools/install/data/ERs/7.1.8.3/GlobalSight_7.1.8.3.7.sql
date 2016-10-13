# For GBS-1416 indesign IDML support
INSERT INTO EXTENSION (NAME, COMPANY_ID) (
   select 'idml', c.id from company c where c.id not in 
     (select distinct COMPANY_ID from extension where name = 'idml'));

INSERT INTO KNOWN_FORMAT_TYPE VALUES (
   46,'InDesign Markup (IDML)','idml','xml',
   'IDML_IMPORTED_EVENT', 'XML_LOCALIZED_EVENT'
);