create table segmentation_rule
(
  id integer
	constraint sr_pk primary key,
  name varchar2(40)
	constraint sr_name_nn not null,
  company_id integer
    constraint sr_company_id_nn not null
    constraint sr_company_id_fk references company,
  sr_type number(4) default 0
	constraint sr_type_nn not null,
  description varchar2(4000),
  rule_text clob,
  is_active char(1)
   constraint sr_active_ck check(is_active in ('Y', 'N'))
   constraint sr_active_nn not null
);

create table segmentation_rule_tm_profile
(
 segmentation_rule_id integer
   constraint srtm_sr_id_fk references segmentation_rule (id),
 tm_profile_id integer
    constraint srtm_tm_profile_id_fk references tm_profile (id)
);

commit;