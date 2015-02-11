-- 
--  This creates database schema for CAP's snippets for the add/delete local content feature.
-- 
--  CREATE SNIPPET TABLES SCHEMA -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -


--  SNIPPET
-- 
DROP TABLE IF EXISTS SNIPPET CASCADE;
CREATE TABLE SNIPPET
(
  ID BIGINT
     AUTO_INCREMENT
     PRIMARY KEY,
  NAME VARCHAR(100)
     NOT NULL,
  DESCRIPTION VARCHAR(4000),
  LOCALE_ID BIGINT,
  CONSTRAINT FK_SNIPPET_ID FOREIGN KEY(LOCALE_ID) REFERENCES LOCALE(ID),
  CONTENT_STRING VARCHAR(4000),
  CONTENT_CLOB TEXT
) AUTO_INCREMENT = 1000;
