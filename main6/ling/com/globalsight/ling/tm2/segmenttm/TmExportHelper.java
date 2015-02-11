package com.globalsight.ling.tm2.segmenttm;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

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
    
    static int getAllTuCount(Connection conn, Tm tm, String createdAfter, String createdBefore,Set<String> attributeSet)
    		throws SQLException
	{
		int result = getAllCount(conn, tm, createdAfter, createdBefore, "TU", attributeSet);
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
}
