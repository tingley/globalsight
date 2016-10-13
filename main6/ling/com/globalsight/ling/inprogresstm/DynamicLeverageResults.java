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
package com.globalsight.ling.inprogresstm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import com.globalsight.everest.integration.ling.tm2.LeverageMatch;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.tuv.Tu;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.ling.tm.LeverageMatchLingManager;
import com.globalsight.ling.tm.TuvBasicInfo;
import com.globalsight.ling.tm2.TmCoreManager;
import com.globalsight.ling.tm2.leverage.LeverageOptions;
import com.globalsight.ling.tm2.leverage.LeverageUtil;
import com.globalsight.ling.tm2.leverage.Leverager;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.SortUtil;

/**
 * DynamicLeverageResults holds a set of DynamicLeveragedSegment as a result of
 * a dynamic leveraging.
 * 
 * When this object is returned from a dynamic leverage API, the leveraged
 * segments are sorted by the score (high to low). If there are more than one
 * segment of the same score, they are sorted by match category in the order of
 * FROM_GOLD_TM, FROM_IN_PROGRESS_TM_SAME_JOB and FROM_IN_PROGRESS_TM_OTHER_JOB,
 * from high to low.
 */
public class DynamicLeverageResults implements Serializable
{
    private static final long serialVersionUID = 7049251457045228463L;

    // Comparator used in generalSort()
    private static GeneralComparator c_generalComapator = new GeneralComparator();
    private static GeneralComparatorByTm c_generalComparatorByTm = new GeneralComparatorByTm();

    // Original source text in GXML
    private String m_originalSourceText;

    // source and target locale of the job
    private GlobalSightLocale m_sourceLocale;
    private GlobalSightLocale m_targetLocale;

    // localize type
    private boolean m_isTranslatable;

    // list of DynamicLeveragedSegment objects
    private ArrayList<DynamicLeveragedSegment> m_leverageResults;
    private LeverageOptions leverageOptions;

    public LeverageOptions getLeverageOptions()
    {
        return leverageOptions;
    }

    public void setLeverageOptions(LeverageOptions leverageOptions)
    {
        this.leverageOptions = leverageOptions;
    }

    /**
     * constuctor.
     * 
     * @param p_originalText
     *            Original souce text
     * @param p_sourceLocale
     *            Source locale
     * @param p_targetLocale
     *            Target locale
     * @param p_isTranslatable
     *            set true is the segment is translatable
     */
    public DynamicLeverageResults(String p_originalText,
            GlobalSightLocale p_sourceLocale, GlobalSightLocale p_targetLocale,
            boolean p_isTranslatable)
    {
        m_originalSourceText = p_originalText;
        m_sourceLocale = p_sourceLocale;
        m_targetLocale = p_targetLocale;
        m_isTranslatable = p_isTranslatable;

        m_leverageResults = new ArrayList<DynamicLeveragedSegment>();
    }

    /**
     * get Nth leverage result. The index of the first result is 0.
     * 
     * @param p_index
     *            index of the result. Starts with 0.
     * @return DynamicLeveragedSegment object
     */
    public DynamicLeveragedSegment get(int p_index)
    {
        return m_leverageResults.get(p_index);
    }

    public void serOrgSid(String orgSid)
    {
        if (m_leverageResults != null)
        {
            for (DynamicLeveragedSegment result : m_leverageResults)
            {
                result.setOrgSid(orgSid);
            }
        }
    }

    /**
     * get the size of the leverage result.
     * 
     * @return size of the leverage result
     */
    public int size()
    {
        return m_leverageResults.size();
    }

    /**
     * Add DynamicLeveragedSegment
     * 
     * @param p_segment
     *            DynamicLeveragedSegment object
     */
    public void add(DynamicLeveragedSegment p_segment)
    {
        m_leverageResults.add(p_segment);
    }

    /**
     * Sort the list of DynamicLeveragedSegment from high to low score and in
     * the order of FROM_GOLD_TM, FROM_IN_PROGRESS_TM_SAME_JOB and
     * FROM_IN_PROGRESS_TM_OTHER_JOB when the score is the same.
     */
    public void generalSort(LeverageOptions leverageOptions, long p_jobId)
    {
        c_generalComapator.setLeverageOptions(leverageOptions);
        c_generalComapator.setJobId(p_jobId);
        SortUtil.sort(m_leverageResults, c_generalComapator);
    }

    /**
     * Merge two DynamicLeverageResults objects. It merges only
     * DynamicLeveragedSegment lists. The other members are not even checked for
     * the equality.
     * 
     * @param p_other
     *            DynamicLeverageResults object that is merged into this object
     */
    public void merge(DynamicLeverageResults p_other)
    {
        int size = p_other.size();
        for (int i = 0; i < size; i++)
        {
            this.add(p_other.get(i));
        }
    }

