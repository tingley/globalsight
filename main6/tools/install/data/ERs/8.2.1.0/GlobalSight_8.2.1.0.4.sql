# Correct TU rep flag that are incorrect since 8.2
update translation_unit set is_repeated = 'N' where repetition_of_id > 0 and is_repeated = 'Y';
