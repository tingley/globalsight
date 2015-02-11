package com.plug.Version_7_1_6_0;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.util.Assert;
import com.util.db.DbUtil;
import com.util.db.DbUtilFactory;
import com.util.db.ResultHander;

public class DbServer
{
    private static Logger log = Logger.getLogger(DbServer.class);

    private DbUtil dbUtil = null;

    public DbUtil getDbUtil()
    {
        if (dbUtil == null)
        {
            dbUtil = DbUtilFactory.getDbUtil();
        }

        return dbUtil;
    }

    public List<Long> getAllCompanyIds() throws SQLException
    {
        String sql = "select id from company";
        List list = getDbUtil().queryForSingleColumn(sql);
        List<Long> companyIds = new ArrayList<Long>();
        for (int i = 0; i < list.size(); i++)
        {
            Assert.assertNotNull(list.get(i), "company id");
            companyIds.add(Long.parseLong(list.get(i).toString()));
        }
        return companyIds;
    }
    
    public List<String> getAllCompanyNames() throws SQLException
    {
        String sql = "select name from company";
        List list = getDbUtil().queryForSingleColumn(sql);
        List<String> names = new ArrayList<String>();
        for (int i = 0; i < list.size(); i++)
        {
            Assert.assertNotNull(list.get(i), "company name");
            names.add(list.get(i).toString());
        }
        return names;
    }

    public boolean checkExist(String tableName, String filterName,
            long companyId) throws SQLException
    {
        String checkSql = null;
        if ("filter_configuration".equals(tableName))
        {
            checkSql = "select id from " + tableName + " where name='"
                    + filterName + "' and company_id=" + companyId;
        }
        else
        {
            checkSql = "select id from " + tableName + " where filter_name='"
                    + filterName + "' and company_id=" + companyId;
        }

        return getDbUtil().queryForSingleColumn(checkSql).size() > 0;
    }

    public List<SidEscapeCompany> getAllSidEscapeCompany(
            String... filterTableName) throws SQLException
    {
        List<SidEscapeCompany> sidEscapeCompanies = new ArrayList<SidEscapeCompany>();
        String sql = "select is_sid_supported, is_unicode_escape, name,"
                + "companyid FROM file_profile f";
        if (filterTableName != null && filterTableName.length != 0)
        {
            sql += " where f.filter_table_name='" + filterTableName[0]
                    + "' and f.known_format_type_id in (4, 10, 11)";
        }
        else
        {
            sql += " where f.known_format_type_id in (4, 10, 11)";
        }
        List<ArrayList> lists = getDbUtil().query(sql);
        for (int i = 0; i < lists.size(); i++)
        {
            ArrayList al = lists.get(i);
            if (al.size() == 4)
            {
                String sid = al.get(0) == null ? "N" : al.get(0).toString();
                String escape = al.get(1) == null ? "N" : al.get(1).toString();
                String fileProfileName = al.get(2) == null ? "" : al.get(2)
                        .toString();
                Assert.assertNotNull(al.get(2), "Company id");
                long companyId = Long.parseLong(al.get(3).toString());
                SidEscapeCompany sidEscapeCompany = new SidEscapeCompany(sid,
                        escape, companyId, fileProfileName);
                sidEscapeCompanies.add(sidEscapeCompany);
            }
        }
        return sidEscapeCompanies;
    }

    public List<JSFunctionCompany> getAllJsFunctionCompany(
            String... filterTableName) throws SQLException
    {
        List<JSFunctionCompany> jsFunctionCompanies = new ArrayList<JSFunctionCompany>();
        String sql = "SELECT js_filter_regex, companyid, name "
                + "FROM file_profile f where js_filter_regex is not null and known_format_type_id in (5) ";
        if (filterTableName != null && filterTableName.length != 0)
        {
            sql += " and filter_table_name='" + filterTableName[0] + "' ";
        }
        List<ArrayList> lists = getDbUtil().query(sql);
        for (int i = 0; i < lists.size(); i++)
        {
            ArrayList al = lists.get(i);
            String jsFunction = (String) al.get(0);
            Assert.assertNotNull(al.get(1), "company id");
            long companyId = Long.parseLong(al.get(1).toString());
            String fileProfileName = al.get(2) == null ? "" : al.get(2)
                    .toString();
            JSFunctionCompany jsFunctionCompany = new JSFunctionCompany(
                    companyId, jsFunction, fileProfileName);
            jsFunctionCompanies.add(jsFunctionCompany);
        }
        return jsFunctionCompanies;
    }

