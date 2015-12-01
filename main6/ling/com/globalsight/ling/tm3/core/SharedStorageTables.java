package com.globalsight.ling.tm3.core;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;

import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.ling.tm3.core.persistence.SQLUtil;

class SharedStorageTables {

    private Connection conn;
    private long poolId;
    
    SharedStorageTables(Connection conn, long poolId) {
        this.conn = conn;
        this.poolId = poolId;
    }
    
    boolean exists() {
        try {
            SQLUtil.exec(conn, "DESCRIBE " + getTuTableName(poolId));
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }
    
    boolean create(Set<TM3Attribute> inlineAttributes) throws SQLException {
        if (exists()) {
            return false;
        }
        createTuStorage(inlineAttributes);
        createAttrTable();
        return true;
    }
    
    boolean destroy() throws SQLException {
        if (!exists()) {
            return false;
        }
        destroyFuzzyIndex();
        destroyAttrTable();
        destroyTuStorage();
        return true;
    }
    
    static String getTuTableName(long poolId) {
        return table(StorageInfo.TU_TABLE_NAME, poolId);
    }
    
    static String getTuvTableName(long poolId) {
        return table(StorageInfo.TUV_TABLE_NAME, poolId);
    }

    static String getTuvExtTableName(long poolId) {
        return table(StorageInfo.TUV_EXT_TABLE_NAME, poolId);
    }

    static String getAttrValTableName(long poolId) {
        return table(StorageInfo.ATTR_VAL_TABLE_NAME, poolId);
    }
    
    static String getFuzzyIndexTableName(long poolId, long tmId) {
        return getFuzzyIndexBaseName(poolId) + "_" + tmId;
    }

    private static String getFuzzyIndexBaseName(long poolId) {
        return table(StorageInfo.INDEX_TABLE_NAME, poolId);
    }
    
    private static String table(String base, long id) {
        return base + "_SHARED_" + id;
    }
    
    protected void createAttrTable() throws SQLException
    {
    	String attrValTable = getAttrValTableName(poolId);
    	if (!DbUtil.isTableExisted(conn, attrValTable))
    	{
            SQLUtil.exec(conn,
                    "CREATE TABLE " + attrValTable + " (" +
                    "tmId      bigint NOT NULL, " +
                    "tuId      bigint NOT NULL, " + 
                    "attrId    bigint NOT NULL, " + 
                    "value     varchar(" + StorageInfo.MAX_ATTR_VALUE_LEN + ") not null, " + 
                    "UNIQUE KEY(tuId, attrId), " +
                    "KEY (tmid, attrId), " + 
                    "FOREIGN KEY (tuId) REFERENCES " + getTuTableName(poolId) + 
                            " (id) ON DELETE CASCADE, " +
                    "FOREIGN KEY (attrId) REFERENCES TM3_ATTR (id) ON DELETE CASCADE " +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8"
            );
    	}
    }

    protected void createTuStorage(Set<TM3Attribute> inlineAttributes)
            throws SQLException
    {
    	StringBuilder stmt = null;

    	String tuTableName = getTuTableName(poolId);
		if (!DbUtil.isTableExisted(conn, tuTableName))
        {
            stmt = new StringBuilder(
                    "CREATE TABLE " + tuTableName + " (" +
                    "tmId bigint NOT NULL, " +
                    "id bigint NOT NULL, " +
                    "srcLocaleId bigint NOT NULL, ");
                for (TM3Attribute attr : inlineAttributes) {
                    stmt.append(attr.getColumnName() + " ")
                        .append(attr.getValueType().getSqlType())
                        .append(", ");
                }
                stmt.append("PRIMARY KEY (id)");
                stmt.append(") ENGINE=InnoDB");
                SQLUtil.exec(conn, stmt.toString());        	
       	}
        
        // Now create the TUV table.  Note the denormalized tmId
        // (to avoid an extra join during fuzzy lookup)
        String tuvTableName = getTuvTableName(poolId);
		if (!DbUtil.isTableExisted(conn, tuvTableName))
		{
	        SQLUtil.exec(conn,
	                "CREATE TABLE " + tuvTableName + " (" +
	                "id bigint NOT NULL, " +
	                "tuId bigint NOT NULL, " +
	                "tmId bigint NOT NULL, " +
	                "localeId bigint NOT NULL, " +
	                "fingerprint bigint NOT NULL, " +
	                "content mediumtext NOT NULL, " + 
	                "firstEventId bigint NOT NULL, " +
	                "lastEventId bigint NOT NULL, " +
	                "creationUser varchar(80) DEFAULT NULL, " +
	                "creationDate datetime NOT NULL, " +
	                "modifyUser varchar(80) DEFAULT NULL, " +
	                "modifyDate datetime NOT NULL, " +
	                "PRIMARY KEY (id), " +
	                "KEY (tmId, localeId, fingerprint), " +
	                "KEY (tuId, localeId), " + 
	                "FOREIGN KEY (tuId) REFERENCES " + tuTableName + " (id) ON DELETE CASCADE, " +
	                "FOREIGN KEY (firstEventID) REFERENCES TM3_EVENTS (id), " +
	                "FOREIGN KEY (lastEventID) REFERENCES TM3_EVENTS (id) " +
	                ") ENGINE=InnoDB DEFAULT CHARSET=utf8"
	            );

			// Create index on TUV table
			stmt = new StringBuilder();
			stmt.append("CREATE INDEX INDEX_").append(tuvTableName)
					.append("_TMID ON ").append(tuvTableName).append(" (tmId)");
			SQLUtil.exec(conn, stmt.toString());

			stmt = new StringBuilder();
			stmt.append("CREATE INDEX INDEX_LOCALE_ID ON ")
					.append(tuvTableName).append(" (localeId)");
			SQLUtil.exec(conn, stmt.toString());
		}

        // Create TUV extension table to store extra attributes
        String tuvExtTableName = getTuvExtTableName(poolId);
        if (!DbUtil.isTableExisted(conn, tuvExtTableName))
        {
			stmt = new StringBuilder().append("CREATE TABLE ")
					.append(tuvExtTableName).append(" (")
    				.append("tuvId BIGINT(20) NOT NULL, ")
    				.append("tuId BIGINT(20) NOT NULL, ")
    				.append("tmId BIGINT(20) NOT NULL, ")
    				.append("lastUsageDate DATETIME DEFAULT NULL, ")
    				.append("jobId BIGINT(20) DEFAULT -1, ")
    				.append("jobName VARCHAR(320) DEFAULT NULL, ")
    				.append("previousHash BIGINT(20) DEFAULT -1, ")
    				.append("nextHash BIGINT(20) DEFAULT -1, ")
    				.append("sid TEXT DEFAULT NULL, ")
    				.append("varchar1 VARCHAR(512), ")
    				.append("varchar2 VARCHAR(512), ")
    				.append("varchar3 VARCHAR(512), ")
    				.append("varchar4 VARCHAR(512), ")
    				.append("text1 TEXT, ")
    				.append("text2 TEXT, ")
    				.append("long1 BIGINT(20), ")
    				.append("long2 BIGINT(20), ")
    				.append("date1 DATETIME, ")
    				.append("date2 DATETIME, ")
    				.append("UNIQUE KEY tuvId (tuvId), ")
    				.append("KEY tuId (tuId), ")
    				.append("KEY tmId (tmId) ")
    				.append(") ENGINE=INNODB;");
    		SQLUtil.exec(conn, stmt.toString());
        }
    }

    protected void destroyAttrTable() throws SQLException {
        SQLUtil.exec(conn, "drop table if exists " + getAttrValTableName(poolId));
    }

    protected void destroyTuStorage() throws SQLException {
        SQLUtil.exec(conn, "drop table if exists " + getTuvExtTableName(poolId));
        SQLUtil.exec(conn, "drop table if exists " + getTuvTableName(poolId));
        SQLUtil.exec(conn, "drop table if exists " + getTuTableName(poolId));
    }

    protected void destroyFuzzyIndex() throws SQLException {
        // Because we have per-TM tables, this is suddenly complicated.
        // We need to drop everything
        SQLUtil.exec(conn, "SET @v = (SELECT CONCAT('drop table ', GROUP_CONCAT(a.table_name)) FROM information_schema.tables a where a.table_schema = DATABASE() AND a.table_name like '" + getFuzzyIndexBaseName(poolId) + "%')");
        SQLUtil.exec(conn, "SET @y = (SELECT IF (@v IS NOT NULL, @v, 'select 1'))");
        SQLUtil.exec(conn, "PREPARE s FROM @y");
        SQLUtil.exec(conn, "EXECUTE s");
    }

}
