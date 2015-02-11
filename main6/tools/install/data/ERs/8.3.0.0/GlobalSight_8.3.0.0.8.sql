# GBS-2794:stuck threads waiting for resource pool

# "calendar_holiday" table
ALTER TABLE calendar_holiday ADD PRIMARY KEY (calendar_id, holiday_id);

# "exportbatch_workflow" table
ALTER TABLE exportbatch_workflow ADD PRIMARY KEY (EXPORT_BATCH_ID, WORKFLOW_ID);

# "segmentation_rule_tm_profile" table
ALTER TABLE segmentation_rule_tm_profile ADD PRIMARY KEY (TM_PROFILE_ID, SEGMENTATION_RULE_ID);

# "workflow_request_wftemplate" table
ALTER TABLE workflow_request_wftemplate ADD PRIMARY KEY (WORKFLOW_REQUEST_ID, WORKFLOW_TEMPLATE_ID);

# "attribute_set_attribute" table
ALTER TABLE attribute_set_attribute ADD PRIMARY KEY (SET_ID, ATTRIBUTE_ID);

# "job_attribute_select_option" table
ALTER TABLE job_attribute_select_option ADD PRIMARY KEY (JOB_ATTRIBUTE_ID, SELECT_OPTION_ID);

DROP INDEX idx_calendar_holiday_cid_hid ON calendar_holiday;