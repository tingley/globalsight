/**
 * 
 */
package com.util.db;

import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class ResultHander
{
    public abstract Object handerResultSet(ResultSet resultSet) throws SQLException;
}
