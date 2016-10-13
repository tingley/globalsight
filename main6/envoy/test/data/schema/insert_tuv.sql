begin
--DELETE from task_tuv where ID = '9998';
--DELETE from task_tuv where ID = '9997';
--DELETE from TRANSLATION_UNIT_VARIANT where ID = '9999';
--DELETE from TRANSLATION_UNIT_VARIANT where ID = '9998';
--DELETE from TRANSLATION_UNIT_VARIANT where ID = '9997';
--DELETE from TRANSLATION_UNIT where ID = '9999';
--DELETE from LEVERAGE_GROUP where ID = '9999';
--delete from task_info where task_id ='1001';
--delete from task_info where task_id ='1002';
--delete from file_profile where id ='1';
--delete from known_format_type where id ='1';
--delete from request where id ='1';
--delete from workflow where iflow_instance_id ='1';
--delete from workflow where iflow_instance_id ='2';
--delete from workflow where iflow_instance_id ='3';
--delete from workflow where iflow_instance_id ='2000';
--delete from job where id ='1';
--delete from task_assignment where l10n_profile_id ='1';
--delete from l10n_profile_workflow_info where l10n_profile_id ='1';
--delete from l10n_profile where id ='1';
--DELETE from TRANSLATION_MEMORY where ID = '9999';
--DELETE from TRANSLATION_MEMORY where ID = '1';
--DELETE from PROJECT where PROJECT_SEQ = '9999';
--DELETE from PROJECT where PROJECT_SEQ = '1';

delete from file_profile_extension where file_profile_id like '%';
delete from extension where ID like '%';
delete from file_profile where ID like '%';


DELETE from task_tuv where ID like '%';
DELETE from TRANSLATION_UNIT_VARIANT where ID like '%';
DELETE from TRANSLATION_UNIT where ID like '%';
delete from target_page_leverage_group where lg_id like '%';
delete from source_page_leverage_group where lg_id like '%';
DELETE from LEVERAGE_GROUP where ID like '%';
delete from target_page where id like '%';
delete from source_page where id like '%';
delete from task_info where task_id like '%';
delete from file_profile where id like '%';
delete from known_format_type where id like '%';
delete from request where id like '%';
delete from workflow where iflow_instance_id like '%';
delete from job where id like '%';
delete from task_assignment where l10n_profile_id like '%';
delete from l10n_profile_workflow_info where l10n_profile_id like '%';
delete from l10n_profile where id like '%';
DELETE from TRANSLATION_MEMORY where ID like '%';
DELETE from PROJECT where PROJECT_SEQ like '%';


