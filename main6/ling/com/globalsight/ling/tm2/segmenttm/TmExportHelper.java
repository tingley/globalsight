package com.globalsight.ling.tm2.segmenttm;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.globalsight.everest.tm.Tm;
import com.globalsight.everest.tm.exporter.ExportUtil;
import com.globalsight.everest.tm.exporter.ReaderThread;
import com.globalsight.terminology.util.SqlUtil;

/**
 * Contains legacy code moved from 
 *      com.globalsight.everest.tm.exporter.ReaderThread
 *      
 * To fetch TUs from TM2 TMs for export.
 */
public class TmExportHelper {
    static private final Logger CATEGORY = Logger
                .getLogger(TmExportHelper.class);
    
    /**
     * Get the count of all TUs in the TM.
     */
    static int getAllTuCount(Connection conn, Tm tm, String createdAfter, String createdBefore)
            throws SQLException
    {
        int result = 0;

        Statement stmt = null;
        ResultSet rset = null;

        try
        {
            String sql = "SELECT COUNT(DISTINCT tu.id) "
                    + "FROM project_tm_tuv_t tuv, project_tm_tu_t tu "
                    + "  WHERE tu.id = tuv.tu_id and tu.tm_id = "
                    + tm.getId();
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

    /**
     * Gets the count of TUs that have a TUV in a given language.
     */
    static int getFilteredTuCount(Tm tm, String p_locale, String createdAfter,
                                 String createdBefore) throws Exception
     {
        int result = 0;

        long localeId = 0;

        Connection conn = null;
        Statement stmt = null;
        ResultSet rset = null;

        try
        {
            localeId = ExportUtil.getLocaleId(p_locale);
            String sql = "SELECT COUNT(DISTINCT tu.id) "
                    + "FROM project_tm_tuv_t tuv, project_tm_tu_t tu "
                    + "WHERE tuv.locale_id = " + localeId
                    + "  AND tuv.tu_id = tu.id " + "  AND tu.tm_id = "
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
    
    /**
     * Gets the number of TUs that have a TUV in a given language.
     * 
     * @param p_locale
     *            a locale string like "en_US".
     */
    static List<Long> getFilteredTuIds(Tm tm, String p_locale, String createdAfter,
            String createdBefore) throws Exception
    {
        List<Long> result = new ArrayList<Long>();

        long localeId = 0;

        Connection conn = null;
        Statement stmt = null;
        ResultSet rset = null;

        try
        {
            localeId = ExportUtil.getLocaleId(p_locale);
            String sql = "SELECT DISTINCT tu.id "
                    + "FROM project_tm_tuv_t tuv, project_tm_tu_t tu "
                    + "WHERE tuv.locale_id = " + localeId
                    + "  AND tuv.tu_id = tu.id " + "  AND tu.tm_id = "
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
        if (isSet(createdAfter))
        {
            result.append(" and ");
            result.append(p_table).append(".CREATION_DATE >= '");
            result.append(createdAfter).append("'");
        }
        if (isSet(createdBefore))
        {
            result.append(" and ");
            result.append(p_table).append(".CREATION_DATE <= '");
            result.append(createdBefore).append("'");
        }

        return result.toString();

    }


    //
    // Database Helpers
    //


    private static boolean isSet(String p_arg)
    {
        if (p_arg != null && p_arg.length() > 0)
        {
            return true;
        }

        return false;
    }

}
