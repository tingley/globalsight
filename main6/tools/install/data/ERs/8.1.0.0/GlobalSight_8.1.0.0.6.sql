INSERT INTO KNOWN_FORMAT_TYPE VALUES (
50, 'FrameMaker9','Adobe Framemaker 9','mif',
   'FRAME_IMPORTED_EVENT',
   'MIF_LOCALIZED_EVENT'
);

INSERT INTO EXTENSION VALUES (
   18, 'fm', 1, 'Y'
);

INSERT INTO EXTENSION (NAME, COMPANY_ID, IS_ACTIVE) (
   select 'fm', c.id, c.is_active from company c where c.id not in 
     (select distinct COMPANY_ID from extension where name = 'fm'));