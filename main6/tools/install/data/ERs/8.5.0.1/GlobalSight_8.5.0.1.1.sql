## GBS-3096: Initial Download Options in My Account does not work properly.

## Originally 0 means "Allow Edit Locked Segments=NO", now 4 means the same.
UPDATE user_parameter
SET VALUE = '1'
WHERE id IN (
	SELECT b.id FROM 
		(SELECT id, user_id, NAME, VALUE FROM user_parameter WHERE NAME = 'editExact' AND VALUE = 'yes') AS a,
		(SELECT id, user_id, NAME, VALUE FROM user_parameter WHERE NAME = 'TMEditType' AND VALUE = 0) AS b
		WHERE a.user_id = b.user_id
);

UPDATE user_parameter
SET VALUE = '4'
WHERE id IN (
	SELECT b.id FROM 
		(SELECT id, user_id, NAME, VALUE FROM user_parameter WHERE NAME = 'editExact' AND VALUE = 'no') AS a,
		(SELECT id, user_id, NAME, VALUE FROM user_parameter WHERE NAME = 'TMEditType' AND VALUE = 0) AS b
		WHERE a.user_id = b.user_id
);

## update 'rtfTrados' and 'text' to 'rtfTradosOptimized' as rtfTrados and text format have been removed since GBS-2845.
UPDATE user_parameter
SET VALUE = 'rtfTradosOptimized'
WHERE NAME = 'format'
AND VALUE in ('rtfTrados', 'text');
