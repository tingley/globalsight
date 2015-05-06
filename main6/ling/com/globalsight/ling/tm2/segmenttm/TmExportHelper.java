package com.globalsight.ling.tm2.segmenttm;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.globalsight.everest.tm.Tm;
import com.globalsight.everest.tm.exporter.ExportUtil;
import com.globalsight.terminology.util.SqlUtil;
import com.globalsight.util.StringUtil;

/**
 * Contains legacy code moved from 
 *      com.globalsight.everest.tm.exporter.ReaderThread
 *      
 * To fetch TUs from TM2 TMs for export.
 */
public class TmExportHelper {
    static private final Logger CATEGORY = Logger
                .getLogger(TmExportHelper.class);
    static private final DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    /**
     * Get the count of all TUs in the TM.
     */
    static int getAllTuCount(Connection conn, Tm tm, String createdAfter, String createdBefore)
            throws SQLException
    {
        int result = getAllCount(conn, tm, createdAfter, createdBefore, "TU");
        return result;
    }
    
	static int getAllTuCountByParamMap(Connection conn, Tm tm,
			Map<String, String> paramMap) throws SQLException
	{
		int result = getTuIdsByParamMap(conn, tm, paramMap, null, null, null)
				.size();
		return result;
	}

    static int getAllTuCount(Connection conn, Tm tm, String createdAfter, String createdBefore,Set<String> attributeSet)
    		throws SQLException
	{
		int result = getAllCount(conn, tm, createdAfter, createdBefore, "TU",
				attributeSet);
		return result;
	}
    
    static int getAllTuCountByParamMap(Connection conn, Tm tm, Map<String,String> paramMap,Set<String> attributeSet)
    		throws SQLException
	{
		int result = getTuIdsByParamMap(conn, tm, paramMap, attributeSet, null,
				null).size();
		return result;
	}
    
    /**
     * Get the count of all TUs in the TM.
     */
    static int getAllTuCount(Connection conn, Tm tm, long startTUId)
            throws SQLException
    {
        int result = getAllCount(conn, tm, startTUId, "TU");
        return result;
    }

    /**
     * Get the count of all TUVs in the TM.
     */
    static int getAllTuvCount(Connection conn, Tm tm, String createdAfter,
            String createdBefore) throws SQLException
    {
        int result = getAllCount(conn, tm, createdAfter, createdBefore, "TUV");
        return result;
    }

    private static int getAllCount(Connection conn, Tm tm, String createdAfter,
            String createdBefore, String type) throws SQLException
    {
        int result = 0;

        Statement stmt = null;
        ResultSet rset = null;

        try
        {
            String sql ="";
            if("TU".equals(type))
            {
                sql = "SELECT COUNT(DISTINCT tu.id) ";
                        
            }
            else
            {
                // TUV
                sql = "SELECT COUNT(tu.id) ";
            }
            sql += "FROM project_tm_tuv_t tuv, project_tm_tu_t tu "
                    + "  WHERE tu.id = tuv.tu_id and tu.tm_id = " + tm.getId();
            sql += getSqlExpression("tuv", createdAfter, createdBefore);
            stmt = conn.createStatement();
            rset = stmt.executeQuery(sql);
            if (rset.next())
            {
                result = rset.getInt(1);
            }
        }
        catch (SQLException e)
        {
            try
            {
                conn.rollback();
            }
            catch (Throwable ignore)
            {
            }
            CATEGORY.warn("can't read TM data", e);

            throw e;
        }
        finally
        {
            try
            {
                if (rset != null)
                    rset.close();
                if (stmt != null)
                    stmt.close();
            }
            catch (Throwable ignore)
            {
            }

        }

        return result;
    }

