# For GBS-2055, by Joey on 2011-08-10

# 1.Add column 'EMAIL'
ALTER TABLE COMPANY ADD COLUMN EMAIL VARCHAR(100) DEFAULT NULL;

# 2.Update the data
update COMPANY c, SYSTEM_PARAMETER SYS 
set c.email = sys.VALUE
where c.email is null 
and c.ID = sys.COMPANY_ID 
and sys.NAME = 'admin.email';

update COMPANY c
set c.email = (select VALUE from SYSTEM_PARAMETER where COMPANY_ID = 1 and NAME = 'admin.email')
where c.email is null;

