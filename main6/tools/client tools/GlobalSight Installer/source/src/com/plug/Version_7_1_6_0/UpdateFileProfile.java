package com.plug.Version_7_1_6_0;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UpdateFileProfile
{
    private DbServer dbServer = new DbServer();
    private static final String updateJavaProperties = "update file_profile set filter_table_name='java_properties_filter' where filter_id = -2 and known_format_type_id in (4,10,11)";
    private static final String updateJavaScript = "update file_profile set filter_table_name='java_script_filter' where filter_id = -2 and known_format_type_id =5";
    private static final String updateMSDocFilter = "update file_profile set filter_table_name='ms_office_doc_filter' where filter_id = -2 and known_format_type_id in (14,33)";
    private static final String updateXmlRule = "update file_profile set filter_table_name='xml_rule_filter' where filter_id = -2 and known_format_type_id in (7,15,16,17,25)";

    private void updateFilterTableName() throws SQLException
    {
        String[] sqls = new String[4];
        sqls[0] = updateJavaProperties;
        sqls[1] = updateJavaScript;
        sqls[2] = updateMSDocFilter;
        sqls[3] = updateXmlRule;
        dbServer.updateBatch(sqls);
    }

    public void updateFilterIds(ArrayList<InitialFilter> filters) throws SQLException
    {
        updateFilterTableName();
        for(int i = 0; i < filters.size(); i++)
        {
            InitialFilter filter = filters.get(i);
            filter.updateFilterIds(filter.getValuesToUpdate());
        }
    }
}