	private static List<Long> getTuIdsByParamMap(Connection conn, Tm tm,
			Map<String, String> paramMap, Set<String> attributeSet,
			String localeIds, String projectName) throws SQLException
	{
		List<Long> result = null;
		try
		{
			String sql = "";
			String tableSql = "FROM project_tm_tuv_t tuv, project_tm_tu_t tu "
					+ "  WHERE tu.id = tuv.tu_id and tu.tm_id = " + tm.getId();
			if (attributeSet != null && attributeSet.size() > 0)
			{
				tableSql += getAttributeSql(attributeSet);
			}
			if (StringUtils.isNotBlank(localeIds))
			{
				tableSql += " AND tuv.locale_id in ( " + localeIds + " )";
			}
			if (StringUtils.isNotBlank(projectName))
			{
				tableSql += " AND tuv.updated_by_project is NOT NULL AND ("
						+ buildCondition(projectName) + ")";
			}

			String orderSql = " ORDER BY tu.id ASC";
			if (!paramMap.isEmpty())
			{
				String stringId = paramMap.get("stringId");
				String isRegex = paramMap.get("isRegex");
				if (StringUtils.isNotBlank(stringId))
				{
					sql = "SELECT DISTINCT tu.id,tuv.sid ";
					sql += tableSql;
					sql += getSqlByParamMap("tuv", paramMap);
					sql += orderSql;
					result = getMapWithSID(conn, sql, stringId, isRegex);
				}
				else
				{
					sql = "SELECT DISTINCT tu.id ";
					sql += tableSql;
					sql += getSqlByParamMap("tuv", paramMap);
					sql += orderSql;
					result = getIdList(conn, sql);
				}

			}
			else
			{
				sql = "SELECT DISTINCT tu.id ";
				sql += tableSql;
				sql += orderSql;
				result = getIdList(conn, sql);
			}
		}
		catch (SQLException e)
		{
			try
			{
				conn.rollback();
			}
			catch (Throwable ignore)
			{
			}
			CATEGORY.warn("can't read TM data", e);

			throw e;
		}

		return result;
	}

    private static int getAllCount(Connection conn, Tm tm, String createdAfter,
            String createdBefore, String type,Set<String> jobAttributeSet) throws SQLException
    {
        int result = 0;

        Statement stmt = null;
        ResultSet rset = null;

        try
        {
            String sql ="";
            if("TU".equals(type))
            {
                sql = "SELECT COUNT(DISTINCT tu.id) ";
                        
            }
            else
            {
                // TUV
                sql = "SELECT COUNT(tu.id) ";
            }
            sql += "FROM project_tm_tuv_t tuv, project_tm_tu_t tu , project_tm_tu_t_prop prop "
                    + "  WHERE tu.id = tuv.tu_id and tu.tm_id = " + tm.getId();
            if(jobAttributeSet != null && jobAttributeSet.size() > 0)
            {
            	sql += getAttributeSql(jobAttributeSet);
            }
            sql += getSqlExpression("tuv", createdAfter, createdBefore);
            stmt = conn.createStatement();
            rset = stmt.executeQuery(sql);
            if (rset.next())
            {
                result = rset.getInt(1);
            }
        }
        catch (SQLException e)
        {
            try
            {
                conn.rollback();
            }
            catch (Throwable ignore)
            {
            }
            CATEGORY.warn("can't read TM data", e);

            throw e;
        }
        finally
        {
            try
            {
                if (rset != null)
                    rset.close();
                if (stmt != null)
                    stmt.close();
            }
            catch (Throwable ignore)
            {
            }

        }

        return result;
    }
    
    private static String getAttributeSql(Set<String> jobAttributeSet)
    {
    	
    	StringBuffer sql = new StringBuffer();
    	String key;
		String value;
		int count = 0;
		
    	for(String keyAndValue: jobAttributeSet)
    	{
    		count++;
    		key = keyAndValue.substring(0,keyAndValue.indexOf(":"));
			value = keyAndValue.substring(keyAndValue.indexOf(":") + 1).replaceAll("'", "''");
			sql.append(" and tu.id in ( select DISTINCT tu_id  from  project_tm_tu_t_prop as prop where prop.prop_value = '" + value + "' and prop.prop_type = 'Att::" + key + "' )");
    	}
    	
    	return sql.toString();
    }

    private static int getAllCount(Connection conn, Tm tm, long startTUId, String type) throws SQLException
    {
        int result = 0;

        Statement stmt = null;
        ResultSet rset = null;

        try
        {
            String sql ="";
            if("TU".equals(type))
            {
                sql = "SELECT COUNT(DISTINCT tu.id) ";
                        
            }
            else
            {
                // TUV
                sql = "SELECT COUNT(tu.id) ";
            }
            sql += "FROM project_tm_tuv_t tuv, project_tm_tu_t tu "
                    + "  WHERE tu.id = tuv.tu_id and tu.tm_id = "
                    + tm.getId()
                    + " and tu.id > "
                    + startTUId;
            
            stmt = conn.createStatement();
            rset = stmt.executeQuery(sql);
            if (rset.next())
            {
                result = rset.getInt(1);
            }
        }
        catch (SQLException e)
        {
            try
            {
                conn.rollback();
            }
            catch (Throwable ignore)
            {
            }
            CATEGORY.warn("can't read TM data", e);

            throw e;
        }
        finally
        {
            try
            {
                if (rset != null)
                    rset.close();
                if (stmt != null)
                    stmt.close();
            }
            catch (Throwable ignore)
            {
            }

        }

        return result;
    }