-- Project(id, name, desc, managerId)
insert into project values (1,'myProject','some desc','tdoomany');
-- TM(id, name, projectId)
insert into translation_memory values(1,'someTM',1);
-- L10nProfile(id, name, desc, srcLocaleId, projectId,... don't care)
insert into l10n_profile (id, name, description, source_locale_id, project_id, is_auto_dispatch, timestamp, dispatch_condition, dispatch_word_count, dispatch_interval, dispatch_time_unit, dispatch_absolute_days, dispatch_timer_type, dispatch_start_time, is_script_run_at_job_creation, job_creation_script_name, tm_choice, is_exact_match_edit, job_exclude_tu_types) values (1,'L10nProfile1','desc',32,1,'Y',sysdate, 0, 13, 0, 1,'12','R',null,'N',null,0, 'Y', null);
-- l10n_profile_workflow_info(l10n_profile_id, target_locale_id, 
--        char_set, workflow_template_id)
insert into l10n_profile_workflow_info values(1,57,'UTF-8',515);
-- task_assignment (task_order, l10n_profile_id, target_locale_id, 
--                  task_assignment)
insert into task_assignment values(1,1,57,'lronning');
-- job(id, name, state, word_count, create_date, is_wordcount_reached, 
--     timestamp)


insert into known_format_type values(1,'HTML','html file','HTML',null,null);
insert into file_profile (id, name, description, known_format_type_id, code_set, xml_rule_id, l10n_profile_id, timestamp) values(1,'HTML FileProfile','',1, 'ISO-8859-1',null,1,sysdate);
insert into extension values(1,'html');
insert into extension values(2,'htm');

insert into file_profile_extension values(1, 1);
insert into file_profile_extension values(1, 2);

insert into job values(1,'TomyD Job','PENDING',sysdate,'Y',sysdate);
-- workflow(id,state,targetLocaleId,jobId,
--          dispatchDate,CompleteDate,timestamp)
-- de_DE, es_ES, fr_FR
insert into workflow values(1,'PENDING',25,1,sysdate,sysdate, '10', '4', sysdate);
insert into workflow values(2,'PENDING',41,1,sysdate,sysdate, '10', '4', sysdate);
insert into workflow values(3,'DISPATCHED',57,1,sysdate,sysdate, '10', '4', sysdate);
insert into workflow values(2000,'DISPATCHED',57,1,sysdate,sysdate, '10', '4', sysdate);
-- request(id, l10nProfId,type,eventflowXml,jobPrefiName,exceptionXml,
--         JobId,PageId,dsId,timestamp)
--insert into request (id, l10n_profile_id, type, event_flow_xml, exception_xml, job_id, page_id, data_source_id, batch_id, batch_page_count, batch_page_number, batch_job_name, timestamp) values (1, 1,'LOCALIZATION_REQUEST', 'eventFlowXml1',null, 1, 1, 1, 0, 0, 0, 'batchJobName1', sysdate);
-- From Lori's tests



INSERT INTO PROJECT (PROJECT_SEQ, PROJECT_NAME, DESCRIPTION, MANAGER_USER_ID) VALUES (9999, 'dummy', 'a dummy project', NULL);
INSERT INTO TRANSLATION_MEMORY (ID, NAME, PROJECT_ID) VALUES (9999, 'dummy', 9999);

INSERT INTO LEVERAGE_GROUP (ID) VALUES (9999);
INSERT INTO LEVERAGE_GROUP (ID) VALUES (9998);
INSERT INTO LEVERAGE_GROUP (ID) VALUES (9997);
INSERT INTO LEVERAGE_GROUP (ID) VALUES (999);
INSERT INTO LEVERAGE_GROUP (ID) VALUES (998);
INSERT INTO LEVERAGE_GROUP (ID) VALUES (997);

INSERT INTO TRANSLATION_UNIT (ID, TM_ID, DATA_TYPE, TU_TYPE, LOCALIZE_TYPE, LEVERAGE_GROUP_ID) VALUES (99999, 9999, 'html', 'text', 'L', 9999);
INSERT INTO TRANSLATION_UNIT (ID, TM_ID, DATA_TYPE, TU_TYPE, LOCALIZE_TYPE, LEVERAGE_GROUP_ID) VALUES (99998, 9999, 'html', 'text', 'L', 9999);
INSERT INTO TRANSLATION_UNIT (ID, TM_ID, DATA_TYPE, TU_TYPE, LOCALIZE_TYPE, LEVERAGE_GROUP_ID) VALUES (9999, 9999, 'html', 'text', 'L', 9999);
INSERT INTO TRANSLATION_UNIT (ID, TM_ID, DATA_TYPE, TU_TYPE, LOCALIZE_TYPE, LEVERAGE_GROUP_ID) VALUES (8888, 9999, 'html', 'text', 'L', 9999);
INSERT INTO TRANSLATION_UNIT (ID, TM_ID, DATA_TYPE, TU_TYPE, LOCALIZE_TYPE, LEVERAGE_GROUP_ID) VALUES (9998, 9999, 'html', 'text', 'L', 9998);
INSERT INTO TRANSLATION_UNIT (ID, TM_ID, DATA_TYPE, TU_TYPE, LOCALIZE_TYPE, LEVERAGE_GROUP_ID) VALUES (9997, 9999, 'html', 'text', 'L', 9997);
INSERT INTO TRANSLATION_UNIT (ID, TM_ID, DATA_TYPE, TU_TYPE, LOCALIZE_TYPE, LEVERAGE_GROUP_ID) VALUES (999, 9999, 'html', 'text', 'L', 999);
INSERT INTO TRANSLATION_UNIT (ID, TM_ID, DATA_TYPE, TU_TYPE, LOCALIZE_TYPE, LEVERAGE_GROUP_ID) VALUES (888, 9999, 'html', 'text', 'L', 999);
INSERT INTO TRANSLATION_UNIT (ID, TM_ID, DATA_TYPE, TU_TYPE, LOCALIZE_TYPE, LEVERAGE_GROUP_ID) VALUES (998, 9999, 'html', 'text', 'L', 998);
INSERT INTO TRANSLATION_UNIT (ID, TM_ID, DATA_TYPE, TU_TYPE, LOCALIZE_TYPE, LEVERAGE_GROUP_ID) VALUES (997, 9999, 'html', 'text', 'L', 997);

INSERT INTO TRANSLATION_UNIT_VARIANT (ID, EXACT_MATCH_KEY, SEGMENT, WORD_COUNT, STATE, LOCALE_ID, TU_ID, TIMESTAMP) VALUES (99999, 0, '<segment>a seg 99999</segment>', 2, 'NOT_LOCALIZED', 32, 99999, sysdate);
INSERT INTO TRANSLATION_UNIT_VARIANT (ID, EXACT_MATCH_KEY, SEGMENT, WORD_COUNT, STATE, LOCALE_ID, TU_ID, TIMESTAMP) VALUES (99998, 0, '<segment>a seg 99998</segment>', 2, 'LOCALIZED', 32, 99998, sysdate);
INSERT INTO TRANSLATION_UNIT_VARIANT (ID, EXACT_MATCH_KEY, SEGMENT, WORD_COUNT, STATE, LOCALE_ID, TU_ID, TIMESTAMP) VALUES (99997, 0, '<segment>a seg 99997</segment>', 2, 'NOT_LOCALIZED', 57, 99999, sysdate);
INSERT INTO TRANSLATION_UNIT_VARIANT (ID, EXACT_MATCH_KEY, SEGMENT, WORD_COUNT, STATE, LOCALE_ID, TU_ID, TIMESTAMP) VALUES (99996, 0, '<segment>a seg 99996</segment>', 2, 'LOCALIZED', 57, 99998, sysdate);
INSERT INTO TRANSLATION_UNIT_VARIANT (ID, EXACT_MATCH_KEY, SEGMENT, WORD_COUNT, STATE, LOCALE_ID, TU_ID, TIMESTAMP) VALUES (9999, 0, '<segment>a seg 9999</segment>', 2, 'NOT_LOCALIZED', 32, 9999, sysdate);
INSERT INTO TRANSLATION_UNIT_VARIANT (ID, EXACT_MATCH_KEY, SEGMENT, WORD_COUNT, STATE, LOCALE_ID, TU_ID, TIMESTAMP) VALUES (8888, 0, '<segment>a seg 8888</segment>', 2, 'NOT_LOCALIZED', 32, 8888, sysdate);

INSERT INTO TRANSLATION_UNIT_VARIANT (ID, EXACT_MATCH_KEY, SEGMENT, WORD_COUNT, STATE, LOCALE_ID, TU_ID, TIMESTAMP) VALUES (98, 0, '<segment>another seg 98</segment>', 2, 'LOCALIZED', 32, 9998, sysdate);

INSERT INTO TRANSLATION_UNIT_VARIANT (ID, EXACT_MATCH_KEY, SEGMENT, WORD_COUNT, STATE, LOCALE_ID, TU_ID, TIMESTAMP) VALUES (97, 0, '<segment>another seg 97</segment>', 2, 'NOT_LOCALIZED', 32, 9997, sysdate);


INSERT INTO TRANSLATION_UNIT_VARIANT (ID, EXACT_MATCH_KEY, SEGMENT, WORD_COUNT, STATE, LOCALE_ID, TU_ID, TIMESTAMP) VALUES (999, 0, '<segment>a seg 999</segment>', 2, 'NOT_LOCALIZED', 57, 999, sysdate);
INSERT INTO TRANSLATION_UNIT_VARIANT (ID, EXACT_MATCH_KEY, SEGMENT, WORD_COUNT, STATE, LOCALE_ID, TU_ID, TIMESTAMP) VALUES (998, 0, '<segment>another seg 9998</segment>', 2, 'OUT_OF_DATE', 57, 999, sysdate);
INSERT INTO TRANSLATION_UNIT_VARIANT (ID, EXACT_MATCH_KEY, SEGMENT, WORD_COUNT, STATE, LOCALE_ID, TU_ID, TIMESTAMP) VALUES (997, 0, '<segment>another seg 9997</segment>', 2, 'OUT_OF_DATE', 57, 999, sysdate);
INSERT INTO TRANSLATION_UNIT_VARIANT (ID, EXACT_MATCH_KEY, SEGMENT, WORD_COUNT, STATE, LOCALE_ID, TU_ID, TIMESTAMP) VALUES (888, 0, '<segment>a seg 888</segment>', 2, 'NOT_LOCALIZED', 57, 888, sysdate);

INSERT INTO TRANSLATION_UNIT_VARIANT (ID, EXACT_MATCH_KEY, SEGMENT, WORD_COUNT, STATE, LOCALE_ID, TU_ID, TIMESTAMP) VALUES (18, 0, '<segment>another seg 8</segment>', 2, 'NOT_LOCALIZED', 57, 998, sysdate);

INSERT INTO TRANSLATION_UNIT_VARIANT (ID, EXACT_MATCH_KEY, SEGMENT, WORD_COUNT, STATE, LOCALE_ID, TU_ID, TIMESTAMP) VALUES (17, 0, '<segment>another seg 7</segment>', 2, 'NOT_LOCALIZED', 57, 997, sysdate);



INSERT INTO TASK_INFO (task_id, workflow_id, accepted_date, completed_date) VALUES (1001, 2000, sysdate, sysdate);
INSERT INTO TASK_INFO (task_id, workflow_id, accepted_date, completed_date) VALUES (1002, 2000, sysdate, sysdate);

INSERT INTO TASK_TUV (ID, current_tuv_id, task_id, version, previous_tuv_id) VALUES (998, 9999, 1001, 1, 998);
INSERT INTO TASK_TUV (ID, current_tuv_id, task_id, version, previous_tuv_id) VALUES (997, 9999, 1002, 2, 997);

INSERT INTO SOURCE_PAGE (ID, external_page_id, word_count, original_encoding, state, data_source_type, data_type, timestamp) VALUES (9999, 'externalPageId9999', 123, 'originalEncoding1', 'EXPORTED', 'data_source_type1', 'html', sysdate);
INSERT INTO SOURCE_PAGE (ID, external_page_id, word_count, original_encoding, state, data_source_type, data_type, previous_page_id, timestamp) VALUES (9998, 'externalPageId9999', 123, 'originalEncoding1', 'LOCALIZED', 'data_source_type1', 'html', 9999, sysdate);

INSERT INTO SOURCE_PAGE (ID, external_page_id, word_count, original_encoding, state, data_source_type, data_type, timestamp) VALUES (9997, 'externalPageId9997', 123, 'originalEncoding1', 'NOT_LOCALIZED', 'data_source_type1', 'html', sysdate);


insert into REQUEST (ID, l10n_profile_id, type, event_flow_xml, exception_xml, job_id, page_id, data_source_id, is_page_cxe_previewable, batch_id, batch_page_count, batch_page_number, batch_job_name, base_href, timestamp) VALUES (9999, 1, 'LOCALIZATION_REQUEST', null, null, 1, 9999, 1, 'N', 9999, 1, 1, 'batchjob9999', null, sysdate);       

INSERT INTO TARGET_PAGE (ID, state, source_page_id, workflow_iflow_instance_id, timestamp) VALUES (9999, 'EXPORTED', 9999, 1, sysdate);
INSERT INTO TARGET_PAGE (ID, state, source_page_id, workflow_iflow_instance_id, timestamp) VALUES (9998, 'LOCALIZED', 9998, 2, sysdate);
INSERT INTO TARGET_PAGE (ID, state, source_page_id, workflow_iflow_instance_id, timestamp) VALUES (9997, 'NOT_LOCALIZED', 9997, 2000, sysdate);

INSERT INTO SOURCE_PAGE_LEVERAGE_GROUP (lg_id, sp_id) VALUES (9999, 9999);
INSERT INTO SOURCE_PAGE_LEVERAGE_GROUP (lg_id, sp_id) VALUES (9998, 9998);
INSERT INTO SOURCE_PAGE_LEVERAGE_GROUP (lg_id, sp_id) VALUES (9997, 9997);
INSERT INTO SOURCE_PAGE_LEVERAGE_GROUP (lg_id, sp_id) VALUES (999, 9999);
INSERT INTO SOURCE_PAGE_LEVERAGE_GROUP (lg_id, sp_id) VALUES (998, 9998);
INSERT INTO SOURCE_PAGE_LEVERAGE_GROUP (lg_id, sp_id) VALUES (997, 9997);

INSERT INTO TARGET_PAGE_LEVERAGE_GROUP (lg_id, tp_id) VALUES (9999, 9999);
INSERT INTO TARGET_PAGE_LEVERAGE_GROUP (lg_id, tp_id) VALUES (9998, 9998);
INSERT INTO TARGET_PAGE_LEVERAGE_GROUP (lg_id, tp_id) VALUES (9997, 9997);
INSERT INTO TARGET_PAGE_LEVERAGE_GROUP (lg_id, tp_id) VALUES (999, 9999);
INSERT INTO TARGET_PAGE_LEVERAGE_GROUP (lg_id, tp_id) VALUES (998, 9998);
INSERT INTO TARGET_PAGE_LEVERAGE_GROUP (lg_id, tp_id) VALUES (997, 9997);

commit;

end;
/
