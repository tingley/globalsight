#GBS-4861 add MsHub authentication URL field
ALTER TABLE mt_profile ADD COLUMN MS_TOKEN_URL VARCHAR(100) DEFAULT 'https://api.cognitive.microsoft.com/sts/v1.0/issueToken';
ALTER TABLE mt_profile ADD COLUMN MS_TRANS_VERSION VARCHAR(10);