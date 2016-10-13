package com.plug.Version_8_5_2.gs.tm;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.util.db.ResultHander;

public class ProjectTMHandler extends ResultHander<ProjectTM>
{
    public static String SQL = "select ID, NAME, COMPANY_ID, IS_REMOTE_TM, TM3_ID, INDEX_TARGET from project_tm";

    @Override
    public ProjectTM handerResultSet(ResultSet resultSet) throws SQLException
    {
        ProjectTM tb = new ProjectTM();
        tb.setID(resultSet.getString(1));
        tb.setNAME(resultSet.getString(2));
        tb.setCOMPANY_ID(resultSet.getString(3));
        tb.setIS_REMOTE_TM(resultSet.getString(4));
        tb.setTM3_ID(resultSet.getString(5));
        tb.setINDEX_TARGET(resultSet.getString(6));
        
        return tb;
    }

}