    /**
     * Gets all TU IDs in the TM.
     */
    static List<Long> getAllTuIds(Connection conn, Tm tm, String createdAfter, String createdBefore)
            throws SQLException
    {
        List<Long> result = new ArrayList<Long>();

        Statement stmt = null;
        ResultSet rset = null;

        try
        {
            String sql = "SELECT DISTINCT tu.id "
                    + "FROM project_tm_tuv_t tuv, project_tm_tu_t tu "
                    + "  WHERE tu.id = tuv.tu_id and tu.tm_id = "
                    + tm.getId();
            sql += getSqlExpression("tuv", createdAfter, createdBefore);
            sql += " ORDER by tu.id ASC";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(sql);

            while (rset.next())
            {
                result.add(new Long(rset.getLong(1)));
            }
        }
        catch (SQLException e)
        {
            try
            {
                conn.rollback();
            }
            catch (Throwable ignore)
            {
            }
            CATEGORY.warn("can't read TM data", e);

            throw e;
        }
        finally
        {
            try
            {
                if (rset != null) rset.close();
                if (stmt != null) stmt.close();
            }
            catch (Throwable ignore)
            {
            }

        }

        return result;
    }
    
    static List<Long> getAllTuIds(Connection conn, Tm tm, String createdAfter, String createdBefore,Set<String> jobAttributeSet)
    			throws SQLException
	{
		List<Long> result = new ArrayList<Long>();
		
		Statement stmt = null;
		ResultSet rset = null;
		
		try
		{
		    String sql = "SELECT DISTINCT tu.id "
		            + "FROM project_tm_tuv_t tuv, project_tm_tu_t tu , project_tm_tu_t_prop prop "
		            +" WHERE tu.id = tuv.tu_id and tu.tm_id = "
		            + tm.getId();
		    
		    if(jobAttributeSet != null && jobAttributeSet.size() > 0)
            {
            	sql += getAttributeSql(jobAttributeSet);
            }
		    sql += getSqlExpression("tuv", createdAfter, createdBefore);
		    sql += " ORDER by tu.id ASC";
		    stmt = conn.createStatement();
		    rset = stmt.executeQuery(sql);
		
		    while (rset.next())
		    {
		        result.add(new Long(rset.getLong(1)));
		    }
		}
		catch (SQLException e)
		{
		    try
		    {
		        conn.rollback();
		    }
		    catch (Throwable ignore)
		    {
		    }
		    CATEGORY.warn("can't read TM data", e);
		
		    throw e;
		}
		finally
		{
		    try
		    {
		        if (rset != null) rset.close();
		        if (stmt != null) stmt.close();
		    }
		    catch (Throwable ignore)
		    {
		    }
		
		}
		
		return result;
	}
    
	static List<Long> getAllTuIdsByParamMap(Connection conn, Tm tm,
			Map<String, String> paramMap) throws SQLException
	{
		return getTuIdsByParamMap(conn, tm, paramMap, null, null, null);
	}

	static List<Long> getAllTuIdsByParamMap(Connection conn, Tm tm,
			Map<String, String> paramMap, Set<String> jobAttributeSet)
			throws SQLException
	{
		return getTuIdsByParamMap(conn, tm, paramMap, jobAttributeSet, null,
				null);
	}

    /**
     * Gets all TU IDs in the TM.
     */
    static List<Long> getAllTuIds(Connection conn, Tm tm, long startTUId)
            throws SQLException
    {
        List<Long> result = new ArrayList<Long>();

        Statement stmt = null;
        ResultSet rset = null;

        try
        {
            String sql = "SELECT DISTINCT tu.id "
                    + "FROM project_tm_tuv_t tuv, project_tm_tu_t tu "
                    + "  WHERE tu.id = tuv.tu_id and tu.tm_id = "
                    + tm.getId()
                    + " and tu.id > "
                    + startTUId;
            
            sql += " ORDER by tu.id ASC";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(sql);

            while (rset.next())
            {
                result.add(new Long(rset.getLong(1)));
            }
        }
        catch (SQLException e)
        {
            try
            {
                conn.rollback();
            }
            catch (Throwable ignore)
            {
            }
            CATEGORY.warn("can't read TM data", e);

            throw e;
        }
        finally
        {
            try
            {
                if (rset != null) rset.close();
                if (stmt != null) stmt.close();
            }
            catch (Throwable ignore)
            {
            }

        }

        return result;
    }
    
