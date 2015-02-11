-- This script reports the top 5 SQL statements executed
-- since Oracle was last started.
   
SELECT address, SUBSTR(sql_text,1,20) Text, buffer_gets, executions,
buffer_gets/executions AVG
FROM v$sqlarea
WHERE executions > 0
AND buffer_gets > 100000
ORDER BY 5;

-- Remember that the 'buffer_gets' value of > 100000 needs to be 
-- varied for the individual system being tuned. On some systems no 
-- queries will read more than 100000 buffers, while on others most 
-- of them will. This value allows you to control how many rows you 
-- see returned from the select. The ADDRESS value retrieved above can 
-- then be used to lookup the whole statement in the v$sqltext view:
-- 
-- SELECT sql_text FROM v$sqltext WHERE address = '...' ORDER BY piece;
-- 
-- Once the whole statement has been identified it can be tuned to 
-- reduce resource usage.