    /**
     * Merge a set of pre-leveraged results into DynamicLeverageResults object.
     * At the end of the process, generalSort() method is called and the
     * elements are sorted in a default order.
     * 
     * Note: The pre-leverage results (stored in leverage_match table) don't
     * have matched source text information. So m_matchedSourceText field in
     * DynamicLeveragedSegment object will be an empty string.
     * 
     * @param p_preLeverageResults
     *            Set of LeverageMatch objects
     */
    public void mergeWithPreLeverage(Set<LeverageMatch> p_preLeverageResults,
            boolean isTmProcedence, long p_jobId)
    {
        // sanity check
        if (p_preLeverageResults == null)
        {
            return;
        }

        for (LeverageMatch levMatch : p_preLeverageResults)
        {
            try
            {
                String source = levMatch.getMatchedOriginalSource() == null ? ""
                        : levMatch.getMatchedOriginalSource();
                int matchCategory = getMatchCategory(levMatch);

                DynamicLeveragedSegment dynLevSegment = new DynamicLeveragedSegment(
                        source, levMatch.getMatchedText(), m_sourceLocale,
                        m_targetLocale, levMatch.getMatchState(),
                        levMatch.getScoreNum(), matchCategory,
                        levMatch.getTmId(), levMatch.getMatchedTuvId());

                int tmIndex = levMatch.getProjectTmIndex();
                long tmId = levMatch.getTmId();
                TuvBasicInfo tuvBasicInfo = null;
                if (tmIndex == -7)
                {
                    // Leverage from in progress TM, tmId is the job ID
                    long jobDataTuId = levMatch.getJobDataTuId();
                    GlobalSightLocale locale = levMatch.getTargetLocale();

                    Job job = ServerProxy.getJobHandler().getJobById(tmId);
                    Tu tu = ServerProxy.getTuvManager().getTuForSegmentEditor(
                            jobDataTuId, tmId);
                    // If job has been discarded, "job" and "tu" will be null...
                    if (job == null || tu == null)
                        continue;

                    Tuv tuv = tu.getTuv(locale.getId(), tmId);
                    tuvBasicInfo = new TuvBasicInfo(
                            levMatch.getLeveragedTargetString(), null, null,
                            locale, tuv.getCreatedDate(), tuv.getCreatedUser(),
                            tuv.getLastModified(), tuv.getLastModifiedUser(),
                            null, tuv.getSid());
                    dynLevSegment.setMatchedTuvJobName(job.getJobName());
                }
                else if (tmId > 0)
                {
                    tuvBasicInfo = new TuvBasicInfo(
                            levMatch.getLeveragedTargetString(), null, null,
                            levMatch.getTargetLocale(),
                            levMatch.getCreationDate(),
                            levMatch.getCreationUser(),
                            levMatch.getModifyDate(), levMatch.getModifyUser(),
                            null, levMatch.getSid());
//                    tuvBasicInfo = LingServerProxy.getTmCoreManager()
//                            .getTuvBasicInfoByTuvId(tmId, matchedTUVId,
//                                    targetLocaleId);
                }

                dynLevSegment.setMatchedTuvBasicInfo(tuvBasicInfo);
                dynLevSegment.setTmIndex(tmIndex);
                dynLevSegment.setOrderNum(levMatch.getOrderNum());
                dynLevSegment.setMtName(levMatch.getMtName());
                dynLevSegment.setOrgSid(levMatch.getOrgSid(tmId));
                // 3220 : Code refactor for getting "SID", "ModifiedDate" etc
                // for performance
                dynLevSegment.setSid(levMatch.getSid());
                dynLevSegment.setModifyDate(levMatch.getModifyDate());
                add(dynLevSegment);
            }
            catch (Exception ignore)
            {

            }
        }
        if (isTmProcedence)
        {
            generalSortByTm(leverageOptions, p_jobId);
        }
        else
        {
            generalSort(leverageOptions, p_jobId);
        }

        removeDuplicates();
    }

