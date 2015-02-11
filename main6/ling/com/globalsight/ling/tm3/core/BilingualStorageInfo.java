package com.globalsight.ling.tm3.core;

import java.sql.Connection;
import java.sql.SQLException;
import com.globalsight.ling.tm3.core.persistence.SQLUtil;

class BilingualStorageInfo<T extends TM3Data> extends DedicatedStorageInfo<T> {

    protected BilingualStorageInfo(BilingualTm<T> tm) {
        super(tm, TM3TmType.BILINGUAL);
    }

    @Override
    public FuzzyIndex<T> getFuzzyIndex() {
        return new BilingualFuzzyIndex<T>(this);
    }

    @Override
    protected void createFuzzyIndex(Connection conn) throws SQLException {
        SQLUtil.exec(conn,
            "CREATE TABLE " + getFuzzyIndexTableName() + " (" +
            "fingerprint    bigint    NOT NULL, " +  
            "tuvId      bigint     NOT NULL, " +
            "tuId       bigint     NOT NULL, " +
            "tuvCount   smallint   NOT NULL," +
            "isSource   tinyint    NOT NULL," +
            "KEY (fingerprint, tuvCount, isSource, tuvId), " +
            "FOREIGN KEY (tuvId) REFERENCES " + getTuvTableName() + 
            " (id) ON DELETE CASCADE, " +
            "FOREIGN KEY (tuId) REFERENCES " + getTuTableName() + 
            " (id) on DELETE CASCADE" +
            ") ENGINE=InnoDB"
        );
    }
}
