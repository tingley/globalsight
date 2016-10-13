# [GBS-2898] <=> add globalsight instance id to off-line files
# 1) Some Old server, the ID = 177 already exist, so i will care this condition.
# 2) This sql just sets the ID, the value will be modified later(ServerUtil.getServerInstanceID()).
INSERT INTO SYSTEM_PARAMETER (ID, NAME, VALUE, COMPANY_ID)
 SELECT 177, 'server.instance.id', 'GlobalSightInstanceID', '1'
        FROM dual
        WHERE NOT EXISTS (SELECT * FROM SYSTEM_PARAMETER WHERE ID = 177);

INSERT INTO SYSTEM_PARAMETER (NAME, VALUE, COMPANY_ID)
 SELECT 'server.instance.id', 'GlobalSightInstanceID', '1'
        FROM dual
        WHERE NOT EXISTS (SELECT * FROM SYSTEM_PARAMETER WHERE NAME = 'server.instance.id');
