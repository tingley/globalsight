package com.plug.Version_7_1_6_0;

import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

public class InitialFilterConfiguration implements InitialFilter
{
    private static Logger log = Logger.getLogger(InitialFilterConfiguration.class);
    
    private DbServer dbServer = new DbServer();
    private static final Vector<String> FILTER_NAMES = new Vector<String>();
    private static final Vector<String> KNOWNFORMATIDS = new Vector<String>();
    private static final Vector<String> FILTER_TABLE_NAMES = new Vector<String>();
    private static final Vector<String> FILTER_DESCRIPTION = new Vector<String>();

    static
    {
        FILTER_NAMES.add("Java Properties Filter");
        FILTER_NAMES.add("Java Script Filter");
        FILTER_NAMES.add("MS Office Doc Filter");
        FILTER_NAMES.add("XML Rule Filter");

        KNOWNFORMATIDS.add("|4|10|11|");
        KNOWNFORMATIDS.add("|5|");
        KNOWNFORMATIDS.add("|14|33|");
        KNOWNFORMATIDS.add("|7|15|16|17|25|");

        FILTER_TABLE_NAMES.add("java_properties_filter");
        FILTER_TABLE_NAMES.add("java_script_filter");
        FILTER_TABLE_NAMES.add("ms_office_doc_filter");
        FILTER_TABLE_NAMES.add("xml_rule_filter");

        FILTER_DESCRIPTION.add("The filter for java properties files.");
        FILTER_DESCRIPTION.add("The filter for java script files.");
        FILTER_DESCRIPTION.add("The filter for MS office doc files.");
        FILTER_DESCRIPTION.add("The filter for XML Rules.");
    }

    public String insert() throws SQLException
    {
        log.info("Init table filter_configuration...");
        
        List<Long> list = dbServer.getAllCompanyIds();
        StringBuilder insertSql = new StringBuilder(
                "insert into filter_configuration(name, known_format_id, filter_table_name, filter_description, company_id) values ");

        for (int i = 0; i < list.size(); i++)
        {
            long companyId = list.get(i);
            for (int j = 0; j < FILTER_NAMES.size(); j++)
            {
                if (dbServer.checkExist("filter_configuration", FILTER_NAMES
                        .get(j), companyId))
                {
                    continue;
                }
                insertSql.append("(");
                insertSql.append("'").append(FILTER_NAMES.get(j)).append("',");
                insertSql.append("'").append(KNOWNFORMATIDS.get(j))
                        .append("',");
                insertSql.append("'").append(FILTER_TABLE_NAMES.get(j)).append(
                        "',");
                insertSql.append("'").append(FILTER_DESCRIPTION.get(j)).append(
                        "',");
                insertSql.append(companyId);
                insertSql.append(")");
                insertSql.append(",");
            }
        }
        insertSql = insertSql.deleteCharAt(insertSql.length() - 1);
        if (!"insert into filter_configuration(name, known_format_id, filter_table_name, filter_description, company_id) values"
                .equals(insertSql.toString()))
        {
            dbServer.insert(insertSql.toString());
        }
        
        log.info("Successful");
        return insertSql.toString();
    }

    @Override
    public List getValuesToUpdate()
    {
        return null;
    }

    @Override
    public void updateFilterIds(List list)
    {

    }
}
