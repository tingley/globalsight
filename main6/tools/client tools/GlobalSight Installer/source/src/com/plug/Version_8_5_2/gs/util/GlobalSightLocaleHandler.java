package com.plug.Version_8_5_2.gs.util;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.util.db.ResultHander;

public class GlobalSightLocaleHandler extends ResultHander<GlobalSightLocale>
{
    public static String SQL = "select ID, ISO_LANG_CODE, ISO_COUNTRY_CODE, IS_UI_LOCALE from locale";

    @Override
    public GlobalSightLocale handerResultSet(ResultSet resultSet) throws SQLException
    {
        GlobalSightLocale tb = new GlobalSightLocale();
        tb.setId(resultSet.getLong(1));
        tb.setLanguage(resultSet.getString(2));
        tb.setCountry(resultSet.getString(3));
        tb.setIsUiLocale("Y".equalsIgnoreCase(resultSet.getString(4)));
        
        return tb;
    }

}
