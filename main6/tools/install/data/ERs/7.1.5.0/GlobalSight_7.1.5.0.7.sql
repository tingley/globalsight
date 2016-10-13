-- for GBS-607
update job set leverage_option = "default" 
where (leverage_option = 'in-context') 
and (id in (select job_id from request where data_source_id in
 (select id from file_profile where
 (is_sid_supported = 'N') 
or 
(xml_rule_id > 0 and (known_format_type_id = (select id from known_format_type where name='XML'))
)))) and (id in 
(select job_id from request where l10n_profile_id in 
(select l10n_profile_id from l10n_profile_tm_profile where tm_profile_id in 
(select id from tm_profile where is_context_match='N' and is_exact_match_leveraging='N'))));
 
update job set leverage_option = "exact" 
where (leverage_option = 'in-context') 
and (id in (select job_id from request where data_source_id in
 (select id from file_profile where
 (is_sid_supported = 'N') 
or 
(xml_rule_id > 0 and (known_format_type_id = 
(select id from known_format_type where name='XML'))
)))) and (id in 
(select job_id from request where l10n_profile_id in 
(select l10n_profile_id from l10n_profile_tm_profile where tm_profile_id in 
(select id from tm_profile where is_exact_match_leveraging='Y'))));
commit;