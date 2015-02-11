package com.globalsight.ling.tm3.core.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper to simplify the use of PreparedStatements with batch values.
 * By default, this will produce a PreparedStatement that requests 
 * generated keys. 
 */
public class BatchStatementBuilder extends AbstractStatementBuilder {
    private List<Object[]> args = new ArrayList<Object[]>();
    private boolean getKeys = true;
    
    public BatchStatementBuilder() {
        super();
    }

    public BatchStatementBuilder(CharSequence s) {
        super(s);
    }
    
    public BatchStatementBuilder append(CharSequence s) {
        super.append(s);
        return this;
    }
    
    public BatchStatementBuilder append(BatchStatementBuilder builder) {
        super.append(builder.getBuffer());
        args.addAll(builder.args);
        return this;
    }
    
    public BatchStatementBuilder addBatch(Object...os) {
        args.add(os);
        return this;
    }
    
    /**
     * Sets whether getGeneratedKeys() is available following the 
     * execution of this statement.
     * @param value
     * @return
     */
    void setRequestKeys(boolean value) {
        this.getKeys = value;
    }
    
    /**
     * Create a PreparedStatement with all attached values inserted.
     */
    public PreparedStatement toPreparedStatement(Connection conn) 
            throws SQLException {
        PreparedStatement ps = conn.prepareStatement(getBuffer().toString(),
            getKeys ? Statement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS);
        for (Object[] arr : args) {
            int index = 1;
            for (Object o : arr) {
                ps.setObject(index++, o);
            }
            ps.addBatch();
        }
        this.setStatement(ps);
        return ps;
    }
    
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("BatchStatement[")
         .append(getBuffer())
         .append(", values=(");
        for (int i = 0; i < args.size(); i++) {
            if (i > 0) s.append(", ");
            s.append("[");
            Object[] os = args.get(i);
            for (int j = 0; j < os.length; j++) {
                if (j > 0) s.append(", ");
                s.append(os[j]);
            }
            s.append("]");
        }
        s.append(")]");
        return s.toString();
    }
}
