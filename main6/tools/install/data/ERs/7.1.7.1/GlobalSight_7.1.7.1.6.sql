# For gbs-799
insert into Text_CONDITION(LENGTH) values(null);
set @LIST_CONDITION_ID = LAST_INSERT_ID();
insert into ATTRIBUTE(name, display_name,description,company_id,visible,editable,required,condition_id,type)
values('protect_cost_center', 'cost center', 'The cost center', 1, 'Y','Y','N',@LIST_CONDITION_ID,'text');