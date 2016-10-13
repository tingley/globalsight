-- #600 and 580
update permissiongroup p1 
 set p1.permission_set = concat(p1.permission_set, '292|293|294|295|296|297|298|299|') 
 where p1.name='Administrator' 
 and p1.company_id != 1 
 and p1.permission_set not like '%|292|293|294|295|296|297|298|299|%';