	static int getFilteredTuCountByParamMap(Connection conn, Tm tm,
			List<String> localeList, Map<String, String> paramMap)
			throws Exception
	{
		String localeIds = getLocaleIds(localeList);
		return getTuIdsByParamMap(conn, tm, paramMap, null, localeIds, null)
				.size();
	}

	static int getFilteredTuCountByParamMap(Connection conn, Tm tm,
			List<String> localeList, Map<String, String> paramMap,
			Set<String> jobAttributeSet) throws Exception
	{
		String localeIds = getLocaleIds(localeList);
		return getTuIdsByParamMap(conn, tm, paramMap, jobAttributeSet,
				localeIds, null).size();
	}
	
    /**
     * Gets the count of TUs that have a TUV in a given language.
     */
    static int getFilteredTuCount(Tm tm, List<String> localeList, String createdAfter,
                                 String createdBefore) throws Exception
     {
        int result = 0;

        Connection conn = null;
        Statement stmt = null;
        ResultSet rset = null;

        try
        {
			String localeIds = getLocaleIds(localeList);
            String sql = "SELECT COUNT(DISTINCT tu.id) "
                    + "FROM project_tm_tuv_t tuv, project_tm_tu_t tu "
                    + "WHERE tuv.locale_id in ( " + localeIds
                    + " ) AND tuv.tu_id = tu.id " + "  AND tu.tm_id = "
                    + tm.getId();
            sql += getSqlExpression("tuv", createdAfter, createdBefore);

            conn = SqlUtil.hireConnection();
            stmt = conn.createStatement();
            rset = stmt.executeQuery(sql);

            if (rset.next())
            {
                result = rset.getInt(1);
            }
        }
        catch (/* SQL */Exception e)
        {
            try
            {
                conn.rollback();
            }
            catch (Throwable ignore)
            {
            }
            CATEGORY.warn("can't read TM data", e);

            throw e;
        }
        finally
        {
            try
            {
                if (rset != null) rset.close();
                if (stmt != null) stmt.close();
            }
            catch (Throwable ignore)
            {
            }

            SqlUtil.fireConnection(conn);
        }

        return result;
     }
    

	static int getFilteredTuCount(Tm tm, List<String> localeList,
			String createdAfter, String createdBefore,
			Set<String> jobAttributeSet) throws Exception
	{
		int result = 0;


		Connection conn = null;
		Statement stmt = null;
		ResultSet rset = null;

		try 
		{
			String localeIds = getLocaleIds(localeList);
			String sql = "SELECT COUNT(DISTINCT tu.id) "
					+ "FROM project_tm_tuv_t tuv, project_tm_tu_t tu , project_tm_tu_t_prop prop "
					+ "WHERE tuv.locale_id in (" + localeIds
					+ " ) AND tuv.tu_id = tu.id " + "  AND tu.tm_id = "
					+ tm.getId();
			
			if(jobAttributeSet != null && jobAttributeSet.size() > 0)
            {
            	sql += getAttributeSql(jobAttributeSet);
            }
			
			sql += getSqlExpression("tuv", createdAfter, createdBefore);

			conn = SqlUtil.hireConnection();
			stmt = conn.createStatement();
			rset = stmt.executeQuery(sql);

			if (rset.next()) {
				result = rset.getInt(1);
			}
		} 
		catch (/* SQL */Exception e) 
		{
			try 
			{
				conn.rollback();
			} 
			catch (Throwable ignore) 
			{}
			CATEGORY.warn("can't read TM data", e);

			throw e;
		} 
		finally 
		{
			try 
			{
				if (rset != null)
					rset.close();
				if (stmt != null)
					stmt.close();
			} 
			catch (Throwable ignore) 
			{}

			SqlUtil.fireConnection(conn);
		}

		return result;
	}
    
