# For GBS-845 by Walter
alter table tb_concept drop PRIMARY KEY;

alter table tb_concept add PRIMARY KEY(CID);

alter table tb_concept change CID CID int(20) auto_increment;

alter table tb_language drop PRIMARY KEY;

alter table tb_language add PRIMARY KEY(LID);

alter table tb_language change LID LID int(20) auto_increment;

ALTER TABLE tb_language DROP KEY IDX_LANGUAGE_TBID_CID_LID;

ALTER TABLE tb_language modify TBID int(20);

ALTER TABLE tb_language modify CID int(20);

alter table tb_term drop PRIMARY KEY;

alter table tb_term add PRIMARY KEY(TID);

alter table tb_term change TID TID int(20) auto_increment;

ALTER TABLE tb_term DROP KEY IDX_TERM_TBID_CID;

ALTER TABLE tb_term modify TBID int(20);

ALTER TABLE tb_term modify CID int(20);

ALTER TABLE tb_term modify LID int(20);

alter table tb_user_data drop PRIMARY KEY;
  
alter table tb_user_data add ID int(20) not null AUTO_INCREMENT first,ADD primary key (ID);