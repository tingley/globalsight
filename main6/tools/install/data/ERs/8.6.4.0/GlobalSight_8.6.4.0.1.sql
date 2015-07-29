# GBS-3564: Multiple Choice (Choice List) attribute values not all written back to TM.
RENAME TABLE text_condition TO attribute_condition_text;
RENAME TABLE int_condition TO attribute_condition_int;
RENAME TABLE float_condition TO attribute_condition_float;
RENAME TABLE file_condition TO attribute_condition_file;
RENAME TABLE date_condition TO attribute_condition_date;
RENAME TABLE list_condition TO attribute_condition_list;
