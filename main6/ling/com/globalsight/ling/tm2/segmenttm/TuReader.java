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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.globalsight.diplomat.util.database.DbAccessor;
import com.globalsight.everest.projecthandler.ProjectTmTuTProp;
import com.globalsight.everest.tm.exporter.ExportUtil;
import com.globalsight.ling.tm2.SegmentTmTu;
import com.globalsight.ling.tm2.SegmentTmTuv;

/**
 * Allows reading of SegmentTmTus from a TM. The caller should retrieve a list
 * of TU ids to read and then use this class to read a subset of TUs into
 * memory. Any data paging should be implemented using the ID list (maye later
 * we implement paging in this class).
 * 
 * Caller must call methods in the following sequence:
 * 
 * 1) batchReadTus() 2) getNextTu() repeatedly 3) batchReadDone()
 * 
 * The class is reusable in that the above 3 steps can be repeated without
 * allocating a new object.
 * 
 * This class should be moved to the ling.tm2 package after that package, and
 * integration, is complete.
 */
public class TuReader
{
    private Connection m_conn = null;
    private Statement m_stmtTus = null;
    private Statement m_stmtTuvs = null;
    private Statement m_stmtTuProps = null;
    private ResultSet m_rsetTus = null;
    private ResultSet m_rsetTuvs = null;
    private ResultSet m_rsetTuProps = null;

    //
    // Constructor
    //

    /**
     * Legacy signature still called from TuQueryResult by the UI threads that
     * are building tables.
     */
    @Deprecated
    public TuReader(Connection p_conn)
    {
        m_conn = p_conn;
    }

    /**
     * Reads TUs and TUVs from the TM. The result sets are kept as member
     * variables of this object while data is read.
     * 
     * Caller must call batchReadDone() at the end to free allocated resources.
     * 
     * @param p_tuIds
     *            is a list of TU ids as Long objects.
     * @param p_min
     *            is the start index to retrieve (inclusive)
     * @param p_max
     *            is the end index to retrieve (exclusive)
     * @param p_locales
     *            is a set of locale ids as Long objects for which TUVs should
     *            be retrieved. This argument can be null, or empty, which means
     *            all TUVs will be retrieved.
     * 
     * @see #batchReadDone()
     */
    public void batchReadTus(List<Long> p_tuIds, int p_min, int p_max,
            ArrayList p_locales) throws SQLException
    {
        m_stmtTus = m_conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY);
        m_stmtTuvs = m_conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY);
        m_stmtTuProps = m_conn.createStatement(
                ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

        String tuids = printIds(p_tuIds, p_min, p_max);

        m_rsetTus = m_stmtTus
                .executeQuery("SELECT id, tm_id, format, type, source_locale_id, source_tm_name "
                        + "FROM project_tm_tu_t "
                        + "WHERE id IN ("
                        + tuids
                        + ") ");

        String locales = "";

        if (p_locales != null && p_locales.size() > 0)
        {
            locales = " AND locale_id IN ("
                    + printIds(p_locales, 0, p_locales.size()) + ") ";
        }

        m_rsetTuvs = m_stmtTuvs
                .executeQuery("SELECT id, tu_id, segment_string, segment_clob, "
                        + "       exact_match_key, locale_id, "
                        + "       creation_date, creation_user, "
                        + "       modify_date, modify_user, updated_by_project, sid "
                        + "FROM project_tm_tuv_t "
                        + "WHERE tu_id IN ("
                        + tuids
                        + ") " + locales
                /* and tm_id = ? */
                );

        m_rsetTuProps = m_stmtTuProps
                .executeQuery("SELECT id, tu_id, prop_type, prop_value "
                        + "FROM project_tm_tu_t_prop " + "WHERE tu_id IN ("
                        + tuids + ") ");
    }

    /**
     * Reads the next TU and its TUVs from the result sets.
     * 
     * @return null if no next result is available.
     */
    public SegmentTmTu getNextTu() throws SQLException, Exception
    {
        SegmentTmTu result = null;
        SegmentTmTuv tuv;

        // Assuming TUs and TUVs come back in the same order :)
        if (m_rsetTus.next())
        {
            result = new SegmentTmTu(m_rsetTus.getLong(1),
                    m_rsetTus.getLong(2), m_rsetTus.getString(3),
                    m_rsetTus.getString(4), true, // translatable
                    ExportUtil.getLocaleById(m_rsetTus.getLong(5)));

            result.setSourceTmName(m_rsetTus.getString(6));

            while (m_rsetTuvs.next())
            {
                long tu_id = m_rsetTuvs.getLong(2);

                if (tu_id == result.getId())
                {
                    tuv = new SegmentTmTuv();

                    tuv.setId(m_rsetTuvs.getLong(1));
                    tuv.setTu(result);

                    String segment = m_rsetTuvs.getString(3);
                    if (segment == null)
                    {
                        segment = DbAccessor.readClob(m_rsetTuvs, 4);
                    }

                    tuv.setSegment(segment);

                    tuv.setExactMatchKey(m_rsetTuvs.getLong(5));
                    tuv.setLocale(ExportUtil.getLocaleById(m_rsetTuvs
                            .getLong(6)));

                    tuv.setCreationDate(m_rsetTuvs.getTimestamp(7));
                    tuv.setCreationUser(m_rsetTuvs.getString(8));
                    tuv.setModifyDate(m_rsetTuvs.getTimestamp(9));
                    tuv.setModifyUser(m_rsetTuvs.getString(10));

                    tuv.setUpdatedProject(m_rsetTuvs.getString(11));
                    tuv.setSid(m_rsetTuvs.getString(12));

                    result.addTuv(tuv);
                }
            }
            m_rsetTuvs.absolute(1);

            while (m_rsetTuProps.next())
            {
                long tu_id = m_rsetTuProps.getLong(2);

                if (tu_id == result.getId())
                {
                    ProjectTmTuTProp prop = new ProjectTmTuTProp();
                    prop.setId(m_rsetTuProps.getLong(1));
                    prop.setPropType(m_rsetTuProps.getString(3));
                    prop.setPropValue(m_rsetTuProps.getString(4));
                    result.addProp(prop);
                }
            }
            m_rsetTuProps.absolute(1);

        }

        return result;
    }

    /**
     * Ends reading data from the DB by closing all SQL resources.
     */
    public void batchReadDone()
    {
        if (m_rsetTus != null)
        {
            try
            {
                m_rsetTus.close();
            }
            catch (Throwable ignore)
            {
            }
            finally
            {
                m_rsetTus = null;
            }
        }

        if (m_rsetTuvs != null)
        {
            try
            {
                m_rsetTuvs.close();
            }
            catch (Throwable ignore)
            {
            }
            finally
            {
                m_rsetTuvs = null;
            }
        }

        if (m_stmtTus != null)
        {
            try
            {
                m_stmtTus.close();
            }
            catch (Exception e)
            {
            }
        }

        if (m_stmtTuvs != null)
        {
            try
            {
                m_stmtTuvs.close();
            }
            catch (Exception e)
            {
            }
        }
    }

    //
    // Private Methods
    //

    /** Prints a list of IDs for a SQL IN clause. */
    private String printIds(List<Long> p_ids, int p_min, int p_max)
    {
        StringBuffer result = new StringBuffer();

        for (int i = p_min; i < p_max; i++)
        {
            Long id = (Long) p_ids.get(i);

            result.append(id);

            if (i < p_max - 1)
            {
                result.append(',');
            }
        }

        return result.toString();
    }
}
