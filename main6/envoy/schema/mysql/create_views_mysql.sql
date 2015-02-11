DROP VIEW IF EXISTS view_task_duration;
DROP VIEW IF EXISTS VIEW_WORKFLOW_LEVEL;

create view view_task_duration as
	
	select 
		distinct(j.id) as jobId,
		concat(l2.iso_lang_code,'_',l2.iso_country_code) as source_locale,
		concat(l1.iso_lang_code,'_',l1.iso_country_code) as target_locale,
		DATEDIFF( ti.completed_date , ti.accepted_date) as duration,
        j.company_id as company_id
	from 
		job j , request r , l10n_profile l10n, 
	       workflow w, task_info ti ,locale l1 , locale l2
	where
		j.id = w.job_id and
		r.job_id = j.id and
		r.l10n_profile_id = l10n.id and
		w.iflow_instance_id = ti.workflow_id and
		w.target_locale_id = l1.id and
		l10n.source_locale_id = l2.id and
		j.create_date between DATE_SUB(now(), INTERVAL 31 DAY) and now() AND
   		j.state in ('EXPORTED', 'LOCALIZED' );


CREATE VIEW VIEW_WORKFLOW_LEVEL AS
	SELECT   
	    distinct(p.project_name) as project_name,
		j.id as job_id, 
		p.manager_user_id as project_manager ,
   		j.name as job_name ,
   		(Select concat(iso_lang_code,'_',iso_country_code) from locale where id = l10n.source_locale_id ) as source_locale,
   		(Select concat(iso_lang_code,'_',iso_country_code) from locale where id = w.target_locale_id ) as target_locale,
  		w.dispatch_date as start_date  ,
   		w.completed_date as actual_end ,
   		c.estimated_cost as estimated_cost,
   		c.actual_cost as actual_cost,
   		c.final_cost as final_cost,
   		c.override_cost as override_cost,
        j.company_id as company_id
		
	from  
		project p,
   		l10n_profile l10n ,
   		request r,
   		job j ,
   		workflow w ,
   		cost c
   	where
   		p.project_seq = l10n.project_id and
   		r.l10n_profile_id = l10n.id and
   		r.job_id = j.id and
   		w.job_id = j.id and
   		c.costable_object_type = 'W' and
   		c.costable_object_id = w.iflow_instance_id and
   		c.rate_type ='E' and
   		j.state in ('DISPATCHED','EXPORTED', 'LOCALIZED', 'ARCHIVED');