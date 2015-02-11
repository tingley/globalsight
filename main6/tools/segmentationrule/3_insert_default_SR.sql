---- default segmentation rule

delete from segmentation_rule where ID = 1;

insert into segmentation_rule ("ID", "NAME", "COMPANY_ID", "SR_TYPE", "DESCRIPTION", "RULE_TEXT", "IS_ACTIVE") 
values (1, 'default', 1, 0, 'Default segmentation rule.', 
'default', 
'Y');

commit;