    /**
     * Gets the number of TUs that have a TUV in a given language.
     * 
     * @param p_locale
     *            a locale string like "en_US".
     */
	static List<Long> getFilteredTuIds(Tm tm, List<String> localeList,
			String createdAfter, String createdBefore) throws Exception
	{
        List<Long> result = new ArrayList<Long>();

        Connection conn = null;
        Statement stmt = null;
        ResultSet rset = null;

        try
        {
        	String localeIds = getLocaleIds(localeList);
            String sql = "SELECT DISTINCT tu.id "
                    + "FROM project_tm_tuv_t tuv, project_tm_tu_t tu "
                    + "WHERE tuv.locale_id in ( " + localeIds
                    + ")  AND tuv.tu_id = tu.id " + "  AND tu.tm_id = "
                    + tm.getId();
            sql += getSqlExpression("tuv", createdAfter, createdBefore);
            sql += " ORDER by tu.id ASC";

            conn = SqlUtil.hireConnection();
            stmt = conn.createStatement();
            rset = stmt.executeQuery(sql);

            while (rset.next())
            {
                result.add(new Long(rset.getLong(1)));
            }
        }
        catch (/* SQL */Exception e)
        {
            CATEGORY.warn("can't read TM data", e);
            throw e;
        }
        finally
        {
            try
            {
                if (rset != null) rset.close();
                if (stmt != null) stmt.close();
            }
            catch (Throwable ignore)
            {
            }

            SqlUtil.fireConnection(conn);
        }

        return result;
    }

    static List<Long> getFilteredTuIds(Tm tm, List<String> localeList, String createdAfter,
            String createdBefore,Set<String> jobAttributeSet) throws Exception
    {
        List<Long> result = new ArrayList<Long>();

        Connection conn = null;
        Statement stmt = null;
        ResultSet rset = null;

        try
        {
        	String localeIds = getLocaleIds(localeList);
            String sql = "SELECT DISTINCT tu.id "
                    + "FROM project_tm_tuv_t tuv, project_tm_tu_t tu , project_tm_tu_t_prop prop "
                    + "WHERE tuv.locale_id in (" + localeIds
                    + " ) AND tuv.tu_id = tu.id " + "  AND tu.tm_id = "
                    + tm.getId();
            
            if(jobAttributeSet != null && jobAttributeSet.size() > 0)
            {
            	sql += getAttributeSql(jobAttributeSet);
            }
            
            sql += getSqlExpression("tuv", createdAfter, createdBefore);
            sql += " ORDER by tu.id ASC";

            conn = SqlUtil.hireConnection();
            stmt = conn.createStatement();
            rset = stmt.executeQuery(sql);

            while (rset.next())
            {
                result.add(new Long(rset.getLong(1)));
            }
        }
        catch (/* SQL */Exception e)
        {
            CATEGORY.warn("can't read TM data", e);
            throw e;
        }
        finally
        {
            try
            {
                if (rset != null) rset.close();
                if (stmt != null) stmt.close();
            }
            catch (Throwable ignore)
            {
            }

            SqlUtil.fireConnection(conn);
        }

        return result;
    }

	static List<Long> getFilteredTuIdsByParamMap(Connection conn, Tm tm,
			List<String> localeList, Map<String, String> paramMap)
			throws Exception
	{
		String localeIds = getLocaleIds(localeList);
		return getTuIdsByParamMap(conn, tm, paramMap, null, localeIds, null);
	}

	static List<Long> getFilteredTuIdsByParamMap(Connection conn, Tm tm,
			List<String> localeList, Map<String, String> paramMap,
			Set<String> jobAttributeSet) throws Exception
	{
		String localeIds = getLocaleIds(localeList);
		return getTuIdsByParamMap(conn, tm, paramMap, jobAttributeSet,
				localeIds, null);
	}

	static int getProjectTuCountByParamMap(Connection conn, Tm tm,
			String projectName, Map<String, String> paramMap) throws Exception
	{

		return getTuIdsByParamMap(conn, tm, paramMap, null, null, projectName)
				.size();
	}

	static int getProjectTuCountByParamMap(Connection conn, Tm tm,
			String projectName, Map<String, String> paramMap,
			Set<String> jobAttributeSet) throws Exception
	{
		return getTuIdsByParamMap(conn, tm, paramMap, jobAttributeSet, null,
				projectName).size();
	}
	
