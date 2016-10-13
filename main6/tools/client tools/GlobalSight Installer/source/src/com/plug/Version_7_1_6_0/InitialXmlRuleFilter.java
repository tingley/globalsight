package com.plug.Version_7_1_6_0;

import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;

public class InitialXmlRuleFilter implements InitialFilter
{
    private static Logger log = Logger.getLogger(InitialXmlRuleFilter.class);

    private DbServer dbServer = new DbServer();
    private String filterNamePrefix = "XML Rule Filter";

    private String buildFilterName(String fileProfileName)
    {
        return fileProfileName + "_" + filterNamePrefix;
    }

    public String insert() throws SQLException
    {
        log.info("Init table xml_rule_filter...");

        List<RuleCompany> ruleCompanies = dbServer.getAllRuleCompany();
        StringBuilder insertSql = new StringBuilder(
                "insert into xml_rule_filter(filter_name, filter_description, xml_rule_id, company_id) values ");
        for (int i = 0; i < ruleCompanies.size(); i++)
        {
            RuleCompany ruleCompany = ruleCompanies.get(i);
            long ruleId = ruleCompany.getXmlRuleId();
            long companyId = ruleCompany.getCompanyId();
            String filterName = buildFilterName(ruleCompany
                    .getFileProfileName());
            if (!dbServer.checkExist(FilterConstants.XMLRULE_TABLENAME,
                    filterName, companyId))
            {
                insertSql.append("(");
                insertSql.append("'").append(filterName).append("',");
                insertSql.append("'").append("").append("',");
                insertSql.append(ruleId).append(",");
                insertSql.append(companyId);
                insertSql.append(")");
                insertSql.append(",");
            }
        }
        insertSql = insertSql.deleteCharAt(insertSql.length() - 1);
        if (!"insert into xml_rule_filter(filter_name, filter_description, xml_rule_id, company_id) values"
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
            RuleCompany ruleCompany = (RuleCompany) list.get(i);
            long ruleId = ruleCompany.getXmlRuleId();
            long companyId = ruleCompany.getCompanyId();

            String fileProfileName = ruleCompany.getFileProfileName();
            int filterId = getFilterId(ruleId, companyId, fileProfileName);
            sqls[i] = "update file_profile set filter_id=" + filterId
                    + " where filter_id=-2 and xml_rule_id=" + ruleId
                    + " and companyid=" + companyId
                    + " and filter_table_name='"
                    + FilterConstants.XMLRULE_TABLENAME + "' and name = '"
                    + fileProfileName + "'";
        }
        dbServer.updateBatch(sqls);
    }

    private int getFilterId(long ruleId, long companyId, String fileProfileName)
            throws SQLException
    {
        String filterName = buildFilterName(fileProfileName);
        String sql = "select id from xml_rule_filter where xml_rule_id="
                + ruleId + " and company_id=" + companyId
                + " and filter_name = '" + filterName + "'";
        List list = dbServer.queryForSingleColumn(sql);
        return list.size() > 0 ? Integer.parseInt(list.get(0).toString()) : -1;
    }

    public List getValuesToUpdate() throws SQLException
    {
        return dbServer.getAllRuleCompany(FilterConstants.XMLRULE_TABLENAME);
    }

}
