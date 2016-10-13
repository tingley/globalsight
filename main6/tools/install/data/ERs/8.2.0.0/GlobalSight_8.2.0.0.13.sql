INSERT INTO EXTENSION (NAME, COMPANY_ID, IS_ACTIVE) (
   select 'lpu', c.id, 'Y' from company c where c.id not in 
     (select distinct COMPANY_ID from extension where name = 'lpu'));

INSERT INTO KNOWN_FORMAT_TYPE VALUES (
   51,'Passolo 2011','Passolo 2011','passolo',
   'PASSOLO_IMPORTED_EVENT', 'XML_LOCALIZED_EVENT'
);

ALTER TABLE `TRANSLATION_UNIT` ADD COLUMN `PASSOLO_STATE` VARCHAR(60) DEFAULT NULL;