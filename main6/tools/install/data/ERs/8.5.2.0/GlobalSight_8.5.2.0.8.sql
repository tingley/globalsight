## for GBS-3268 Refactor Office 2010 Extractor and Filter. (Docx)
alter table known_format_type
   change NAME
   NAME VARCHAR(60)
     NOT NULL
     UNIQUE;
     
INSERT INTO KNOWN_FORMAT_TYPE VALUES (
54, '(Beta) New Office 2010 Filter (DOCX only)','Microsoft Office 2010 document','office-xml',
   'MSOFFICE_IMPORTED_EVENT',
   'XML_LOCALIZED_EVENT'
);

     
UPDATE filter_configuration SET KNOWN_FORMAT_ID = "|43|54|" WHERE KNOWN_FORMAT_ID = "|43|";