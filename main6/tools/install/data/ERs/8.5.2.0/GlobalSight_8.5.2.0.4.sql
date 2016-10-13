## GBS-3315: Make "Target" Reverse Indexing Optional During TM Import and Job/Workflow Export.
ALTER TABLE project_tm ADD COLUMN INDEX_TARGET CHAR(1) DEFAULT 'N';
