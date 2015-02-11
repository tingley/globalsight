# For GBS-1376: Remove Some Useless Extensions, By Joey on 2010/08/12

DELETE FROM extension 
where id not in (select extension_id from file_profile_extension)
and name in ('ai', 'asp', 'cfm', 'cfml', 'cpp', 'css', 'fm', 'jhtml', 'pdf', 'qxd', 'shtml');
