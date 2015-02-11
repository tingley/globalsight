update known_format_type
set PRE_EXTRACT_EVENT = 'ADOBE_IMPORTED_EVENT'
where NAME = 'INDD';

insert into known_format_type
select max(id) + 1, 'AI',
'Illustrator AI','ai',
'ADOBE_IMPORTED_EVENT',
'XML_LOCALIZED_EVENT'
from known_format_type;

commit;