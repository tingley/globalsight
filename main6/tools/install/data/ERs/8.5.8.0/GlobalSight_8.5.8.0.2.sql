# GBS-3671 McAfee SaaS: Character Escape Handler

update filter_configuration set 
NAME = 'Base Text Filter', 
KNOWN_FORMAT_ID='|0|', 
FILTER_DESCRIPTION='The filter to handle extracted text.'
where FILTER_TABLE_NAME = 'base_filter';