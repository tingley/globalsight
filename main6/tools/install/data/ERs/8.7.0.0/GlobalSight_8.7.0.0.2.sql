## GBS-4309 Activity Comment Attachment Upload Check

ALTER TABLE TASK_INFO ADD COLUMN IS_ACTIVITY_COMMENT_UPLOADED INTEGER DEFAULT 0;
ALTER TABLE TASK_INFO ADD COLUMN IS_ACTIVITY_COMMENT_UPLOAD_CHECK INTEGER DEFAULT 0;