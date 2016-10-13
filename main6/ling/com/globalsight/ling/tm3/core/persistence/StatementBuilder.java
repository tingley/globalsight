package com.globalsight.ling.tm3.core.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class to simplify construction of {@link PreparedStatement}.
 */
public class StatementBuilder extends AbstractStatementBuilder {
    private List<Object> args = new ArrayList<Object>();
    
    public StatementBuilder() { 
        super();
    }
    
    public StatementBuilder(CharSequence s) {
        super(s);
    }
    
    public StatementBuilder(CharSequence s, Object...args) {
        super(s);
        addValues(args);
    }
    
    public StatementBuilder append(CharSequence s) {
        super.append(s);
        return this;
    }
    
    public StatementBuilder append(StatementBuilder builder) {
        super.append(builder.getBuffer());
        args.addAll(builder.args);
        return this;
    }
    
    public StatementBuilder addValue(Object o) {
        args.add(o);
        return this;
    }
    
    public StatementBuilder addValues(Object...os) {
        args.addAll(Arrays.asList(os));
        return this;
    }
    
    /**
     * Create a PreparedStatement with all attached values inserted.
     */
    public PreparedStatement toPreparedStatement(Connection conn) 
            throws SQLException {
        PreparedStatement ps = conn.prepareStatement(getBuffer().toString());
        int index = 1;
        for (Object o : args) {
            ps.setObject(index++, o);
        }
        this.setStatement(ps);
        return ps;
    }
    
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("Statement[")
         .append(getBuffer())
         .append(", values=(");
        for (int i = 0; i < args.size(); i++) {
            if (i > 0) s.append(", ");
            s.append(args.get(i));
        }
        s.append(")]");
        return s.toString();
    }
}
