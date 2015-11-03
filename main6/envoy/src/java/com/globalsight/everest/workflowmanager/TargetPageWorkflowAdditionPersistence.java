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

package com.globalsight.everest.workflowmanager;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.globalsight.everest.comment.IssueImpl;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.page.PageException;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.page.pageimport.AbstractTargetPagePersistence;
import com.globalsight.everest.page.pageimport.ExtractedFileImporter;
import com.globalsight.everest.page.pageimport.TargetPagePersistence;
import com.globalsight.everest.persistence.tuv.SegmentTuTuvPersistence;
import com.globalsight.everest.persistence.tuv.SegmentTuUtil;
import com.globalsight.everest.persistence.tuv.SegmentTuvUtil;
import com.globalsight.everest.tuv.LeverageGroup;
import com.globalsight.everest.tuv.Tu;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.tuv.TuvState;
import com.globalsight.ling.common.srccomment.SourceComment;
import com.globalsight.ling.tm.ExactMatchedSegments;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.terminology.termleverager.TermLeverageResult;
import com.globalsight.util.GlobalSightLocale;

public class TargetPageWorkflowAdditionPersistence extends
        AbstractTargetPagePersistence implements TargetPagePersistence
{
    private static Logger s_logger = Logger
            .getLogger(TargetPageWorkflowAdditionPersistence.class);


    public Collection<TargetPage> persistObjectsWithExtractedFile(
            SourcePage p_sourcePage, Collection p_targetLocales,
            TermLeverageResult p_termMatches, boolean p_useLeveragedSegments,
            boolean p_useLeveragedTerms,
            ExactMatchedSegments p_exactMatchedSegments) throws PageException
    {
        ArrayList<TargetPage> targetPages = new ArrayList<TargetPage>();

        // I think Aswin wanted to say that the sequence numbers are
        // allocated inside a separate transaction and their values
        // will be lost if the any one of the calls fails. Hence the
        // separate try-catch statements.
        Session session = null;
        Transaction transaction = null;

        try
        {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            List<LeverageGroup> levertages = p_sourcePage.getExtractedFile()
                    .getLeverageGroupSet();

            // if it is a discard-add workflow, do not insert the tuvs.
            Job job = p_sourcePage.getRequest().getJob();
            long jobId = job.getId();
            Collection<Workflow> workflows = job.getWorkflows();
            Collection<GlobalSightLocale> allTargetLocales = new ArrayList<GlobalSightLocale>();
            for (Iterator it = p_targetLocales.iterator(); it.hasNext();)
            {
                GlobalSightLocale targetLocale = (GlobalSightLocale) it.next();
                allTargetLocales.add(targetLocale);
                for (Workflow wf : workflows)
                {
                    boolean breakFlag = false;
                    if (wf.getTargetLocale().equals(targetLocale))
                    {
                        Vector<TargetPage> tPages = wf.getTargetPages();
                        for (TargetPage page : tPages)
                        {
                            // Check if the page is existed.
                            if (page.getExternalPageId().equals(
                                    p_sourcePage.getExternalPageId()))
                            {
                                it.remove();
                                breakFlag = true;
                                break;
                            }
                        }

                        if (breakFlag)
                            break;
                    }
                }

                TargetPage targetPage = new TargetPage(targetLocale,
                        p_sourcePage);
                targetPage.getExtractedFile().getLeverageGroupSet()
                        .addAll(levertages);
                targetPage.setTimestamp(new Timestamp(System
                        .currentTimeMillis()));

                session.save(targetPage);
                targetPages.add(targetPage);
            }

            Collection sourceTuvs = SegmentTuvUtil.getSourceTuvs(p_sourcePage);
            for (Iterator<Tuv> it = sourceTuvs.iterator(); it.hasNext();)
            {
            	it.next().setState(TuvState.NOT_LOCALIZED);
            }
            SegmentTuUtil.getTusBySourcePageId(p_sourcePage.getId());
            HashMap<Tu, Tuv> sourceTuvMap = getSourceTuvMap(sourceTuvs, jobId);
            Set<Tuv> targetTuvs = createPersistenceTuv(p_sourcePage,
                    p_targetLocales, p_termMatches, p_useLeveragedSegments,
                    p_useLeveragedTerms, p_exactMatchedSegments, sourceTuvs,
                    sourceTuvMap);

            targetTuvs = SegmentTuTuvPersistence.saveTargetTuvs(targetTuvs,
                    jobId);

            // add target tuv source comment
            Iterator sourceTuvsIt = sourceTuvs.iterator();
            while (sourceTuvsIt.hasNext())
            {
                Object obj = sourceTuvsIt.next();
                Tuv srctuv = (Tuv) obj;
                if (srctuv == null)
                {
                    continue;
                }

                String srcComment = srctuv.getSrcComment();
                // add src comment
                if (srcComment != null)
                {
                    Tu tu = srctuv.getTu(jobId);

                    if (tu == null)
                    {
                        continue;
                    }

                    for (GlobalSightLocale targetLocale : allTargetLocales)
                    {
                        Tuv tgtTuv = tu.getTuv(targetLocale.getId(), jobId);

                        if (tgtTuv == null)
                        {
                            continue;
                        }

                        tgtTuv.setSrcComment(srcComment);

                        TargetPage tPage = null;
                        for (int i = 0; i < targetPages.size(); i++)
                        {
                            tPage = (TargetPage) targetPages.get(i);
                            if (tPage.getGlobalSightLocale().getId() == tgtTuv
                                    .getLocaleId())
                            {
                                break;
                            }
                        }

                        String jobUid = job == null ? null : job
                                .getCreateUserId();
                        IssueImpl issue = SourceComment.createSourceComment(
                                tPage, tu, tgtTuv, jobUid);
                        session.save(issue);
                    }
                }
            }

            transaction.commit();

            // It is for GBS-3454 and only available for IPTranslator.
            updateMtEngineWordCount(p_sourcePage, targetPages);
        }
        catch (Exception e)
        {
            if (transaction != null)
            {
                transaction.rollback();
            }

            s_logger.error("The target page could not be persisted", e);
            throw new PageException(e);
        }

        return targetPages;
    }

    private Set<Tuv> createPersistenceTuv(SourcePage p_sourcePage,
            Collection p_targetLocales, TermLeverageResult p_termMatches,
            boolean p_useLeveragedSegments, boolean p_useLeveragedTerms,
            ExactMatchedSegments p_exactMatchedSegments, Collection sourceTuvs,
            HashMap<Tu, Tuv> sourceTuvMap) throws PageException
    {
        Set<Tuv> tuvs = new HashSet<Tuv>();

        try
        {
            for (Iterator it = p_targetLocales.iterator(); it.hasNext();)
            {
                GlobalSightLocale targetLocale = (GlobalSightLocale) it.next();
                boolean useLeveragedTerms = ExtractedFileImporter
                        .getLeveragematch();
                ArrayList<Tuv> targetTuvs = getTargetTuvs(p_sourcePage,
                        sourceTuvMap, targetLocale, p_termMatches,
                        useLeveragedTerms, p_exactMatchedSegments);
                tuvs.addAll(targetTuvs);
            }
        }
        catch (Exception e)
        {
            throw new PageException(e);
        }

        return tuvs;
    }

    private HashMap<Tu, Tuv> getSourceTuvMap(Collection p_tuvs, long p_jobId)
    {
        HashMap<Tu, Tuv> result = new HashMap<Tu, Tuv>();
        Iterator it = p_tuvs.iterator();
        while (it.hasNext())
        {
            Tuv tuv = (Tuv) it.next();
            Tu tu = tuv.getTu(p_jobId);
            result.put(tu, tuv);
        }
        return result;
    }

}
