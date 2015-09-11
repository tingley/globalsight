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
package com.globalsight.everest.persistence.tuv;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.tuv.LeverageGroup;
import com.globalsight.everest.tuv.Tu;
import com.globalsight.everest.tuv.TuImpl;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.tuv.TuvImpl;
import com.globalsight.ling.docproc.extractor.xliff.XliffAlt;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.persistence.hibernate.HibernateUtil;

/**
 * Helper for maintain all data TU/TUV related instead of Hibernate
 * implementation.
 *
 * @author york.jin
 * @since 2012-03-22
 * @version 8.2.3
 */
public class SegmentTuTuvPersistence
{
    static private final Logger logger = Logger
            .getLogger(SegmentTuTuvPersistence.class);

    public SegmentTuTuvPersistence()
    {

    }

    /**
     * Save TUs, source TUVs and their related data when import to create job.
     * For now, there are only TUs, source TUVs in source page, no target TUVs.
     *
     * @param p_sourcePage
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static SourcePage saveTuTuvAndRelatedData(SourcePage p_sourcePage,
            long p_jobId) throws Exception
    {
        Connection conn = null;

        try
        {
            conn = DbUtil.getConnection();
            boolean autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            List<LeverageGroup> lgList = p_sourcePage.getExtractedFile()
                    .getLeverageGroups();
            for (LeverageGroup lg : lgList)
            {
                Collection<Tu> tus = lg.getTus(false);
                long lgId = lg.getLeverageGroupId();
                List<Tuv> allTuvs = new ArrayList<Tuv>();
                for (Iterator<Tu> tuIter = tus.iterator(); tuIter.hasNext();)
                {
                    TuImpl tu = (TuImpl) tuIter.next();
                    tu.setLeverageGroupId(lgId);
                    allTuvs.addAll(tu.getTuvs(false, p_jobId));
                }

                // Save TU data (translation_unit_XX)
                SegmentTuUtil.saveTus(conn, tus, p_jobId);

                // Save "removed_tag","removed_prefix_tag","removed_suffix_tag".
                RemovedTagsUtil.saveAllRemovedTags(tus);

                // Save TUV data
                SegmentTuvUtil.saveTuvs(conn, allTuvs, p_jobId);

                // Save XliffAlt data into source TUVs first
                List<XliffAlt> xlfAlts = new ArrayList<XliffAlt>();
                for (Tuv sourceTuv : allTuvs)
                {
                    if (sourceTuv.getXliffAlt(false) != null
                            && sourceTuv.getXliffAlt(false).size() > 0)
                    {
                        xlfAlts.addAll(sourceTuv.getXliffAlt(false));
                    }
                }
                HibernateUtil.save(xlfAlts);
            }
            conn.commit();
            conn.setAutoCommit(autoCommit);
        }
        catch (Exception ex)
        {
            conn.rollback();
            throw ex;
        }
        finally
        {
            DbUtil.silentReturnConnection(conn);
        }

        return p_sourcePage;
    }

    /**
     * Save target TUVs and related data when import to generate target TUVs.
     *
     * @param p_targetTuvs
     * @param p_jobId
     * @throws Exception
     */
    public static Set<Tuv> saveTargetTuvs(Set<Tuv> p_targetTuvs, long p_jobId)
            throws Exception
    {
        Connection conn = null;

        try
        {
            conn = DbUtil.getConnection();
            boolean autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            // Set tuvIds for all TUVs first
            SegmentTuTuvIndexUtil.setTuvIds(p_targetTuvs);

            // Set tuvIds into its own XliffAlt (for XLF based formats)
            for (Iterator tuvIter = p_targetTuvs.iterator(); tuvIter.hasNext();)
            {
                TuvImpl tuv = (TuvImpl) tuvIter.next();
                Set<XliffAlt> xlfAlts = tuv.getXliffAlt(false);
                if (xlfAlts != null && xlfAlts.size() > 0)
                {
                    for (Iterator altIter = xlfAlts.iterator(); altIter
                            .hasNext();)
                    {
                        XliffAlt alt = (XliffAlt) altIter.next();
                        alt.setTuvId(tuv.getId());
                    }
                }
            }

            // Save TUV data
            SegmentTuvUtil.saveTuvs(conn, p_targetTuvs, p_jobId);

            // Save XliffAlt & IssueEditionRelation
            Set<XliffAlt> xlfAlts = new HashSet<XliffAlt>();
            for (Iterator it = p_targetTuvs.iterator(); it.hasNext();)
            {
                TuvImpl targetTuv = (TuvImpl) it.next();
                if (targetTuv.getXliffAlt(false) != null)
                {
                    xlfAlts.addAll(targetTuv.getXliffAlt(false));
                }
            }
            HibernateUtil.save(xlfAlts);

            conn.commit();
            conn.setAutoCommit(autoCommit);

            return p_targetTuvs;
        }
        catch (Exception ex)
        {
            conn.rollback();
            throw ex;
        }
        finally
        {
            DbUtil.silentReturnConnection(conn);
        }
    }
}
