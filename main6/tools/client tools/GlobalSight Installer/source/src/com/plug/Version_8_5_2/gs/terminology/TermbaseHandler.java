package com.plug.Version_8_5_2.gs.terminology;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.util.db.ResultHander;

public class TermbaseHandler extends ResultHander<Termbase>
{
    public static String SQL = "select TBID, TB_NAME, TB_DESCRIPTION, TB_DEFINITION, COMPANYID from tb_termbase";

    @Override
    public Termbase handerResultSet(ResultSet resultSet) throws SQLException
    {
        Termbase tb = new Termbase();
        tb.setTBID(resultSet.getString(1));
        tb.setTB_NAME(resultSet.getString(2));
        tb.setTB_DESCRIPTION(resultSet.getString(3));
        tb.setTB_DEFINITION(resultSet.getString(4));
        tb.setCOMPANYID(resultSet.getString(5));
        
        return tb;
    }

}