    static int getProjectTuCount(Tm tm, String projectName, String createdAfter,
            String createdBefore) throws Exception  
    {
        int result = 0;

        Connection conn = null;
        Statement stmt = null;
        ResultSet rset = null;
        try
        {
            String sql = "SELECT COUNT(DISTINCT tu.id) " +
                         "FROM project_tm_tuv_t tuv, project_tm_tu_t tu " +
                         "WHERE (" + buildCondition(projectName) +
                         ")  AND tuv.updated_by_project is NOT NULL  AND tuv.tu_id = tu.id " +
                         "  AND tu.tm_id = " + tm.getId();
            sql+=getSqlExpression("tuv", createdAfter, createdBefore);            

            conn = SqlUtil.hireConnection();
            stmt = conn.createStatement();
            rset = stmt.executeQuery(sql);

            if (rset.next())
            {
                result = rset.getInt(1);
            }
        }
        catch (/*SQL*/Exception e)
        {
            CATEGORY.warn("can't read TM data", e);
            throw e;
        }
        finally
        {
            try
            {
                if (rset != null) rset.close();
                if (stmt != null) stmt.close();
            }
            catch (Throwable ignore) {}

            SqlUtil.fireConnection(conn);
        }

        return result;
    }
    
    static int getProjectTuCount(Tm tm, String projectName, String createdAfter,
            String createdBefore,Set<String> jobAttributeSet) throws Exception  
    {
        int result = 0;

        Connection conn = null;
        Statement stmt = null;
        ResultSet rset = null;
        try
        {
            String sql = "SELECT COUNT(DISTINCT tu.id) " +
                         "FROM project_tm_tuv_t tuv, project_tm_tu_t tu , project_tm_tu_t_prop prop " +
                         "WHERE (" + buildCondition(projectName) +
                         ")  AND tuv.updated_by_project is NOT NULL  AND tuv.tu_id = tu.id " +
                         "  AND tu.tm_id = " + tm.getId();
            
            if(jobAttributeSet != null && jobAttributeSet.size() > 0)
            {
            	sql += getAttributeSql(jobAttributeSet);
            }
            
            sql+=getSqlExpression("tuv", createdAfter, createdBefore);            

            conn = SqlUtil.hireConnection();
            stmt = conn.createStatement();
            rset = stmt.executeQuery(sql);

            if (rset.next())
            {
                result = rset.getInt(1);
            }
        }
        catch (/*SQL*/Exception e)
        {
            CATEGORY.warn("can't read TM data", e);
            throw e;
        }
        finally
        {
            try
            {
                if (rset != null) rset.close();
                if (stmt != null) stmt.close();
            }
            catch (Throwable ignore) {}

            SqlUtil.fireConnection(conn);
        }

        return result;
    }
    
    static List<Long> getProjectNameTuIds(Tm tm, String propType,
            String createdAfter, String createdBefore) throws Exception 
    {
        List<Long> result = new ArrayList<Long>();

        Connection conn = null;
        Statement stmt = null;
        ResultSet rset = null;
        String propTypeCondition = buildCondition(propType);
        try
        {            
            String sql = "SELECT DISTINCT tu.id " +
            "FROM project_tm_tuv_t tuv, project_tm_tu_t tu " +
            "WHERE ("+buildCondition(propType)+
            ")  AND tuv.tu_id = tu.id " +
            "  AND tu.tm_id = " + tm.getId();
            sql+=getSqlExpression("tuv", createdAfter, createdBefore);
            sql+=" ORDER by tu.id ASC";
            
            conn = SqlUtil.hireConnection();
            stmt = conn.createStatement();
            rset = stmt.executeQuery(sql);

            while (rset.next())
            {
                result.add(new Long(rset.getLong(1)));
            }
        }
        catch (/*SQL*/Exception e)
        {
            CATEGORY.warn("can't read TM data", e);
            throw e;
        }
        finally
        {
            try
            {
                if (rset != null) rset.close();
                if (stmt != null) stmt.close();
            }
            catch (Throwable ignore) {}

            SqlUtil.fireConnection(conn);
        }

        return result;
    }
    
