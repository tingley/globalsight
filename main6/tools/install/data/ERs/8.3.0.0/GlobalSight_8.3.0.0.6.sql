-- [#GBS-1328] summary reports - QBR report
-- 228 means "Vendor PO Report"

update permissiongroup
set permission_set = concat(permission_set, '385|')
where instr(permission_set, '|228|') > 0
and instr(permission_set, '|385|') = 0;
