package com.globalsight.ling.tm3.core.persistence;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * This class should not be public; I need to fix the test setup.
 * 
 * Note that instances of this class are not themselves threadsafe.
 */
public class DistributedId {

    private String tableName;
    private long nextId = 0;
    private long max = 0;
    private int increment = 100;
    
    public DistributedId(String tableName) {
        this.tableName = tableName;
    }
    
    public DistributedId(String tableName, int increment) {
        this.tableName = tableName;
        this.increment = increment;
    }

    public long getId(Connection conn) throws SQLException {
        if (nextId >= max) {
            nextId = reserveId(conn, increment);
            max = nextId + increment;
        }
        return nextId++;
    }
    
    public void destroy(Connection conn) throws SQLException {
        SQLUtil.exec(conn, new StatementBuilder()
            .append("delete from TM3_ID where tableName = ?")
            .addValue(tableName));
    }

    /**
     * Reserve a set of |count| ids.
     * @param conn
     * @param count
     * @return
     * @throws SQLException
     */
    private long reserveId(Connection conn, int count) throws SQLException {
        // The magic to why this query works is the use of LAST_INSERT_ID(expr), which
        // will set the scoped LAST_INSERT_ID value to be the value of 'expr', 
        // prior to updating it. 
        SQLUtil.exec(conn, new StatementBuilder()
            .append("INSERT INTO TM3_ID (tableName, nextId) VALUES (?, LAST_INSERT_ID(1)+?)")
            .addValues(tableName, count)
            .append(" ON DUPLICATE KEY UPDATE nextId=LAST_INSERT_ID(nextId)+?")
            .addValue(count)
        );
        
        return SQLUtil.getLastInsertId(conn);
    }
}
