-- This script tells you how full the System4
-- table spaces are. The tablespaces queried are
-- 'INDX','RBS','SYSTEM4','TEMP'.

set lines 130
set pages 200
spool tablespace.log

SELECT d.tablespace_name "Name",
       TO_CHAR(NVL((a.bytes - NVL(f.bytes, 0)) / a.bytes * 100, 0), '990.00') "Used %"
FROM   sys.dba_tablespaces d,
       (select tablespace_name, sum(bytes) bytes from dba_data_files group by tablespace_name) a,
       (select tablespace_name, sum(bytes) bytes from dba_free_space group by tablespace_name) f
WHERE d.tablespace_name = a.tablespace_name(+)
AND   d.tablespace_name = f.tablespace_name(+)
AND   d.tablespace_name in ('INDX','RBS','SYSTEM4','TEMP')
AND NOT (d.extent_management like 'LOCAL' AND d.contents like 'TEMPORARY')
UNION ALL SELECT d.tablespace_name "Name",
                 TO_CHAR(NVL(t.bytes / a.bytes * 100, 0), '990.00') "Used %"
          FROM   sys.dba_tablespaces d,
                 (select tablespace_name, sum(bytes) bytes from dba_temp_files group by tablespace_name) a,
                 (select tablespace_name, sum(bytes_cached) bytes from v$temp_extent_pool group by tablespace_name) t
          WHERE d.tablespace_name = a.tablespace_name(+)
          AND   d.tablespace_name = t.tablespace_name(+)
          AND   d.extent_management like 'LOCAL'
          AND   d.contents like 'TEMPORARY'
          AND   d.tablespace_name in ('INDX','RBS','SYSTEM4','TEMP');

prompt Saving results to tablespace.log 
prompt 
spool off;
exit;
/