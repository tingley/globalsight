package com.plug.Version_7_1_6_0;

import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;

public class InitialJavaScriptFilter implements InitialFilter
{
    private static Logger log = Logger.getLogger(InitialJavaScriptFilter.class);
    private DbServer dbServer = new DbServer();
    private String filterNamePrefix = "Java Script Filter";

    private String buildFilterName(String fileProfileName)
    {
        return fileProfileName + "_" + filterNamePrefix;
    }

    public String insert() throws SQLException
    {
        log.info("Init table java_script_filter...");

        List<JSFunctionCompany> jsFunctionCompanies = dbServer
                .getAllJsFunctionCompany();
        StringBuilder insertSql = new StringBuilder(
                "insert into java_script_filter(filter_name, filter_description, js_function_filter, company_id) values ");
        for (int i = 0; i < jsFunctionCompanies.size(); i++)
        {
            JSFunctionCompany jsFunctionCompany = jsFunctionCompanies.get(i);
            String jsFunction = jsFunctionCompany.getJsFunction();
            long companyId = jsFunctionCompany.getCompanyId();
            String filterName = buildFilterName(jsFunctionCompany
                    .getFileProfileName());
            if (!dbServer.checkExist(FilterConstants.JAVASCRIPT_TABLENAME,
                    filterName, companyId))
            {
                insertSql.append("(");
                insertSql.append("'").append(filterName).append("',");
                insertSql.append("'").append("").append("',");
                insertSql.append("'").append(jsFunction).append("',");
                insertSql.append(companyId);
                insertSql.append(")");
                insertSql.append(",");
            }
        }
        insertSql = insertSql.deleteCharAt(insertSql.length() - 1);
        if (!"insert into java_script_filter(filter_name, filter_description, js_function_filter, company_id) values"
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
            JSFunctionCompany jsFunctionCompany = (JSFunctionCompany) list
                    .get(i);
            String jsFunction = jsFunctionCompany.getJsFunction();
            long companyId = jsFunctionCompany.getCompanyId();

            String fileProfileName = jsFunctionCompany.getFileProfileName();
            int filterId = getFilterId(jsFunction, companyId, fileProfileName);
            sqls[i] = "update file_profile set filter_id=" + filterId
                    + " where filter_id=-2 and js_filter_regex='" + jsFunction
                    + "' and companyid=" + companyId
                    + " and filter_table_name='"
                    + FilterConstants.JAVASCRIPT_TABLENAME + "' and name = '"
                    + fileProfileName + "'";
        }
        dbServer.updateBatch(sqls);
    }

    private int getFilterId(String jsFunction, long companyId,
            String fileProfileName) throws SQLException
    {
        String filterName = buildFilterName(fileProfileName);
        String sql = "select id from java_script_filter where js_function_filter='"
                + jsFunction
                + "' and company_id="
                + companyId
                + " and filter_name = '" + filterName + "'";
        List list = dbServer.queryForSingleColumn(sql);
        return list.size() > 0 ? Integer.parseInt(list.get(0).toString()) : -1;
    }

    public List getValuesToUpdate() throws SQLException
    {
        return dbServer
                .getAllJsFunctionCompany(FilterConstants.JAVASCRIPT_TABLENAME);
    }

}
