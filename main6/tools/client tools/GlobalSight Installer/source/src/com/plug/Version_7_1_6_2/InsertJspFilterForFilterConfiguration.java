package com.plug.Version_7_1_6_2;

import java.sql.SQLException;

import com.plug.Version_7_1_6_0.DbServer;

public class InsertJspFilterForFilterConfiguration
{
    public static void insertFilterConfigur(String name, String formatId,
            String tableName, String desc, long companyId) throws SQLException
    {
        DbServer dbServer = new DbServer();
        if(! dbServer.checkExist("filter_configuration", name, companyId))
        {
            StringBuilder insertSql = new StringBuilder("insert into "
                    + "filter_configuration(name, known_format_id, "
                    + "filter_table_name, filter_description, company_id) values ");

            insertSql.append("(");
            insertSql.append("'").append(name).append("',");
            insertSql.append("'").append(formatId).append("',");
            insertSql.append("'").append(tableName).append("',");
            insertSql.append("'").append(desc).append("',");
            insertSql.append(companyId);
            insertSql.append(")");
            insertSql.append("");

            dbServer.getDbUtil().insert(insertSql.toString());
        }
    }
}