    static List<Long> getProjectNameTuIds(Tm tm, String propType,
            String createdAfter, String createdBefore, Set<String> jobAttributeSet) throws Exception 
    {
        List<Long> result = new ArrayList<Long>();

        Connection conn = null;
        Statement stmt = null;
        ResultSet rset = null;
        String propTypeCondition = buildCondition(propType);
        try
        {            
            String sql = "SELECT DISTINCT tu.id " +
            "FROM project_tm_tuv_t tuv, project_tm_tu_t tu , project_tm_tu_t_prop prop " +
            "WHERE ("+buildCondition(propType)+
            ")  AND tuv.tu_id = tu.id " +
            "  AND tu.tm_id = " + tm.getId();
            
            if(jobAttributeSet != null && jobAttributeSet.size() > 0)
            {
            	sql += getAttributeSql(jobAttributeSet);
            }
            
            sql+=getSqlExpression("tuv", createdAfter, createdBefore);
            sql+=" ORDER by tu.id ASC";
            
            conn = SqlUtil.hireConnection();
            stmt = conn.createStatement();
            rset = stmt.executeQuery(sql);

            while (rset.next())
            {
                result.add(new Long(rset.getLong(1)));
            }
        }
        catch (/*SQL*/Exception e)
        {
            CATEGORY.warn("can't read TM data", e);
            throw e;
        }
        finally
        {
            try
            {
                if (rset != null) rset.close();
                if (stmt != null) stmt.close();
            }
            catch (Throwable ignore) {}

            SqlUtil.fireConnection(conn);
        }

        return result;
    }
    
	static List<Long> getProjectNameTuIdsByParamMap(Connection conn, Tm tm,
			String projectName, Map<String, String> paramMap) throws Exception
	{
		return getTuIdsByParamMap(conn, tm, paramMap, null, null, projectName);
	}

	static List<Long> getProjectNameTuIdsByParamMap(Connection conn, Tm tm,
			String projectName, Map<String, String> paramMap,
			Set<String> jobAttributeSet) throws Exception
	{
		return getTuIdsByParamMap(conn, tm, paramMap, jobAttributeSet, null,
				projectName);
	}

    private static String buildCondition(String propType) 
    {
        if (propType==null || propType.trim().length() == 0)
        {
            return "";
        }
        String s = " tuv.updated_by_project = '";
        StringBuffer sb = new StringBuffer(s);
        String[] projects = propType.split(",");
        for(int i = 0; i < projects.length; i ++)
        {
            if( i == projects.length - 1)
            {
                sb.append(projects[i]).append("'");
            }
            else
            {
                sb.append(projects[i]).append("'").append(" or ").append(s);
            }
            
        }
        return sb.toString();
    }
    

    // For the exporting TM enhancement issue
    private static String getSqlExpression(String p_table, String createdAfter,
            String createdBefore)
    {
        StringBuffer result = new StringBuffer();

        if (StringUtils.isNotBlank(createdAfter))
        {
            createdAfter = format.format(new Date(createdAfter));
            result.append(" and ");
            result.append(p_table).append(".CREATION_DATE >= '");
            result.append(createdAfter).append("'");
        }
        if (StringUtils.isNotBlank(createdBefore))
        {
            createdBefore = format.format(new Date(createdBefore));
            result.append(" and ");
            result.append(p_table).append(".CREATION_DATE <= '");
            result.append(createdBefore).append("'");
        }

        return result.toString();

    }
    
	private static String getSqlByParamMap(String p_table,
			Map<String, String> paramMap)
	{
		StringBuffer result = new StringBuffer();
		String createUser = paramMap.get("createUser");
		String modifyUser = paramMap.get("modifyUser");
		String modifyAfter = paramMap.get("modifyAfter");
		String modifyBefore = paramMap.get("modifyBefore");
		String createdAfter = paramMap.get("createdAfter");
		String createdBefore = paramMap.get("createdBefore");
		String tuIds = paramMap.get("tuIds");
		String stringId = paramMap.get("stringId");

		if (StringUtils.isNotBlank(createdAfter))
		{
			createdAfter = format.format(new Date(createdAfter));
			result.append(" and ");
			result.append(p_table).append(".CREATION_DATE >= '");
			result.append(createdAfter).append("'");
		}
		if (StringUtils.isNotBlank(createdBefore))
		{
			createdBefore = format.format(new Date(createdBefore));
			result.append(" and ");
			result.append(p_table).append(".CREATION_DATE <= '");
			result.append(createdBefore).append("'");
		}

		if (StringUtils.isNotBlank(modifyAfter))
		{
			modifyAfter = format.format(new Date(modifyAfter));
			result.append(" and ");
			result.append(p_table).append(".MODIFY_DATE >= '");
			result.append(modifyAfter).append("'");
		}
		if (StringUtils.isNotBlank(modifyBefore))
		{
			modifyBefore = format.format(new Date(modifyBefore));
			result.append(" and ");
			result.append(p_table).append(".MODIFY_DATE <= '");
			result.append(modifyBefore).append("'");
		}

		if (StringUtils.isNotBlank(createUser))
		{
			result.append(" and ");
			result.append(p_table).append(".CREATION_USER = '");
			result.append(createUser).append("'");
		}
		if (StringUtils.isNotBlank(modifyUser))
		{
			result.append(" and ");
			result.append(p_table).append(".MODIFY_USER = '");
			result.append(modifyUser).append("'");
		}

		if (StringUtils.isNotBlank(tuIds))
		{
			result.append(getTuIds(p_table, tuIds));
		}

		if (StringUtils.isNotBlank(stringId))
		{
			result.append(" and ");
			result.append(p_table).append(".SID IS NOT NULL");
		}

		return result.toString();

	}

