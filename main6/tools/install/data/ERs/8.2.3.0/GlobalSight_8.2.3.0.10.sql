# GBS-2351 Automatic archive exported jobs, Vincent Yan
# This SQL is used to change job.timestamp with the latest date of exported workflow which is in the job
UPDATE job j SET timestamp=(SELECT export_date FROM workflow w WHERE w.JOB_ID=j.ID ORDER BY export_date DESC LIMIT 0,1) WHERE j.state='EXPORTED';
