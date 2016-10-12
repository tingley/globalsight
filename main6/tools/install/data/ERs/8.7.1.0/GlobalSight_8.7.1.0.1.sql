## GBS-4436:Workflow Notifications & Listener URL Update (Job Status)

ALTER TABLE workflow_state_posts ADD COLUMN POST_JOB_CHANGE CHAR(1) DEFAULT 'N';
