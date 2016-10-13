# GBS-2394:upload job permission
update permissiongroup
set permission_set = concat(permission_set, '368|')
where instr(permission_set, '|188|') > 0
and instr(permission_set, '|368|') = 0
and "cvs.admin" = (select name from permission where id=300);

update permissiongroup
set permission_set = concat(permission_set, '367|')
where instr(permission_set, '|188|') > 0
and instr(permission_set, '|367|') = 0
and "cvs.operate" = (select name from permission where id=300)
