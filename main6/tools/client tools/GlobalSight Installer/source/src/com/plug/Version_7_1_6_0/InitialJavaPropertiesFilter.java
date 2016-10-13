package com.plug.Version_7_1_6_0;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class InitialJavaPropertiesFilter implements InitialFilter
{
    private static Logger log = Logger
            .getLogger(InitialJavaPropertiesFilter.class);

    private DbServer dbServer = new DbServer();
    private static final Map<Integer, String> NAME_SID_ESCAPE = new HashMap<Integer, String>();
    static
    {
        NAME_SID_ESCAPE.put(0, "Java Properties Filter1_Y_Y");
        NAME_SID_ESCAPE.put(1, "Java Properties Filter2_Y_N");
        NAME_SID_ESCAPE.put(2, "Java Properties Filter3_N_Y");
        NAME_SID_ESCAPE.put(3, "Java Properties Filter4_N_N");
    }

    private static final String filterNamePrefix = "Java Properties Filter";

    private String builderFilterName(String fileProfileName)
    {
        return fileProfileName + "_" + filterNamePrefix;
    }

    public String insert() throws SQLException
    {
        log.info("Init table java_properties_filter...");
        List<SidEscapeCompany> allSidEscapeCompany = dbServer
                .getAllSidEscapeCompany();
        StringBuilder insertSql = new StringBuilder(
                "insert into java_properties_filter(filter_name, filter_description, enable_sid_support, enable_unicode_escape, company_id) values ");
        for (int i = 0; i < allSidEscapeCompany.size(); i++)
        {
            SidEscapeCompany sidEscapeCompany = allSidEscapeCompany.get(i);
            long companyId = sidEscapeCompany.getCompanyId();
            String filterName = builderFilterName(sidEscapeCompany
                    .getFileProfileName());
            String sidSupport = sidEscapeCompany.getEnableSidSupport();
            String unicodeEscape = sidEscapeCompany.getUnicodeEscape();
            if (!dbServer.checkExist(FilterConstants.JAVAPROPERTIES_TABLENAME,
                    filterName, companyId))
            {
                insertSql.append("(");
                insertSql.append("'").append(filterName).append("',");
                insertSql.append("'").append("").append("',");
                insertSql.append("'").append(sidSupport).append("',");
                insertSql.append("'").append(unicodeEscape).append("',");
                insertSql.append(companyId);
                insertSql.append(")");
                insertSql.append(",");
            }
        }
        insertSql = insertSql.deleteCharAt(insertSql.length() - 1);
        if (!"insert into java_properties_filter(filter_name, filter_description, enable_sid_support, enable_unicode_escape, company_id) values"
                .equals(insertSql.toString()))
        {
            dbServer.insert(insertSql.toString());
        }

        log.info("Successful");
        return insertSql.toString();
    }

    public void updateFilterIds(List list) throws SQLException
    {
        String[] sqls = new String[list.size()];
        for (int i = 0; i < list.size(); i++)
        {
            SidEscapeCompany sidEscapeCompany = (SidEscapeCompany) list.get(i);
            String sid = sidEscapeCompany.getEnableSidSupport();
            String escape = sidEscapeCompany.getUnicodeEscape();
            long companyId = sidEscapeCompany.getCompanyId();
            String fileProfileName = sidEscapeCompany.getFileProfileName();
            int filterId = getFilterId(sid, escape, companyId, fileProfileName);
            sqls[i] = "update file_profile set filter_id=" + filterId
                    + " where filter_id=-2 and is_sid_supported='" + sid
                    + "' and is_unicode_escape='" + escape + "' and companyid="
                    + companyId + " and filter_table_name='"
                    + FilterConstants.JAVAPROPERTIES_TABLENAME
                    + "' and name = '" + fileProfileName + "'";
        }

        dbServer.updateBatch(sqls);
    }

    private int getFilterId(String sid, String escape, long companyId,
            String fileProfileName) throws SQLException
    {
        String filterName = builderFilterName(fileProfileName);
        String sql = "select id from java_properties_filter where enable_sid_support='"
                + sid
                + "' and enable_unicode_escape='"
                + escape
                + "' and company_id="
                + companyId
                + " and filter_name = '"
                + filterName + "'";
        List list = dbServer.queryForSingleColumn(sql);
        return list.size() > 0 ? Integer.parseInt(list.get(0).toString()) : -1;
    }

    public List getValuesToUpdate() throws SQLException
    {
        return dbServer
                .getAllSidEscapeCompany(FilterConstants.JAVAPROPERTIES_TABLENAME);
    }
}
