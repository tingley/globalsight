package com.globalsight.ling.tm3.core;

import java.sql.Connection;
import java.sql.SQLException;

import com.globalsight.ling.tm3.core.persistence.SQLUtil;

/**
 * Common base class for TMs that have their own dedicated tables.
 *
 */
abstract class DedicatedStorageInfo<T extends TM3Data>  extends StorageInfo<T> {
    
    private TuStorage<T> tuStorage;
    
    protected DedicatedStorageInfo(BaseTm<T> tm, TM3TmType type) {
        super(tm, type);
    }

    @Override
    public TuStorage<T> getTuStorage() {
        if (tuStorage == null) {
            tuStorage = new DedicatedTuStorage<T>(this);
        }
        return tuStorage;
    }

    @Override
    protected void initializeTm() {
        BaseTm<T> tm = getTm();
        Long id = tm.getId();
        if (id == null) {
            throw new IllegalStateException("No id -- TM must be persisted before initialization");
        }
        tm.setTuTableName(getTableName(id, TU_TABLE_NAME));
        tm.setTuvTableName(getTableName(id, TUV_TABLE_NAME));
        tm.setFuzzyIndexTableName(getTableName(id, INDEX_TABLE_NAME));
        tm.setAttrValTableName(getTableName(id, ATTR_VAL_TABLE_NAME));
    }
    
    @Override
    protected void createTuStorage(Connection conn) throws SQLException {
        // XXX I could split this out between bilingual and multilingual
        // if I wanted by dropping the locale
        String tuTableName = getTuTableName();
        StringBuilder stmt = new StringBuilder(
            "CREATE TABLE " + tuTableName + " (" +
            "id bigint NOT NULL, " +
            "srcLocaleId bigint NOT NULL, ");
        for (TM3Attribute attr : getInlineAttributes()) {
            stmt.append(attr.getColumnName() + " ")
                .append(attr.getValueType().getSqlType())
                .append(", ");
        }
        stmt.append("PRIMARY KEY (id)");
        stmt.append(") ENGINE=InnoDB");
        SQLUtil.exec(conn, stmt.toString());

        // Now create the TUV table
        String tuvTableName = getTuvTableName();
        SQLUtil.exec(conn,
            "CREATE TABLE " + tuvTableName + " (" +
            "id bigint NOT NULL, " +
            "tuId bigint NOT NULL, " +
            "localeId bigint NOT NULL, " +
            "fingerprint bigint NOT NULL, " +
            "content text NOT NULL, " +
            "firstEventId bigint, " + 
            "lastEventId bigint, " +
            "creationUser varchar(80) DEFAULT NULL, " +
            "creationDate datetime NOT NULL, " +
            "modifyUser varchar(80) DEFAULT NULL, " +
            "modifyDate datetime NOT NULL, " +
            "PRIMARY KEY (id), " +
            "KEY (tuId, localeId), " +
            "KEY (fingerprint), " +
            "KEY (localeId), " + 
            "FOREIGN KEY (tuId) REFERENCES " + tuTableName + " (id) ON DELETE CASCADE, " +
            "FOREIGN KEY (firstEventID) REFERENCES TM3_EVENTS (id), " +
            "FOREIGN KEY (lastEventID) REFERENCES TM3_EVENTS (id) " +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8"
        );
    }

    // XXX This is now shared by all multilingual TMs
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
            "KEY (fingerprint, tuvCount, localeId, isSource, tuvId), " +
            "FOREIGN KEY (tuvId) REFERENCES " + getTuvTableName() + 
            " (id) ON DELETE CASCADE, " +
            "FOREIGN KEY (tuId) REFERENCES " + getTuTableName() + 
            " (id) on DELETE CASCADE" +
            ") ENGINE=InnoDB"
        );
    }
    
    @Override
    protected void createAttrTable(Connection conn) throws SQLException {
        SQLUtil.exec(conn, 
            "CREATE TABLE " + getAttrValTableName() + " (" +
            "tuId      bigint not null, " + 
            "attrId    bigint not null, " + 
            "value     varchar(" + MAX_ATTR_VALUE_LEN + ") not null, " + 
            "UNIQUE KEY(tuId, attrId), " + 
            "FOREIGN KEY (tuId) REFERENCES " + getTuTableName() + 
                    " (id) ON DELETE CASCADE, " +
            "FOREIGN KEY (attrId) REFERENCES TM3_ATTR (id) ON DELETE CASCADE " +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8"
        );
    }
    
    @Override
    protected void destroyAttrTable(Connection conn) throws SQLException {
        SQLUtil.exec(conn, "drop table if exists " + getAttrValTableName());
    }
    
    @Override
    protected void destroyTuStorage(Connection conn) throws SQLException {
        SQLUtil.exec(conn, "drop table if exists " + getTuvTableName());
        SQLUtil.exec(conn, "drop table if exists " + getTuTableName());
        getTuIds().destroy(conn);
        getTuvIds().destroy(conn);
    }
    
    
    @Override
    protected void destroyFuzzyIndex(Connection conn) throws SQLException {
        SQLUtil.exec(conn, "drop table if exists " + getFuzzyIndexTableName());
    }
        
}
