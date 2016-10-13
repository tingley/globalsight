/**
 *  Copyright 2009 Welocalize, Inc. 
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  
 *  You may obtain a copy of the License at 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */
package com.globalsight.ling.tm2.segmenttm;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.tm.Tm;
import com.globalsight.everest.tm.TmRemover;
import com.globalsight.ling.tm2.lucene.LuceneIndexWriter;

/**
 * This contains a lot of code that used to live in
 * com.globalsight.everest.tm.TmRemover. It handles bulk removal of TM2 segment
 * tms.
 */
public class TmRemoveHelper
{
    static private final Logger c_logger = Logger.getLogger(TmRemover.class);

    static class Query
    {
        static final String REMOVE_TU_T = "delete from project_tm_tu_t where tm_id = ?";

        static final String REMOVE_TU_L = "delete from project_tm_tu_l where tm_id = ?";

        static final String REMOVE_TUV_T = "delete project_tm_tuv_t from project_tm_tuv_t, project_tm_tu_t "
                + "where project_tm_tu_t.id = project_tm_tuv_t.tu_id "
                + "and project_tm_tu_t.tm_id = ?";

        static final String REMOVE_TUV_T_BY_LANGUAGE = "delete project_tm_tuv_t from project_tm_tuv_t, project_tm_tu_t "
                + "where project_tm_tu_t.id = project_tm_tuv_t.tu_id "
                + "and project_tm_tu_t.tm_id= ? "
                + "and project_tm_tuv_t.locale_id = ?";

        static final String REMOVE_TUV_L = "delete project_tm_tuv_l from project_tm_tuv_l, project_tm_tu_l "
                + "where project_tm_tu_l.id = project_tm_tuv_l.tu_id "
                + "and project_tm_tu_l.tm_id = ?";

        static final String REMOVE_TUV_L_BY_LANGUAGE = "delete project_tm_tuv_l from project_tm_tuv_l, project_tm_tu_l "
                + "where project_tm_tu_l.id = project_tm_tuv_l.tu_id "
                + "and project_tm_tu_l.tm_id = ? "
                + "and project_tm_tuv_l.locale_id = ? ";
    }

    public static void removeTm(Tm tm) throws Exception
    {
        String tmName = tm.getName();
        ServerProxy.getProjectHandler().removeProjectTm(tm.getId());
        c_logger.info("Removed Tm: " + tmName);
    }

    static void removeDataByLocale(Connection connection, long tmId,
            long localeId, String p_sqlString) throws Exception
    {
        PreparedStatement ps = null;

        try
        {
            ps = connection.prepareStatement(p_sqlString);
            ps.setLong(1, tmId);
            ps.setLong(2, localeId);

            int num = ps.executeUpdate();

            c_logger.info("Removed " + num + " rows from a Tm table.");
        }
        finally
        {
            if (ps != null)
            {
                try
                {
                    ps.close();
                }
                catch (SQLException ignore)
                {
                }
            }
        }
    }

    static void removeData(Connection connection, long tmId, String p_sqlString)
            throws Exception
    {
        PreparedStatement ps = null;

        try
        {
            ps = connection.prepareStatement(p_sqlString);
            ps.setLong(1, tmId);
            int num = ps.executeUpdate();

            c_logger.info("Removed " + num + " rows from a Tm table.");
        }
        finally
        {
            if (ps != null)
            {
                try
                {
                    ps.close();
                }
                catch (SQLException ignore)
                {
                }
            }
        }
    }

    static void removeIndex(long tmId) throws Exception
    {
        LuceneIndexWriter.removeTm(tmId);
        c_logger.info("Removed Tm index.");
    }

}
