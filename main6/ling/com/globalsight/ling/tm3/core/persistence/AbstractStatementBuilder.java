package com.globalsight.ling.tm3.core.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class AbstractStatementBuilder {
    private StringBuilder sb = new StringBuilder();
    private PreparedStatement ps;
    
    public AbstractStatementBuilder() { }
    
    public AbstractStatementBuilder(CharSequence s) {
        append(s);
    }

    protected PreparedStatement getStatement() {
        return ps;
    }
    
    protected StringBuilder getBuffer() {
        return sb;
    }
    
    protected void setStatement(PreparedStatement ps) {
        this.ps = ps;
    }
    
    public AbstractStatementBuilder append(CharSequence s) {
        sb.append(s);
        return this;
    }

    public void close() throws SQLException {
        if (getStatement() != null) {
            getStatement().close();
        }
    }

    public abstract PreparedStatement toPreparedStatement(Connection conn)
                throws SQLException;
    
    public abstract String toString();
}
