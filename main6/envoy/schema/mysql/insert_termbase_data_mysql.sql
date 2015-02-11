--  insert_termbase_data.sql
--       

--  define a sample termbase 

INSERT INTO TB_TERMBASE (TBID, TB_NAME, TB_DESCRIPTION, TB_DEFINITION, COMPANYID) VALUES (
       1, 'Sample', 'Sample Termbase', '<definition><name>Sample</name><description>Sample Termbase</description><languages><language><name>English</name><locale>en</locale><hasterms>true</hasterms></language><language><name>French</name><locale>fr</locale><hasterms>true</hasterms></language><language><name>Spanish</name><locale>es</locale><hasterms>true</hasterms></language><language><name>German</name><locale>de</locale><hasterms>true</hasterms></language></languages><fields></fields></definition>', 1);

INSERT INTO TB_SEQUENCE (NAME, VALUE) VALUES ('tbid', 2);
INSERT INTO TB_SEQUENCE (NAME, VALUE) VALUES ('cid', 1);
INSERT INTO TB_SEQUENCE (NAME, VALUE) VALUES ('lid', 1);
INSERT INTO TB_SEQUENCE (NAME, VALUE) VALUES ('tid', 1);

commit;
