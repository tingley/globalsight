-- 
--  Inserts default file extensions
-- 
     
INSERT INTO EXTENSION VALUES (
   1,'htm', 1, 'Y'
);
INSERT INTO EXTENSION VALUES (
   2,'html', 1, 'Y'
);
/*
INSERT INTO EXTENSION VALUES (
   4,'shtml', 1
);
INSERT INTO EXTENSION VALUES (
   5,'jhtml', 1
);*/
INSERT INTO EXTENSION VALUES (
   6,'txt', 1, 'Y'
);
/*
INSERT INTO EXTENSION VALUES (
   7,'css', 1
);*/
INSERT INTO EXTENSION VALUES (
   8,'js', 1, 'Y'
);
INSERT INTO EXTENSION VALUES (
   9,'properties', 1, 'Y'
);
/*
INSERT INTO EXTENSION VALUES (
   10,'cfm', 1
);
INSERT INTO EXTENSION VALUES (
   11,'cfml', 1
);
INSERT INTO EXTENSION VALUES (
   12,'asp', 1
);*/
INSERT INTO EXTENSION VALUES (
   13,'jsp', 1, 'Y'
);
INSERT INTO EXTENSION VALUES (
   14,'xml', 1, 'Y'
);
INSERT INTO EXTENSION VALUES (
   15,'doc', 1, 'Y'
);
INSERT INTO EXTENSION VALUES (
   16,'xls', 1, 'Y'
);
INSERT INTO EXTENSION VALUES (
   17,'ppt', 1, 'Y'
);
INSERT INTO EXTENSION VALUES (
   18,'fm', 1, 'Y'
);
/*
INSERT INTO EXTENSION VALUES (
   19,'qxd', 1
);
INSERT INTO EXTENSION VALUES (
   20,'cpp', 1
);*/
INSERT INTO EXTENSION VALUES (
   21,'java', 1, 'Y'
);
/*
INSERT INTO EXTENSION VALUES (
   22,'pdf', 1
);*/
INSERT INTO EXTENSION VALUES (
   23,'indd', 1, 'Y'
);
/*
INSERT INTO EXTENSION VALUES (
   24,'ai', 1
);*/

INSERT INTO EXTENSION VALUES (
   25,'rtf', 1, 'Y'
);

INSERT INTO EXTENSION VALUES (
   26,'docx', 1, 'Y'
);

INSERT INTO EXTENSION VALUES (
   27,'pptx', 1, 'Y'
);

INSERT INTO EXTENSION VALUES (
   28,'xlsx', 1, 'Y'
);

INSERT INTO EXTENSION VALUES (
   29,'inx', 1, 'Y'
);

INSERT INTO EXTENSION VALUES (
   30,'xlf', 1, 'Y'
);

INSERT INTO EXTENSION VALUES (
   31,'xliff', 1, 'Y'
);

INSERT INTO EXTENSION VALUES (
   32,'odt', 1, 'Y'
);

INSERT INTO EXTENSION VALUES (
   33,'ods', 1, 'Y'
);

INSERT INTO EXTENSION VALUES (
   34,'odp', 1, 'Y'
);

INSERT INTO EXTENSION VALUES (
   35,'po', 1, 'Y'
);

INSERT INTO EXTENSION VALUES (
   36,'rc', 1, 'Y'
);

INSERT INTO EXTENSION VALUES (
   37,'resx', 1, 'Y'
);

INSERT INTO EXTENSION VALUES (
   38,'idml', 1, 'Y'
);

-- GBS-1785, Vincent Yan, 2011/01/29
INSERT INTO EXTENSION VALUES (
   39,'xlz', 1, 'Y'
);

INSERT INTO EXTENSION VALUES (
   40, 'mif', 1, 'Y'
);

INSERT INTO EXTENSION VALUES (
   41,'lpu', 1, 'Y'
);

INSERT INTO EXTENSION VALUES (
   42,'exe', 1, 'Y'
);

INSERT INTO EXTENSION VALUES (
   43,'dll', 1, 'Y'
);
--  
--  NOTE: ids >= 500 and < 1000 are reserved for customer-specific 
--  predefined extensions.
-- 

commit;

