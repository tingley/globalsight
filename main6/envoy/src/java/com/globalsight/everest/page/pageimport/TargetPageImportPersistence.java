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

package com.globalsight.everest.page.pageimport;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.globalsight.cxe.util.XmlUtil;
import com.globalsight.cxe.util.fileImport.eventFlow.EventFlowXml;
import com.globalsight.everest.comment.IssueImpl;
import com.globalsight.everest.page.ExtractedFile;
import com.globalsight.everest.page.PageException;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.persistence.tuv.SegmentTuTuvPersistence;
import com.globalsight.everest.persistence.tuv.SegmentTuvUtil;
import com.globalsight.everest.tuv.LeverageGroup;
import com.globalsight.everest.tuv.Tu;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.ling.common.srccomment.SourceComment;
import com.globalsight.ling.tm.ExactMatchedSegments;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.terminology.termleverager.TermLeverageResult;
import com.globalsight.util.GlobalSightLocale;

public class TargetPageImportPersistence extends AbstractTargetPagePersistence
        implements TargetPagePersistence
{
    private static Logger s_logger = Logger
            .getLogger(TargetPageImportPersistence.class);


    @Override
    public Collection<TargetPage> persistObjectsWithExtractedFile(
            SourcePage p_sourcePage, Collection p_targetLocales,
            TermLeverageResult p_termMatches, boolean p_useLeveragedSegments,
            boolean p_useLeveragedTerms,
            ExactMatchedSegments p_exactMatchedSegments) throws PageException
    {
        long jobId = p_sourcePage.getJobId();

        List<TargetPage> targetPages = new ArrayList<TargetPage>();
        List<LeverageGroup> levertages = p_sourcePage.getExtractedFile()
                .getLeverageGroupSet();

        Session session = null;
        Transaction transaction = null;

        try
        {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();
            // Generate target pages
            for (Iterator it = p_targetLocales.iterator(); it.hasNext();)
            {
                GlobalSightLocale targetLocale = (GlobalSightLocale) it.next();
                TargetPage targetPage = new TargetPage(targetLocale,
                        p_sourcePage);
                targetPage.setTimestamp(new Timestamp(System
                        .currentTimeMillis()));
                targetPage.getExtractedFile().getLeverageGroupSet()
                        .addAll(levertages);

                session.save(targetPage);
                targetPages.add(targetPage);
            }

            // Create target TUVs for all target locales
            long time_PERFORMANCE = System.currentTimeMillis();

            Set<Tuv> tuvs = createPersistenceTuv(p_sourcePage, p_targetLocales,
                    p_termMatches, p_useLeveragedSegments, p_useLeveragedTerms,
                    p_exactMatchedSegments);

            if (s_logger.isDebugEnabled())
            {
                s_logger.debug("Performance:: createPersistenceTuv for "
                        + p_sourcePage.getExternalPageId() + " time = "
                        + (System.currentTimeMillis() - time_PERFORMANCE));

                s_logger.debug("Persisting target pages for a source page with an extracted file.");
            }

            time_PERFORMANCE = System.currentTimeMillis();

            // Save all target TUVs
            tuvs = SegmentTuTuvPersistence.saveTargetTuvs(tuvs, jobId);

            // Add source comment
            String jobUid = getJobUid(p_sourcePage);
            for (Iterator iterator = tuvs.iterator(); iterator.hasNext();)
            {
                Tuv tuv = (Tuv) iterator.next();
                if (tuv.getSrcComment() != null)
                {
                    Tu tu = tuv.getTu(jobId);
                    TargetPage targetPage = null;
                    for (TargetPage tPage : targetPages)
                    {
                        if (tPage.getGlobalSightLocale().getId() == tuv
                                .getLocaleId())
                        {
                            targetPage = tPage;
                            break;
                        }
                    }

                    IssueImpl issue = SourceComment.createSourceComment(
                            targetPage, tu, tuv, jobUid);
                    session.save(issue);
                }
            }

            if (s_logger.isDebugEnabled())
            {
                s_logger.debug("Performance:: Persists target pages for "
                        + p_sourcePage.getExternalPageId() + " time = "
                        + (System.currentTimeMillis() - time_PERFORMANCE));
            }

            HibernateUtil.commit(transaction);

            // It is for GBS-3454 and only available for IPTranslator.
            updateMtEngineWordCount(p_sourcePage, targetPages);
        }
        catch (Exception e)
        {
            HibernateUtil.rollback(transaction);

            s_logger.error("The target page could not be persisted", e);
            throw new PageException(e);
        }

        return targetPages;
    }

    private Set<Tuv> createPersistenceTuv(SourcePage p_sourcePage,
            Collection p_targetLocales, TermLeverageResult p_termMatches,
            boolean p_useLeveragedSegments, boolean p_useLeveragedTerms,
            ExactMatchedSegments p_exactMatchedSegments) throws PageException
    {
        Set<Tuv> tuvs = new HashSet<Tuv>();

        try
        {
            HashMap<Tu, Tuv> sourceTuvMap = getSourceTuvMap(p_sourcePage);

            for (Iterator it = p_targetLocales.iterator(); it.hasNext();)
            {
                GlobalSightLocale targetLocale = (GlobalSightLocale) it.next();

                ArrayList<Tuv> targetTuvs = getTargetTuvs(p_sourcePage,
                        sourceTuvMap, targetLocale, p_termMatches,
                        p_useLeveragedTerms, p_exactMatchedSegments);
                tuvs.addAll(targetTuvs);
            }
        }
        catch (PageException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new PageException(e);
        }

        return tuvs;
    }

    private HashMap<Tu, Tuv> getSourceTuvMap(SourcePage p_sourcePage)
    {
        long jobId = p_sourcePage.getJobId();

        HashMap<Tu, Tuv> result = new HashMap<Tu, Tuv>();

        // Assume this page contains an extracted file, otherwise
        // wouldn't have reached this place in the code.
        Iterator<LeverageGroup> it1 = getExtractedFile(p_sourcePage)
                .getLeverageGroups().iterator();
        List<Tuv> srcTuvList = new ArrayList<Tuv>();
        while (it1.hasNext())
        {
            LeverageGroup leverageGroup = it1.next();
            Collection<Tu> tus = leverageGroup.getTus();

            for (Iterator<Tu> it2 = tus.iterator(); it2.hasNext();)
            {
                Tu tu = it2.next();
                Tuv tuv = tu.getTuv(p_sourcePage.getLocaleId(), jobId);
                srcTuvList.add(tuv);
                result.put(tu, tuv);
            }
        }

        SegmentTuvUtil.setHashValues(srcTuvList);

        return result;
    }

    /**
     * Returns the page's Extracted Primary File or NULL if it doesn't contain
     * an Extracted file.
     */
    private ExtractedFile getExtractedFile(SourcePage p_page)
    {
        ExtractedFile result = null;

        if (p_page.getPrimaryFileType() == ExtractedFile.EXTRACTED_FILE)
        {
            result = (ExtractedFile) p_page.getPrimaryFile();
        }

        return result;
    }

    private String getJobUid(SourcePage p_sourcePage)
    {
        String jobUid = null;
        try
        {
            String eventflowXml = p_sourcePage.getRequest().getEventFlowXml();
            EventFlowXml eventFlowXml = XmlUtil.string2Object(EventFlowXml.class, eventflowXml);
            jobUid = eventFlowXml.getSource().getImportInitiatorId();
        }
        catch (Exception e)
        {
            jobUid = null;
        }

        return jobUid;
    }

}