    private static String getLocaleIds(List<String> localeList) throws Exception{
    	String localeIds = "";
    	if (localeList != null && localeList.size() > 0)
		{
			for (int i = 0; i < localeList.size(); i++)
			{
				localeIds += ExportUtil.getLocaleId(localeList.get(i))
						+ ",";
			}
		}
		if (StringUtil.isNotEmpty(localeIds) && localeIds.endsWith(","))
		{
			localeIds = localeIds.substring(0, localeIds.lastIndexOf(","));
		}
    	return localeIds;
    }

	private static String getTuIds(String p_table, String tuIds)
	{
		StringBuffer sqlBuffer = new StringBuffer();
		String tuIdStr = "";
		String[] tuIdsArr = tuIds.split(",");
		int count = 0;
		sqlBuffer.append(" AND (");
		for (String tuId : tuIdsArr)
		{
			if (tuId.contains("-"))
			{
				String[] tuIdArr = tuId.split("-");
				if (count == 0)
				{
					sqlBuffer.append(p_table).append(".TU_ID >=  ")
							.append(tuIdArr[0]).append(" AND ").append(p_table)
							.append(".TU_ID <=  ").append(tuIdArr[1]);
				}
				else
				{
					sqlBuffer.append(" OR ").append(p_table)
							.append(".TU_ID >= ").append(tuIdArr[0])
							.append(" AND ").append(p_table)
							.append(".TU_ID <= ").append(tuIdArr[1]);
				}
				count++;
			}
			else
			{
				tuIdStr += tuId + ",";
			}
		}
		if (StringUtil.isNotEmpty(tuIdStr))
		{
			if (count == 0)
			{
				sqlBuffer.append(p_table).append(".TU_ID in (")
						.append(tuIdStr.substring(0, tuIdStr.lastIndexOf(",")))
						.append(") ");
			}
			else
			{
				sqlBuffer.append(" OR ").append(p_table).append(".TU_ID in (")
						.append(tuIdStr.substring(0, tuIdStr.lastIndexOf(",")))
						.append(") ");
			}
		}
		sqlBuffer.append(") ");

		return sqlBuffer.toString();
	}

	private static List<Long> getMapWithSID(Connection conn, String sql,
			String stringId, String isRegex) throws SQLException
	{
		List<Long> tuIdList = new ArrayList<Long>();
		Map<Long, String> map = new HashMap<Long, String>();
		Statement stmt = conn.createStatement();
		ResultSet rset = stmt.executeQuery(sql);

		while (rset.next())
		{
			map.put(rset.getLong(1), rset.getString(2));
		}
		stmt.close();
		rset.close();

		boolean regex = Boolean.parseBoolean(isRegex);
		Pattern pattern = Pattern.compile(stringId);
		Matcher matcher = null;
		Iterator it = map.keySet().iterator();
		while (it.hasNext())
		{
			long key = Long.valueOf(String.valueOf(it.next()));
			String sid = map.get(key);
			if (regex)
			{
				matcher = pattern.matcher(sid);
				if (matcher.matches())
				{
					tuIdList.add(key);
				}
			}
			else
			{
				if (stringId.equalsIgnoreCase(sid))
				{
					tuIdList.add(key);
				}
			}
		}

		return tuIdList;
	}

	private static List<Long> getIdList(Connection conn, String sql)
			throws SQLException
	{
		List<Long> idList = new ArrayList<Long>();
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(sql);
		while (rs.next())
		{
			idList.add(rs.getLong(1));
		}
		stmt.close();
		rs.close();

		return idList;
	}
}
