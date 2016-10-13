/**
 * 
 */
package com.util.db;

import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class ResultHander<T>
{
    public abstract T handerResultSet(ResultSet resultSet) throws SQLException;
}