    public List<HeaderTranslateCompany> getAllHeaderTranslateCompany(
            String... filterTableName) throws SQLException
    {
        List<HeaderTranslateCompany> sidEscapeCompanies = new ArrayList<HeaderTranslateCompany>();
        String sql = "select is_header_translate, companyid, name FROM file_profile f";
        if (filterTableName != null && filterTableName.length != 0)
        {
            sql += " where filter_table_name='" + filterTableName[0] + "' and known_format_type_id in (14, 33)";
        }else{
            sql += " where known_format_type_id in (14, 33)";
        }
        List<ArrayList> lists = getDbUtil().query(sql);
        for (int i = 0; i < lists.size(); i++)
        {
            ArrayList al = lists.get(i);
            String headerTranslate = al.get(0) == null ? "N" : al.get(0)
                    .toString();
            Assert.assertNotNull(al.get(1), "company id");
            long companyId = Long.parseLong(al.get(1).toString());
            String fileProfileName = al.get(2) == null ? "" : (String) al
                    .get(2);
            HeaderTranslateCompany headerTranslateCompany = new HeaderTranslateCompany(
                    headerTranslate, companyId, fileProfileName);
            sidEscapeCompanies.add(headerTranslateCompany);
        }
        return sidEscapeCompanies;
    }

    public List<RuleCompany> getAllRuleCompany(String... filterTableName)
            throws SQLException
    {
        List<RuleCompany> ruleCompanies = new ArrayList<RuleCompany>();
        String sql = "select xml_rule_id, companyid, name "
                + "FROM file_profile f where xml_rule_id is not null and known_format_type_id in (7, 15, 16, 17, 25)";
        if (filterTableName != null && filterTableName.length != 0)
        {
            sql += " and filter_table_name='" + filterTableName[0] + "'";
        }
        List<ArrayList> lists = getDbUtil().query(sql);
        for (int i = 0; i < lists.size(); i++)
        {
            ArrayList al = lists.get(i);
            Assert.assertNotNull(al.get(0), "xml rule id");
            Assert.assertNotNull(al.get(1), "company id");
            long ruleId = Long.parseLong(al.get(0).toString());
            long companyId = Long.parseLong(al.get(1).toString());
            String fileProfileName = al.get(2) == null ? "" : (String) al
                    .get(2);
            RuleCompany ruleCompany = new RuleCompany(ruleId, companyId,
                    fileProfileName);
            ruleCompanies.add(ruleCompany);
        }
        return ruleCompanies;
    }

    public long getCompanyIdByName(String name)
    {
        long id = -1;
        String sql = "select id from company where name='" + name + "'";
        try
        {
            List ids = getDbUtil().query(sql, new ResultHander()
            {
                public Object handerResultSet(ResultSet resultSet)
                        throws SQLException
                {
                    return resultSet.getLong(1);
                }
            });

            if (ids.size() > 1)
            {
                log.error("There are more then one companies named as " + name);
            }
            else if (ids.size() == 0)
            {
                log.info("Company(" + name + ") is not exist.");
            }
            else
            {
                id = (Long) (ids.get(0));
            }
        }
        catch (SQLException e)
        {
            log.error(e.getMessage(), e);
        }

        return id;
    }

    public void insert(String sql) throws SQLException
    {
        getDbUtil().insert(sql);
    }

    public void updateBatch(String[] sql) throws SQLException
    {
        getDbUtil().updateBatch(sql);
    }
    
    public void update(String sql) throws SQLException
    {
    	getDbUtil().update(sql);
    }

    public List queryForSingleColumn(String sql) throws SQLException
    {
        return getDbUtil().queryForSingleColumn(sql);
    }

    public List query(String sql) throws SQLException
    {
        return getDbUtil().query(sql);
    }
}