    /**
     * As the static matches in "leverage_match" may be both from gold TM and In
     * Progress TM, here need decide the match category according to
     * "matchedTableType" and "ProjectTmIndex".
     * 
     */
    private int getMatchCategory(LeverageMatch p_levMatches)
    {
        // Default set it to "from gold TM".
        int matchCategory = DynamicLeveragedSegment.FROM_GOLD_TM;

        int matchedTableType = (int) p_levMatches.getMatchedTableType();
        // The static match is from In-Progress job TM (via "update leverage"
        // operation)
        if ((matchedTableType == LeverageMatchLingManager.IN_PROGRESS_TM_T || matchedTableType == LeverageMatchLingManager.IN_PROGRESS_TM_L)
                && p_levMatches.getProjectTmIndex() == Leverager.IN_PROGRESS_TM_PRIORITY)
        {
            // For in progress matches, the "tmId" is the jobId.
            long jobIdMatchFrom = p_levMatches.getTmId();

            long srcPageId = p_levMatches.getSourcePageId();
            try
            {
                SourcePage sp = ServerProxy.getPageManager().getSourcePage(
                        srcPageId);
                long jobId = sp.getJobId();
                if (jobIdMatchFrom == jobId)
                {
                    matchCategory = DynamicLeveragedSegment.FROM_IN_PROGRESS_TM_SAME_JOB;
                }
                else
                {
                    matchCategory = DynamicLeveragedSegment.FROM_IN_PROGRESS_TM_OTHER_JOB;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        return matchCategory;
    }

    // m_leverageResults must be sorted by generalSort() before this
    // method is called.
    private void removeDuplicates()
    {
        DynamicLeveragedSegment prevSeg = null;

        for (Iterator<DynamicLeveragedSegment> it = m_leverageResults
                .iterator(); it.hasNext();)
        {
            DynamicLeveragedSegment currSeg = it.next();
            if (currSeg.equals(prevSeg))
            {
                it.remove();
            }
            else
            {
                prevSeg = currSeg;
            }
        }
    }

    private static class GeneralComparatorByTm implements Comparator
    {
        private HashMap m_categoryMap;
        private LeverageOptions leveragetOptions;
        private long jobId = -1;

        public void setLeverageOptions(LeverageOptions leveragetOptions)
        {
            this.leveragetOptions = leveragetOptions;
        }

        public void setJobId(long jobId)
        {
            this.jobId = jobId;
        }

        public LeverageOptions getLeverageOptions()
        {
            return this.leveragetOptions;
        }

        private GeneralComparatorByTm()
        {
            m_categoryMap = new HashMap();
            m_categoryMap.put(
                    new Integer(DynamicLeveragedSegment.FROM_GOLD_TM),
                    new Integer(1));
            m_categoryMap.put(new Integer(
                    DynamicLeveragedSegment.FROM_IN_PROGRESS_TM_SAME_JOB),
                    new Integer(2));
            m_categoryMap.put(new Integer(
                    DynamicLeveragedSegment.FROM_IN_PROGRESS_TM_OTHER_JOB),
                    new Integer(3));
        }

        public int compare(Object o1, Object o2)
        {
            int result = 1;
            if (o1 instanceof DynamicLeveragedSegment
                    && o2 instanceof DynamicLeveragedSegment)
            {
                DynamicLeveragedSegment segment1 = (DynamicLeveragedSegment) o1;
                DynamicLeveragedSegment segment2 = (DynamicLeveragedSegment) o2;

				result = (int) (segment1.getMatchedTuvId() - segment2
						.getMatchedTuvId());
                if (result != 0)
                {
                	return result;
                }

                int tmIndex1 = segment1.getTmIndex();
                float score1 = segment1.getScore();
                int tmIndex2 = segment2.getTmIndex();
                float score2 = segment2.getScore();

                result = tmIndex1 - tmIndex2;
                if (result != 0)
                {
                    return result;
                }

                result = (int) (score2 - score1);
                if (result != 0)
                {
                    return result;
                }

                if (score2 == 100)
                {
                    result = LeverageUtil.compareSid(segment1, segment2, jobId);
                    if (result != 0)
                    {
                        return result;
                    }
                }

                if (segment1.getModifyDate() != null
                        && segment2.getModifyDate() != null)
                {
                    result = segment1.getModifyDate().compareTo(
                            segment2.getModifyDate());
                    int mode;
                    if (leveragetOptions.isLatestLeveragingForReimport())
                    {
                        mode = LeverageOptions.PICK_LATEST;
                    }
                    else
                    {
                        mode = leveragetOptions.getMultipleExactMatcheMode();
                    }

                    if (mode == LeverageOptions.PICK_LATEST)
                    {
                        result = -result;
                    }

                    if (result != 0)
                    {
                        return result;
                    }
                }

                int orderNum1 = segment1.getOrderNum();
                // If "order_num" indicates it is MT matches
                if (orderNum1 >= TmCoreManager.LM_ORDER_NUM_START_MT
                        && orderNum1 <= 400)
                {
                    // "order_num" first in order to ensure MT matches with tag
                    // are in advance than that without tag.
                    result = segment1.getOrderNum() - segment2.getOrderNum();
                    if (result != 0)
                    {
                        return result;
                    }
                    // "targetText" second
                    result = segment1.getMatchedTargetText().compareTo(
                            segment2.getMatchedTargetText());
                    if (result != 0)
                    {
                        return result;
                    }
                }
                else
                {
                    // "targetText" first
                    result = segment1.getMatchedTargetText().compareTo(
                            segment2.getMatchedTargetText());
                    if (result != 0)
                    {
                        return result;
                    }
                    // "order_num" second
                    result = segment1.getOrderNum() - segment2.getOrderNum();
                    if (result != 0)
                    {
                        return result;
                    }
                }

                Integer category1 = (Integer) m_categoryMap.get(new Integer(
                        segment1.getMatchCategory()));
                Integer category2 = (Integer) m_categoryMap.get(new Integer(
                        segment2.getMatchCategory()));

                if (category1 != null && category2 != null)
                {
                    result = category1.intValue() - category2.intValue();
                }
            }
            return result;
        }
    }

    // Comparator used in generalSort()
    private static class GeneralComparator implements Comparator
    {
        private HashMap m_categoryMap;
        private LeverageOptions leverageOptions;
        private long jobId = -1;

        private GeneralComparator()
        {
            m_categoryMap = new HashMap();
            m_categoryMap.put(
                    new Integer(DynamicLeveragedSegment.FROM_GOLD_TM),
                    new Integer(1));
            m_categoryMap.put(new Integer(
                    DynamicLeveragedSegment.FROM_IN_PROGRESS_TM_SAME_JOB),
                    new Integer(2));
            m_categoryMap.put(new Integer(
                    DynamicLeveragedSegment.FROM_IN_PROGRESS_TM_OTHER_JOB),
                    new Integer(3));
        }

        public void setLeverageOptions(LeverageOptions leverageOptions)
        {
            this.leverageOptions = leverageOptions;
        }

        public void setJobId(long jobId)
        {
            this.jobId = jobId;
        }

        public int compare(Object o1, Object o2)
        {
            int result = 1; // default can be any of -1, 0, 1

            if (o1 instanceof DynamicLeveragedSegment
                    && o2 instanceof DynamicLeveragedSegment)
            {
                DynamicLeveragedSegment segment1 = (DynamicLeveragedSegment) o1;
                DynamicLeveragedSegment segment2 = (DynamicLeveragedSegment) o2;

				result = (int) (segment1.getMatchedTuvId() - segment2
						.getMatchedTuvId());
                if (result != 0)
                {
                	return result;
                }

                result = (int) (segment2.getScore() - segment1.getScore());
                if (result != 0)
                {
                    return result;
                }

                if (segment1.getScore() == 100)
                {
                    result = LeverageUtil.compareSid(segment1, segment2, jobId);
                    if (result != 0)
                    {
                        return result;
                    }
                }

                if (segment1.getModifyDate() != null
                        && segment2.getModifyDate() != null)
                {
                    result = segment1.getModifyDate().compareTo(
                            segment2.getModifyDate());
                    int mode;
                    if (leverageOptions.isLatestLeveragingForReimport())
                    {
                        mode = LeverageOptions.PICK_LATEST;
                    }
                    else
                    {
                        mode = leverageOptions.getMultipleExactMatcheMode();
                    }

                    if (mode == LeverageOptions.PICK_LATEST)
                    {
                        result = -result;
                    }

                    if (result != 0)
                    {
                        return result;
                    }
                }

                result = segment1.getTmIndex() - segment2.getTmIndex();
                if (result != 0)
                {
                    return result;
                }

                int orderNum1 = segment1.getOrderNum();
                // If "order_num" indicates it is MT matches
                if (orderNum1 >= TmCoreManager.LM_ORDER_NUM_START_MT
                        && orderNum1 <= 400)
                {
                    // "order_num" first in order to ensure MT matches with tag
                    // are in advance than that without tag.
                    result = segment1.getOrderNum() - segment2.getOrderNum();
                    if (result != 0)
                    {
                        return result;
                    }
                    // "targetText" second
                    result = segment1.getMatchedTargetText().compareTo(
                            segment2.getMatchedTargetText());
                    if (result != 0)
                    {
                        return result;
                    }
                }
                else
                {
                    // "targetText" first
                    result = segment1.getMatchedTargetText().compareTo(
                            segment2.getMatchedTargetText());
                    if (result != 0)
                    {
                        return result;
                    }
                    // "order_num" second
                    result = segment1.getOrderNum() - segment2.getOrderNum();
                    if (result != 0)
                    {
                        return result;
                    }
                }

                Integer category1 = (Integer) m_categoryMap.get(new Integer(
                        segment1.getMatchCategory()));
                Integer category2 = (Integer) m_categoryMap.get(new Integer(
                        segment2.getMatchCategory()));

                if (category1 != null && category2 != null)
                {
                    result = category1.intValue() - category2.intValue();
                }
            }

            return result;
        }
    }

    public void generalSortByTm(LeverageOptions leverageOptions, long p_jobId)
    {
        c_generalComparatorByTm.setLeverageOptions(leverageOptions);
        c_generalComparatorByTm.setJobId(p_jobId);
        SortUtil.sort(m_leverageResults, c_generalComparatorByTm);
    }
}
