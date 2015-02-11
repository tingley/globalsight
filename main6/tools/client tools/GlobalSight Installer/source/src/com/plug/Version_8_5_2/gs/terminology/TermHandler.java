package com.plug.Version_8_5_2.gs.terminology;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.lucene.document.Document;

import com.plug.Version_8_5_2.gs.ling.lucene.IndexDocument;
import com.util.db.ResultHander;

public class TermHandler extends ResultHander<Document>
{
    @Override
    public Document handerResultSet(ResultSet resultSet) throws SQLException
    {
        Document doc = IndexDocument.DataDocument(resultSet.getLong(1), resultSet.getLong(2), resultSet.getString(3));
        
        return doc;
    }

    public static String generateSQL(String tbid, String lang)
    {
        String SQL = "select CID, tID, TERM from tb_term where TBID=" + tbid + " and LANG_NAME='"
                + lang + "'";
        return SQL;
    }

}
