# For GBS-1354: Support PO File Format, By Joey

# 1
INSERT INTO EXTENSION (NAME, COMPANY_ID) (
   select 'po', c.id from company c where c.id not in 
     (select distinct COMPANY_ID from extension where name = 'po'));
     
# 2
INSERT INTO KNOWN_FORMAT_TYPE VALUES (
42, 'Portable Object', 'Portable Object File', 'po', 
    'HTML_IMPORTED_EVENT', 'HTML_LOCALIZED_EVENT'
);
