REM This script checks to see if the LEV_MATCH2, SEGMENT_TM_QUERY,
REM TOKEN_INSERT and TMLEV_MATCH stored procedures are valid. The
REM query results are written to a file, which are then read by
REM the install program.

--  turn off printing of blank lines
SET NEWPAGE NONE  

--  turn off page breaks  
SET PAGESIZE 0 

--  turn off column headings    
SET HEADING OFF  

--  turn off feedback from START command  
SET ECHO OFF     

--  turn off feedback on number of rows returned  
SET FEEDBACK OFF  

--   no output to the terminal 
SET TERM OFF    

--  trim trailing spaces in the spool file  
SET TRIMSPOOL ON  

spool data/sql_sp.tmp;
col object_name format a25;
col object_type format a12;
select object_name, object_type, '=', status 
	from user_objects where 
	object_name IN ('LEV_MATCH2', 'SEGMENT_TM_QUERY',
	                'TOKEN_INSERT', 'TMLEV_MATCH')
	order by object_name, object_type;
spool off;

