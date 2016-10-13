# For gbs-942
insert into LIST_CONDITION(MULTIPLE) values('n');
set @LIST_CONDITION_ID = LAST_INSERT_ID();
insert into SELECT_OPTION(VALUE, LIST_CONDITION_ID) values('not set',@LIST_CONDITION_ID);
insert into SELECT_OPTION(VALUE, LIST_CONDITION_ID) values('yes',@LIST_CONDITION_ID);
insert into SELECT_OPTION(VALUE, LIST_CONDITION_ID) values('no',@LIST_CONDITION_ID);
insert into ATTRIBUTE(name, display_name,description,company_id,visible,editable,required,condition_id,type)
values('protect_international_cost_center', 'international \(non-US\) cost center', 'submitter\'s cost center is an international \(non-US\) cost center or not', 1, 'Y','Y','Y',@LIST_CONDITION_ID,'choice list');