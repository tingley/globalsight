package com.globalsight.ling.tm3.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.globalsight.ling.tm3.core.persistence.DistributedId;
import com.globalsight.ling.tm3.core.persistence.SQLUtil;
import com.globalsight.ling.tm3.core.persistence.StatementBuilder;

class MultilingualSharedStorageInfo<T extends TM3Data> extends StorageInfo<T> {
    
    private long sharedStorageId;
    
    protected MultilingualSharedStorageInfo(MultilingualSharedTm<T> tm) {
        super(tm, TM3TmType.MULTILINGUAL_SHARED);
        this.sharedStorageId = tm.getSharedStorageId();
    }
    
    /**
     * Initialize the tablespace defined by this object.  For shared
     * storage TMs, this is actually a no-op with respect to tables.
     * 
     * @param conn JDBC connection
     * @throws SQLException
     */
    @Override
    public void create() throws SQLException {
        initializeTm();
        // Shared TMs still have a dedicated fuzzy index
        createFuzzyIndex(getSession().connection());
    }
    
    /**
     * Remove the data for this TM.  This cleans up the dedicated
     * fuzzy index table and removes TM-specific data from the shared
     * tables.
     * 
     * @param conn JDBC connection
     * @throws SQLException
     */
    @Override
    public void destroy() throws SQLException {
        Connection conn = getSession().connection();
        
        destroyFuzzyIndex(conn);
        // Although we could just cascade everything from the tu table,
        // it's faster to use the index on tmId to delete in bulk from each 
        // table
        SQLUtil.exec(conn, new StatementBuilder()
            .append("delete from ").append(getAttrValTableName())
            .append(" where tmId = ?").addValue(getTm().getId())
        );
        SQLUtil.exec(conn, new StatementBuilder()
            .append("delete from ").append(getTuvTableName())
            .append(" where tmId = ?").addValue(getTm().getId())
        );
        SQLUtil.exec(conn, new StatementBuilder()
            .append("delete from ").append(getTuTableName())
            .append(" where tmId = ?").addValue(getTm().getId())
        );
    }
   
    @Override
    FuzzyIndex<T> getFuzzyIndex() {
        return new MultilingualFuzzyIndex<T>(this);
    }

    @Override
    TuStorage<T> getTuStorage() {
        return new SharedTuStorage<T>(this);
    }

    @Override
    protected void initializeTm() {
        BaseTm<T> tm = getTm();
        Long id = tm.getId();
        if (id == null) {
            throw new IllegalStateException("No id -- TM must be persisted before initialization");
        }
        tm.setTuTableName(SharedStorageTables.getTuTableName(sharedStorageId));
        tm.setTuvTableName(SharedStorageTables.getTuvTableName(sharedStorageId));
        tm.setFuzzyIndexTableName(
            SharedStorageTables.getFuzzyIndexTableName(sharedStorageId, id));
        tm.setAttrValTableName(SharedStorageTables.getAttrValTableName(sharedStorageId));
    }

    
    //
    // Standard creation and deletion routines do nothing.  All table 
    // manipulation is handled at the pool level.  The exception to
    // this is the fuzzy index, which still exists per-tm.
    //
    
    @Override
    protected void createAttrTable(Connection conn) throws SQLException {
    }

    @Override
    protected void createFuzzyIndex(Connection conn) throws SQLException {
        SQLUtil.exec(conn,
            "CREATE TABLE " + getFuzzyIndexTableName() + " (" +
            "fingerprint    bigint    NOT NULL, " +  
            "tuvId      bigint     NOT NULL, " +
            "tuId       bigint     NOT NULL, " +
            "localeId   bigint     NOT NULL, " +
            "tuvCount   smallint   NOT NULL," +
            "isSource   tinyint    NOT NULL," +
            "UNIQUE KEY (fingerprint, tuvCount, localeId, isSource, tuvId), " +
            "FOREIGN KEY (tuvId) REFERENCES " + getTuvTableName() + 
            " (id) ON DELETE CASCADE, " +
            "FOREIGN KEY (tuId) REFERENCES " + getTuTableName() + 
            " (id) on DELETE CASCADE" +
            ") ENGINE=InnoDB"
        );
    }

    @Override
    protected void createTuStorage(Connection conn) throws SQLException {
    }

    @Override
    protected void destroyAttrTable(Connection conn) throws SQLException {
    }

    @Override
    protected void destroyFuzzyIndex(Connection conn) throws SQLException {
        SQLUtil.exec(conn, "drop table if exists " + getFuzzyIndexTableName());
    }

    @Override
    protected void destroyTuStorage(Connection conn) throws SQLException {
    }

}
