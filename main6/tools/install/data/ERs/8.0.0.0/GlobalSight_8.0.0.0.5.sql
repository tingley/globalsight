CREATE TABLE TM_TB_USERS
(
 TM_TB_ID BIGINT NOT NULL,
 USER_ID VARCHAR(80) NOT NULL,
 T_TYPE  CHAR(2) NOT NULL,
 PRIMARY KEY(TM_TB_ID, USER_ID, T_TYPE)
);

insert into TM_TB_USERS (TM_TB_ID, USER_ID, T_TYPE) 
(
 select distinct ptm.id, pu.user_id, "TM" from 
 project_tm ptm, permissiongroup_user pu, permissiongroup pg 
 where pg.company_id = ptm.company_id 
 and pu.permissiongroup_id = pg.id 
 and pg.name not in ("Administrator", "SuperAdministrator")
);

insert into TM_TB_USERS (TM_TB_ID, USER_ID, T_TYPE) 
(
 select distinct ptm.id, pu.user_id, "TM" from 
 project_tm ptm, permissiongroup_user pu, permissiongroup pg 
 where pg.company_id != ptm.company_id 
 and pg.company_id = 1 
 and pu.permissiongroup_id = pg.id 
 and pg.name != "SuperAdministrator"
);

insert into TM_TB_USERS (TM_TB_ID, USER_ID, T_TYPE) 
(
 select distinct tt.tbid, pu.user_id, "TB" from 
 tb_termbase tt, permissiongroup_user pu, permissiongroup pg  
 where pg.company_id = tt.companyid 
 and pu.permissiongroup_id = pg.id 
 and pg.name not in ("Administrator", "SuperAdministrator")
);

insert into TM_TB_USERS (TM_TB_ID, USER_ID, T_TYPE) 
(
 select distinct tt.tbid, pu.user_id, "TB" from 
 tb_termbase tt, permissiongroup_user pu, permissiongroup pg  
 where pg.company_id != tt.companyid 
 and pg.company_id = 1
 and pu.permissiongroup_id = pg.id 
 and pg.name != "SuperAdministrator"
);