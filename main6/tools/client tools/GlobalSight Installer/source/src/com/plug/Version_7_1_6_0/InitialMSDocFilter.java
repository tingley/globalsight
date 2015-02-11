package com.plug.Version_7_1_6_0;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class InitialMSDocFilter implements InitialFilter
{
    private static Logger log = Logger.getLogger(InitialMSDocFilter.class);

    private DbServer dbServer = new DbServer();
    private static final Map<Integer, String> NAME_HEADERTRANSLATE = new HashMap<Integer, String>();
    static
    {
        NAME_HEADERTRANSLATE.put(0, "MS Office Doc Filter1_Y");
        NAME_HEADERTRANSLATE.put(1, "MS Office Doc Filter2_N");
    }
    private String filterNamePrefix = "MS Office Doc Filter";

    private String buildFilterName(String fileProfileName)
    {
        return fileProfileName + "_" + filterNamePrefix;
    }

    public String insert() throws SQLException
    {
        List<HeaderTranslateCompany> allHeaderTranslateCompany = dbServer
                .getAllHeaderTranslateCompany();
        log.info("Init table ms_office_doc_filter...");

        StringBuilder insertSql = new StringBuilder(
                "insert into ms_office_doc_filter(filter_name, filter_description, is_header_translate, company_id) values ");
        for (int i = 0; i < allHeaderTranslateCompany.size(); i++)
        {
            HeaderTranslateCompany headerTranslateCompany = allHeaderTranslateCompany
                    .get(i);
            long companyId = headerTranslateCompany.getCompanyId();
            String filterName = buildFilterName(headerTranslateCompany
                    .getFileProfileName());
            String headerTranslate = headerTranslateCompany
                    .getHeaderTranslate();
            if (!dbServer.checkExist(FilterConstants.MSOFFICEDOC_TABLENAME,
                    filterName, companyId))
            {
                insertSql.append("(");
                insertSql.append("'").append(filterName).append("',");
                insertSql.append("'").append("").append("',");
                insertSql.append("'").append(headerTranslate).append("',");
                insertSql.append(companyId);
                insertSql.append(")");
                insertSql.append(",");
            }
        }
        insertSql = insertSql.deleteCharAt(insertSql.length() - 1);
        if (!"insert into ms_office_doc_filter(filter_name, filter_description, is_header_translate, company_id) values"
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
            HeaderTranslateCompany headerTranslateCompany = (HeaderTranslateCompany) list
                    .get(i);
            String headerTranslate = headerTranslateCompany
                    .getHeaderTranslate();
            long companyId = headerTranslateCompany.getCompanyId();
            String fileProfileName = headerTranslateCompany
                    .getFileProfileName();
            int filterId = getFilterId(headerTranslate, companyId,
                    fileProfileName);
            sqls[i] = "update file_profile set filter_id=" + filterId
                    + " where filter_id=-2 and is_header_translate='"
                    + headerTranslate + "' and companyid=" + companyId
                    + " and filter_table_name='"
                    + FilterConstants.MSOFFICEDOC_TABLENAME + "' and name = '"
                    + fileProfileName + "'";
        }
        dbServer.updateBatch(sqls);
    }

    private int getFilterId(String headerTranslate, long companyId,
            String fileProfileName) throws SQLException
    {
        String filterName = buildFilterName(fileProfileName);
        String sql = "select id from ms_office_doc_filter where is_header_translate='"
                + headerTranslate
                + "' and company_id="
                + companyId
                + " and filter_name = '" + filterName + "'";
        List list = dbServer.queryForSingleColumn(sql);
        return list.size() > 0 ? Integer.parseInt(list.get(0).toString()) : -1;
    }

    @Override
    public List getValuesToUpdate() throws SQLException
    {
        return dbServer
                .getAllHeaderTranslateCompany(FilterConstants.MSOFFICEDOC_TABLENAME);
    }
}
