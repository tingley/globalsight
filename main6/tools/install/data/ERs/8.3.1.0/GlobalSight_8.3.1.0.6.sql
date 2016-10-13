# GBS-2817: Users from super company should not be able to change e-mail address from 'My Account'.
# '5' is the fixed permission ID for super company 'LocalizationParticipant' group.
UPDATE permissiongroup
SET permission_set = CONCAT(permission_set, '387|')
WHERE id != 5
AND permission_set NOT LIKE '%|387|%'