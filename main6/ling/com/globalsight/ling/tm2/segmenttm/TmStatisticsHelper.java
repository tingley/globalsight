package com.globalsight.ling.tm2.segmenttm;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.globalsight.everest.localemgr.LocaleManager;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.tm.StatisticsInfo;
import com.globalsight.everest.tm.Tm;
import com.globalsight.terminology.util.SqlUtil;
import com.globalsight.util.GlobalSightLocale;

/**
 * TM2 statistics-gathering code, originally from
 *  com.globalsight.everest.tm.TmManagerLocal.doGetProjectTmStatistics()
 * The project info section (p_includeProjects) is originally from 
 *  com.globalsight.everest.tm.TmManagerLocal.getProjects()
 */
class TmStatisticsHelper {
    private static final Logger CATEGORY =
        Logger.getLogger(
            TmStatisticsHelper.class);
    
    /**
     * Get statistics for a TM2 project TM.
     * @param conn JDBC connection.  This should already be active.  
     * @param tm TM to get statistics for.
     * @param p_uiLocale UI locale
     * @return TM statistics
     */
    static StatisticsInfo getStatistics(Connection conn, Tm tm, Locale p_uiLocale,
            boolean p_includeProjects) {
        
        StatisticsInfo result = new StatisticsInfo();

        int tus = 0, tuvs = 0;

        Statement stmt = null;
        ResultSet rset = null;

        try
        {
            stmt = conn.createStatement();
            rset = stmt.executeQuery(
                "SELECT COUNT(DISTINCT tu.id) FROM project_tm_tu_t tu,project_tm_tuv_t tuv " +
                "WHERE tuv.tu_id = tu.id and tu.tm_id = " + tm.getId());

            if (rset.next())
            {
                tus = rset.getInt(1);
            }

            rset = stmt.executeQuery(
                "SELECT COUNT(DISTINCT tuv.id) " +
                "FROM project_tm_tuv_t tuv, project_tm_tu_t tu " +
                "WHERE tuv.tu_id = tu.id " +
                "  AND tu.tm_id = " + tm.getId());

            if (rset.next())
            {
                tuvs = rset.getInt(1);
            }
        }
        catch (Exception e)
        {
            CATEGORY.error("can't read TM data", e);
        }
        finally
        {
            try
            {
                if (rset != null) rset.close();
                if (stmt != null) stmt.close();
            }
            catch (Throwable ignore) {}

        }

        result.setTm(tm.getName());
        result.setTUs(tus);
        result.setTUVs(tuvs);

        try
        {
            stmt = conn.createStatement();

            rset = stmt.executeQuery(
                "SELECT tuv.locale_id, " +
                "COUNT(DISTINCT tuv.tu_id), COUNT(DISTINCT tuv.id) " +
                "FROM project_tm_tuv_t tuv, project_tm_tu_t tu " +
                "WHERE tu.tm_id = " + tm.getId() + " " +
                "AND tu.id = tuv.tu_id " +
                "GROUP BY tuv.locale_id");

            LocaleManager mgr = ServerProxy.getLocaleManager();
            while (rset.next())
            {
                long localeId = rset.getLong(1);
                tus = rset.getInt(2);
                tuvs = rset.getInt(3);

                GlobalSightLocale locale = mgr.getLocaleById(localeId);

                result.addLanguageInfo(localeId, locale.getLocale(),
                    locale.getLocale().getDisplayName(p_uiLocale), tus, tuvs);
            }
        }
        catch (Exception e)
        {
            CATEGORY.error("can't read TM data", e);
        }
        finally
        {
            try
            {
                if (rset != null) rset.close();
                if (stmt != null) stmt.close();
            }
            catch (Throwable ignore) {}

        }

        if (p_includeProjects)
        {
            try
            {
                conn = SqlUtil.hireConnection();
                stmt = conn.createStatement();

                rset = stmt.executeQuery(
                    "SELECT tuv.updated_by_project, " +
                    "COUNT(DISTINCT tuv.tu_id), COUNT(DISTINCT tuv.id) " +
                    "FROM project_tm_tuv_t tuv, project_tm_tu_t tu " +
                    "WHERE tu.tm_id = " + tm.getId() + " " +
                    "AND tuv.updated_by_project is NOT NULL  " +
                    "AND tu.id = tuv.tu_id group by tuv.updated_by_project");
                
                while (rset.next())
                {
                    String project = rset.getString(1);
                    tus = rset.getInt(2);
                    tuvs = rset.getInt(3);
                    result.addUpdateProjectInfo(project, tus, tuvs);
                }
            }
            catch (Exception e)
            {
                CATEGORY.warn("can't read TM data", e);
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
                    CATEGORY.error(ignore.getMessage(), ignore);
                }

                SqlUtil.fireConnection(conn);
            }
        }
        
        return result;
    }
}
