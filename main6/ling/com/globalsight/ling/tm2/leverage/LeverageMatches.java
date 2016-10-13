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
package com.globalsight.ling.tm2.leverage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.globalsight.cxe.entity.customAttribute.JobAttribute;
import com.globalsight.cxe.entity.customAttribute.TMAttributeCons;
import com.globalsight.cxe.entity.customAttribute.TMAttributeManager;
import com.globalsight.cxe.entity.customAttribute.TMPAttribute;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobImpl;
import com.globalsight.everest.projecthandler.ProjectTM;
import com.globalsight.everest.projecthandler.ProjectTmTuT;
import com.globalsight.everest.projecthandler.ProjectTmTuTProp;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.util.comparator.TMPAttributeComparator;
import com.globalsight.ling.common.Text;
import com.globalsight.ling.tm.LingManagerException;
import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.ling.tm2.SegmentTmTu;
import com.globalsight.ling.tm2.SegmentTmTuv;
import com.globalsight.ling.tm2.TmUtil;
import com.globalsight.ling.tm2.indexer.NgramTokenizer;
import com.globalsight.ling.tm2.indexer.Token;
import com.globalsight.ling.tm3.core.DefaultManager;
import com.globalsight.ling.tm3.core.TM3Attribute;
import com.globalsight.ling.tm3.core.TM3Exception;
import com.globalsight.ling.tm3.core.TM3Tm;
import com.globalsight.ling.tm3.core.TM3Tu;
import com.globalsight.ling.tm3.integration.GSDataFactory;
import com.globalsight.ling.tm3.integration.segmenttm.TM3Util;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.RuntimeCache;
import com.globalsight.util.SortUtil;
import com.globalsight.util.StringUtil;

/**
 * LeverageMatches holds leverage match results for a segment
 */

public class LeverageMatches
{
    private static Logger c_logger = Logger.getLogger(LeverageMatches.class
            .getName());

    // leverage options
    private LeverageOptions m_leverageOptions = null;

    // original source Tuv
    private BaseTmTuv m_originalSourceTuv = null;

    // list of LeveragedTu. All matched segments are held here.
    private List<LeveragedTu> m_leveragedTus = new ArrayList<LeveragedTu>();

    // Repository of matches grouped by target locales. This object is
    // built using data in m_leveragedTus after leverage options are
    // applied to the data.
    private OrderedMatchSegments m_orderedMatchSegments = null;

    private Job job = null;

    // constructor
    public LeverageMatches(BaseTmTuv p_sourceTuv,
            LeverageOptions p_leverageOptions)
    {
        m_originalSourceTuv = p_sourceTuv;
        m_leverageOptions = p_leverageOptions;
    }

    /**
     * apply Page Tm matching options to matches. According to the options user
     * selected, match scores and match states are adjusted.
     */
    public void applyPageTmOptions() throws Exception
    {
        Iterator itLeveragedTu = m_leveragedTus.iterator();
        while (itLeveragedTu.hasNext())
        {
            LeveragedTu tu = (LeveragedTu) itLeveragedTu.next();
            if (tu.getScore() == 100)
            {
                String originalText = m_originalSourceTuv.getExactMatchFormat();
                String matchedSrcText = tu.getSourceTuv().getExactMatchFormat();

                // if the strings are exactly the same, match state is
                // either PAGE_TM_EXACT_MATCH or TYPE_DIFFERENT. At
                // this point, PAGE_TM_EXACT_MATCH is set to all
                // 100% matches.
                if (originalText.equals(matchedSrcText))
                {
                    if (!tu.getType().equals(m_originalSourceTuv.getType()))
                    {
                        tu.setMatchState(MatchState.TYPE_DIFFERENT);

                        // type sensitive leveraging?
                        if (m_leverageOptions
                                .isTypeSensitiveLeveragingForReimport())
                        {
                            tu.setScore(100 - m_leverageOptions
                                    .getTypeDifferencePenaltyForReimport());
                        }
                    }
                }
                else
                {
                    // exact match key is the same, but the text is
                    // not exactly the same. Not a match
                    // candidate. Give 0% score.
                    tu.setScore(0);
                    tu.setMatchState(MatchState.NOT_A_MATCH);
                }
                // Fixing GBS-567, move out "setRefTmScores" to avoid 100%
                // matching limit
                /*
                 * if(tu.getScore() == 100){ setRefTmScores(tu); }
                 */
            }

            setRefTmScores(tu);
        }
    }

    /**
     * apply Segment Tm matching options to matches. According to the options
     * user selected, match scores and match states are adjusted.
     */
    public void applySegmentTmOptions() throws Exception
    {
        removeInvalidLeveragedTus();

        Iterator itLeveragedTu = m_leveragedTus.iterator();
        while (itLeveragedTu.hasNext())
        {
            LeveragedTu tu = (LeveragedTu) itLeveragedTu.next();

            if (tu.getScore() == 100)
            {
                if (m_leverageOptions.isAutoRepair())
                {
                    repairPlaceholders(tu);
                }

                String originalFuzzyFormat = m_originalSourceTuv
                        .getFuzzyIndexFormat();
                String matchedSrcFuzzyFormat = tu.getSourceTuv()
                        .getFuzzyIndexFormat();

                String originalText = m_originalSourceTuv.getExactMatchFormat();
                String matchedSrcText = tu.getSourceTuv().getExactMatchFormat();

                c_logger.debug("originalText: " + originalText);
                c_logger.debug("matchedSrcText: " + matchedSrcText);

                if (!originalFuzzyFormat.equals(matchedSrcFuzzyFormat))
                {
                    // Even if the fuzzy match score is 100%, fuzzy
                    // index format is not equal. That means that the
                    // match is not really 100% exact. The word order
                    // might be different or stop words might be
                    // different. Recalculate the score using n-gram
                    // indexing and demote to fuzzy.
                    float score = ngramScore(originalFuzzyFormat,
                            matchedSrcFuzzyFormat);

                    if (score < m_leverageOptions.getMatchThreshold())
                    {
                        tu.setMatchState(MatchState.STATISTICS_MATCH);
                    }
                    else
                    {
                        tu.setMatchState(MatchState.FUZZY_MATCH);
                    }

                    tu.setScore(score);
                }
                else if (originalText.equals(matchedSrcText))
                {
                    // if the strings are exactly the same, match state is
                    // either SEGMENT_TM_EXACT_MATCH or TYPE_DIFFERENT. At
                    // this point, SEGMENT_TM_EXACT_MATCH has been set to
                    // all 100% matches.
                    c_logger.debug("Tu " + tu.getId() + " is exact match.");

                    if (!tu.getType().equals(m_originalSourceTuv.getType()))
                    {
                        tu.setMatchState(MatchState.TYPE_DIFFERENT);

                        // type sensitive leveraging?
                        if (m_leverageOptions.isTypeSensitiveLeveraging())
                        {
                            tu.setScore(100 - m_leverageOptions
                                    .getTypeDifferencePenalty());
                        }
                    }
                }
                else
                {
                    c_logger.debug("Tu " + tu.getId()
                            + " is quasi exact match.");

                    setOptionAppliedScore(tu);
                }
            }

            setRefTmScores(tu);

            try
            {
				// As we have not implemented in-progress TM for GBS-3650, below
				// rules only apply for golden TM matches.
    			if (tu instanceof LeveragedSegmentTu && !isAllTmpAttrMatched(tu))
                {
                    TranslationMemoryProfile tmp = m_leverageOptions.getTmProfile();
    				// "Disregard if TU attributes not matched"
    				if (TranslationMemoryProfile.CHOICE_DISREGARD.equals(tmp.getChoiceIfAttNotMatch()))
    				{
    					itLeveragedTu.remove();
    				}
    				// "Penalize if TU attributes not matched"
    				else
    				{
    	                float score = tu.getScore();
    	                float oriscore = score;
    					score = score - tmp.getTuAttNotMatchPenalty();
    					if (score < 0)
    					{
    						score = 0;
    					}
    	    			tu.setScore(score);
    	                if (oriscore > score)
    	                {
    	                    tu.setMatchState(MatchState.FUZZY_MATCH);
    	                }
    				}
                }
            }
            catch (Exception e)
            {
                c_logger.error("Error when setting tuv attributes scores", e);
            }
        }
    }

    /**
     * If there is no source TUV in one TM TU returned by TM leveraging, these
     * data will cause error, should be removed.
     */
    private void removeInvalidLeveragedTus()
    {
        for (Iterator itLeveragedTu = m_leveragedTus.iterator(); itLeveragedTu
                .hasNext();)
        {
            LeveragedTu tu = (LeveragedTu) itLeveragedTu.next();
            if (tu.getSourceTuv() == null)
            {
                itLeveragedTu.remove();
            }
        }
    }

    // for GBS-937, Custom TM attributes originally;
    // GBS-3564 reverse the logic.
    private boolean isAllTmpAttrMatched(LeveragedTu tu)
    {
        long tmId = tu.getTmId();
        ProjectTM ptm = HibernateUtil.get(ProjectTM.class, tmId);
        if (ptm == null)
        {
            c_logger.debug("ptm == null");
            return false;
        }

        boolean isTM3 = ptm.getTm3Id() != null;
        long tuId = tu.getId();
        TranslationMemoryProfile tmp = m_leverageOptions.getTmProfile();
        if (tmp == null)
        {
            c_logger.debug("tmp == null");
            return false;
        }

        Set<ProjectTmTuTProp> tuProps = null;
        List<TMPAttribute> tmpAtts = tmp.getAllTMPAttributes();
		SortUtil.sort(tmpAtts, new TMPAttributeComparator(
				TMPAttributeComparator.ORDER, Locale.ENGLISH));

        if (!isTM3)
        {
            ProjectTmTuT tmTU = HibernateUtil.get(ProjectTmTuT.class, tuId);

            if (tmTU == null)
            {
                c_logger.debug("tmTU == null");
                return false;
            }

            tuProps = tmTU.getProps();
        }
        else
        {
            tuProps = new HashSet<ProjectTmTuTProp>();
            TM3Tm tm3tm = null;
            try
            {
                tm3tm = DefaultManager.create().getTm(new GSDataFactory(),
                        ptm.getTm3Id());
                if (tm3tm != null)
                {
                    TM3Tu tm3tu = tm3tm.getTu(tuId);
                    if (tm3tu == null)
                        return false;

                    Map<TM3Attribute, Object> tm3Attributes = tm3tu
                            .getAttributes();
                    if (tm3Attributes != null && !tm3Attributes.isEmpty())
                    {
                        for (Map.Entry<TM3Attribute, Object> tmAt : tm3Attributes
                                .entrySet())
                        {
                            ProjectTmTuTProp prop = TM3Util.toProjectTmTuTProp(
                                    tmAt.getKey(), tmAt.getValue());

                            if (prop != null)
                            {
                                tuProps.add(prop);
                            }
                        }
                    }
                }
            }
            catch (Exception e)
            {
                throw new TM3Exception(e);
            }
        }

        // debug some information
        if (c_logger.isDebugEnabled())
        {
            c_logger.debug("tu id " + tuId);
            c_logger.debug("tmp : " + tmp.getId() + " " + tmp.getName());

            if (tmpAtts != null && tmpAtts.size() > 0)
            {
                c_logger.debug("tmpAtts : ");
                c_logger.debug(tmpAtts.toString());
            }
            else
            {
                c_logger.debug("tmpAtts is empty.");
            }

            if (tuProps != null && tuProps.size() > 0)
            {
                c_logger.debug("tuProps : ");
                c_logger.debug(tuProps.toString());
            }
            else
            {
                c_logger.debug("tuProps is empty.");
            }

            if (job != null)
            {
                List<JobAttribute> jobAtts = job.getAllJobAttributes();
                if (jobAtts != null && jobAtts.size() > 0)
                {
                    c_logger.debug("jobAtts : ");
                    for (JobAttribute jobAtt : jobAtts)
                    {
                        c_logger.debug(jobAtt.getAttribute().getName() + " : "
                                + jobAtt.getValue());
                    }
                }
                else
                {
                    c_logger.debug("jobAtts is empty.");
                }
            }
            else
            {
                c_logger.debug("job == null");
            }
        }

		boolean isFinalMatched = true;
        if (tmpAtts != null && tmpAtts.size() > 0)
        {
        	// Record if it is matched of every TM profile attributes separately.
        	List<Boolean> matchedList = new ArrayList<Boolean>();
			for (TMPAttribute tmpAtt : tmpAtts)
            {
                String tmpAttValueType = tmpAtt.getValueType();
                String tmpAttName = tmpAtt.getAttributeName();
                String tmpAttOp = tmpAtt.getOperator();
                Object jobAttValue = null;
                // get attribute value of TU
                String tuAttValue = ProjectTmTuTProp.getAttributeValue(tuProps,
                        tmpAttName);

                // get job attribute value if needed
                if (tmpAttValueType.equals(TMAttributeCons.VALUE_FROM_JOBATT)
                        && job != null)
                {
                    List<JobAttribute> jobAtts = null;
                    if (job instanceof JobImpl)
                    {
                        String uuid = ((JobImpl) job).getUuid();
                        jobAtts = RuntimeCache.getJobAttributes(uuid);
                    }
                    if (jobAtts == null)
                    {
                        jobAtts = job.getAllJobAttributes();
                    }
                    if (jobAtts != null)
                    {
                        for (JobAttribute jobAtt : jobAtts)
                        {
                            if (jobAtt.getAttribute().getName()
                                    .equalsIgnoreCase(tmpAttName))
                            {
                                Object v = jobAtt.getValue();
                                jobAttValue = v == null ? "" : v;
                            }
                        }
                    }
                }
                else if (tmpAttValueType.equals(TMAttributeCons.VALUE_INPUT))
                {
                    jobAttValue = tmpAtt.getValueData();
                }

                Boolean matched = false;
                try
                {
					matched = TMAttributeManager.isTMPAttributeMatched(
							tmpAttOp, tuAttValue, jobAttValue);
                }
                catch (Exception e)
                {
                	c_logger.error(e);
                }
				matchedList.add(new Boolean(matched));
            }

			// Decide if it is matched for all TM profile TU attributes.
			String curAndOr = null;
			boolean preMatched = false;
			boolean curMatched = false;
			for (int i = 0; i < tmpAtts.size(); i++)
			{
				TMPAttribute tmpAtt = tmpAtts.get(i);
				if (i == 0)
				{
					preMatched = matchedList.get(i);
					isFinalMatched = preMatched;
				}
				else
				{
					curAndOr = tmpAtt.getAndOr();
					curMatched = matchedList.get(i);
					if ("and".equalsIgnoreCase(curAndOr))
					{
						isFinalMatched = preMatched && curMatched;
						preMatched = isFinalMatched;
					}
					else
					{
						isFinalMatched = preMatched || curMatched;
						preMatched = isFinalMatched;
					}

					// If up to now it is false, and the next is "and", the
					// result must be false.
					if (!preMatched
							&& (i + 1 < tmpAtts.size())
							&& "and".equalsIgnoreCase(tmpAtts.get(i + 1).getAndOr()))
					{
						break;
					}
				}
			}
        }

        return isFinalMatched;
    }

    public BaseTmTuv getOriginalTuv()
    {
        return m_originalSourceTuv;
    }

    /**
     * Add a LeveragedTu to this object. When this method is called,
     * m_orderedMatchSegments is cleared.
     * 
     * @param p_leveragedTu
     *            LeveragedTu object
     */
    public void add(LeveragedTu p_leveragedTu)
    {
        m_orderedMatchSegments = null;
        m_leveragedTus.add(p_leveragedTu);
    }

    /**
     * Remove matches with STATISTICS_MATCH or NOT_A_MATCH state.
     */
    public void removeNoMatches()
    {
        // clear the cache
        m_orderedMatchSegments = null;

        for (Iterator it = m_leveragedTus.iterator(); it.hasNext();)
        {
            LeveragedTu tu = (LeveragedTu) it.next();

            MatchState matchState = tu.getMatchState();
            if (matchState.equals(MatchState.STATISTICS_MATCH)
                    || matchState.equals(MatchState.NOT_A_MATCH))
            {
                it.remove();
            }
        }
    }

    /**
     * Break up this LeverageMatches object into LeverageMatches for the main
     * segment and subflows. This method breaks up m_originalSourceTuv and
     * m_leveragedTus but m_originalSourceTuv is not assigned. The field must be
     * populated later.
     * 
     * @return collection of broken up LeverageMatches
     */
    public Collection separateMatch() throws Exception
    {
        Map brokenUpLevMatches = new HashMap();
        GlobalSightLocale sourceLocale = m_originalSourceTuv.getLocale();

        // break up the original source segment and put them in a Map
        // (sub id as key, LeverageMatches as value)
        Collection brokenUpSource = TmUtil.createSegmentTmTus(
                m_originalSourceTuv.getTu(), sourceLocale);
        Iterator itBrokenUpSource = brokenUpSource.iterator();
        while (itBrokenUpSource.hasNext())
        {
            SegmentTmTu brokenUpSourceTu = (SegmentTmTu) itBrokenUpSource
                    .next();
            brokenUpLevMatches.put(
                    brokenUpSourceTu.getSubId(),
                    new LeverageMatches(brokenUpSourceTu
                            .getFirstTuv(sourceLocale), m_leverageOptions));
        }

        // break up each element of m_leveragedTus and add broken up
        // segments to an appropriate LeverageMatches.
        Iterator itLeveragedPageTu = m_leveragedTus.iterator();
        while (itLeveragedPageTu.hasNext())
        {
            LeveragedTu leveragedPageTu = (LeveragedTu) itLeveragedPageTu
                    .next();
            Iterator itBrokenUpPageTu = TmUtil.createSegmentTmTus(
                    leveragedPageTu, sourceLocale).iterator();
            while (itBrokenUpPageTu.hasNext())
            {
                SegmentTmTu brokenUpPageTu = (SegmentTmTu) itBrokenUpPageTu
                        .next();

                LeverageMatches levMatch = (LeverageMatches) brokenUpLevMatches
                        .get(brokenUpPageTu.getSubId());

                // def 10864
                // ignore unfound subflows
                if (levMatch != null)
                {
                    levMatch.add((LeveragedTu) brokenUpPageTu);
                }
            }
        }

        return brokenUpLevMatches.values();
    }

    /**
     * Returns subflow id of the original source Tuv of this object. If
     * m_originalSourceTuv is not a derived class of SegmentTmTuv, this method
     * returns "0".
     * 
     * @return sub id
     */
    public String getSubflowId()
    {
        String subId = null;
        if (m_originalSourceTuv instanceof SegmentTmTuv)
        {
            subId = ((SegmentTmTu) m_originalSourceTuv.getTu()).getSubId();
        }
        else
        {
            subId = SegmentTmTu.ROOT;
        }

        return subId;
    }

    /**
     * Returns exact match of each target locales in ExactLeverageMatch object.
     * ExactLeverageMatch holds only one LeveragedTuv as an exact match for each
     * target locale. If there is no exact match for a locale, the locale won't
     * be added in ExactLeverageMatch.
     * 
     * @return ExactLeverageMatch object
     */
    public ExactLeverageMatch getExactLeverageMatch(long p_jobId)
    {
        if (m_orderedMatchSegments == null)
        {
            populateOrderedMatchSegments(p_jobId);
        }

        return m_orderedMatchSegments.getExactLeverageMatch();
    }

    /**
     * Returns a Set of target locales that have an exact match.
     * 
     * @return Set of GlobalSightLocale objects which are target locales that
     *         have an exact match
     */
    public Set getExactMatchLocales(long p_jobId)
    {
        if (m_orderedMatchSegments == null)
        {
            populateOrderedMatchSegments(p_jobId);
        }

        return m_orderedMatchSegments.getExactMatchLocales();
    }

    /**
     * Returns an exact match as LeveragedTuv for a given locale.
     * 
     * @return exact match for a given locale. If there is no exact match for
     *         the locale, null is returned.
     */
    public LeveragedTuv getExactLeverageMatch(GlobalSightLocale p_targetLocale,
            Long p_jobId)
    {
        if (m_orderedMatchSegments == null)
        {
            populateOrderedMatchSegments(p_jobId);
        }

        return m_orderedMatchSegments.getExactLeverageMatch(p_targetLocale);
    }

    // For Lexmark TMs leverage issue
    public List<LeveragedTu> getLeveragedTus()
    {
        return this.m_leveragedTus;
    }

    public void setLeveragedTus(List<LeveragedTu> leveragedTus)
    {
        this.m_leveragedTus = leveragedTus;
    }

    /**
     * Merge a given LeverageMatches object into this object. If the two object
     * don't have a common original source Tuv, an exception is thrown. When
     * this method is called, m_orderedMatchSegments are cleared. The field must
     * be populated later.
     * 
     * @param p_other
     *            LeverageMatches object that is merged into this object
     */
    public void merge(LeverageMatches p_other) throws Exception
    {
        if (p_other != null)
        {
            if (!m_originalSourceTuv.equals(p_other.m_originalSourceTuv))
            {
                throw new LingManagerException("LevMatchesMergeUnequalOrig",
                        null, null);
            }

            m_orderedMatchSegments = null;
            Iterator itOtherLeverageTu = p_other.m_leveragedTus.iterator();
            while (itOtherLeverageTu.hasNext())
            {
                add((LeveragedTu) itOtherLeverageTu.next());
            }
        }
    }
    
    public void merge(LeverageMatches p_other, GlobalSightLocale targetLocale) throws Exception
    {
        if (p_other != null)
        {
            if (!m_originalSourceTuv.equals(p_other.m_originalSourceTuv))
            {
                throw new LingManagerException("LevMatchesMergeUnequalOrig",
                        null, null);
            }

            m_orderedMatchSegments = null;
            Iterator itOtherLeverageTu = p_other.m_leveragedTus.iterator();
            while (itOtherLeverageTu.hasNext())
            {
                LeveragedTu ltu = (LeveragedTu) itOtherLeverageTu.next();
                GlobalSightLocale srcLocale = ltu.getSourceLocale();
                List<BaseTmTuv> tuvs = ltu.getTuvs();
                List<BaseTmTuv> removeTuvs = new ArrayList<BaseTmTuv>();
                for (BaseTmTuv baseTmTuv : tuvs)
                {
                    if (baseTmTuv.getLocale().equals(srcLocale)
                            || baseTmTuv.getLocale().equals(targetLocale))
                    {
                        continue;
                    }
                    else
                    {
                        removeTuvs.add(baseTmTuv);
                    }
                }

                for (BaseTmTuv baseTmTuv : removeTuvs)
                {
                    ltu.removeTuv(baseTmTuv);
                }

                add(ltu);
            }
        }
    }

    /**
     * Set Tuv id of the original source Tuv
     * 
     * @param p_id
     *            id of the original source Tuv
     */
    public void setOriginalSourceTuvId(long p_id)
    {
        m_originalSourceTuv.setId(p_id);
    }

    /**
     * Returns an Iterator that iterates all target locales. next() method of
     * this Iterator returns GlobalSightLocale object
     * 
     * @return Iterator
     */
    public Iterator targetLocaleIterator(long jobId)
    {
        if (m_orderedMatchSegments == null)
        {
            populateOrderedMatchSegments(jobId);
        }

        return m_orderedMatchSegments.targetLocaleIterator();
    }

    /**
     * Returns an Iterator that iterates matches for a given target. The number
     * of iteration doesn't exceed the user specified number of returned
     * matches. next() method of this Iterator returns LeveragedTuv object.
     * 
     * @param p_targetLocale
     *            target locale
     * @param p_jobId
     *            -- this is for SID comparison, only "LeverageMatch" object
     *            cares this. For others, "-1" is fine.
     * @return Iterator
     */
    public Iterator matchIterator(GlobalSightLocale p_targetLocale, long p_jobId)
    {
        if (m_orderedMatchSegments == null)
        {
            populateOrderedMatchSegments(p_jobId);
        }

        return m_orderedMatchSegments.matchIterator(p_targetLocale,
                m_leverageOptions.getNumberOfMatchesReturned(), Math.min(
                        m_leverageOptions.getMatchThreshold(),
                        FuzzyMatcher.MIN_MATCH_POINT));
    }

    // build m_orderedMatchSegments
    private void populateOrderedMatchSegments(long p_jobId)
    {
        m_orderedMatchSegments = new OrderedMatchSegments();
        m_orderedMatchSegments.populate(m_leveragedTus, m_leverageOptions,
                p_jobId);
    }

    private void setOptionAppliedScore(LeveragedTu p_tu) throws Exception
    {
        SegmentTmTuv sourceTuv = (SegmentTmTuv) m_originalSourceTuv;
        SegmentTmTuv matchedTuv = (SegmentTmTuv) p_tu.getSourceTuv();

        // check the difference of the source and the matched segments
        boolean wsDifferent = isWhitespaceDifferent(sourceTuv, matchedTuv);
        boolean caseDifferent = isCaseDifferent(sourceTuv, matchedTuv);
        boolean codeDifferent = isCodeDifferent(sourceTuv, matchedTuv);

        if (canBecomeExact(wsDifferent, caseDifferent, codeDifferent))
        {
            // if type sensitive option is on and the types are
            // different, demote the match
            if (m_leverageOptions.isTypeSensitiveLeveraging()
                    && !p_tu.getType().equals(m_originalSourceTuv.getType()))
            {
                p_tu.setMatchState(MatchState.TYPE_DIFFERENT);
                p_tu.setScore(100 - m_leverageOptions
                        .getTypeDifferencePenalty());
            }
            // if code is different but the code difference cannot
            // allow 100% match, demote the match
            // GBS-615, add code-sensitive leveraging check
            else if (m_leverageOptions.isCodeSensitiveLeveraging()
                    && codeDifferent
                    && !canCodeDifferenceBe100Percent(sourceTuv, matchedTuv))
            {
                p_tu.setMatchState(MatchState.CODE_DIFFERENT);
                p_tu.setScore(100 - m_leverageOptions
                        .getCodeDifferencePenalty());
            }
            else
            {
                // score stays 100%. Find a right state.
                p_tu.setMatchState(getMatchStateForQuasiExact(wsDifferent,
                        caseDifferent, codeDifferent));
            }
        }
        // Can not be 100%. Demote the match.
        else
        {
            p_tu.setMatchState(getMatchStateForDemoted(wsDifferent,
                    caseDifferent, codeDifferent));
            p_tu.setScore(getMatchScore(wsDifferent, caseDifferent,
                    codeDifferent));
        }
    }

    private boolean canBecomeExact(boolean p_wsDifferent,
            boolean p_caseDifferent, boolean p_codeDifferent)
    {
        boolean canExact = true;

        if ((p_wsDifferent && m_leverageOptions
                .isWhiteSpaceSensitiveLeveraging())
                || (p_caseDifferent && m_leverageOptions
                        .isCaseSensitiveLeveraging())
                || (p_codeDifferent && m_leverageOptions
                        .isCodeSensitiveLeveraging()))
        {
            canExact = false;
        }

        return canExact;
    }

    private MatchState getMatchStateForDemoted(boolean p_wsDifferent,
            boolean p_caseDifferent, boolean p_codeDifferent)
    {
        MatchState state = null;

        if (p_wsDifferent
                && m_leverageOptions.isWhiteSpaceSensitiveLeveraging())
        {
            state = MatchState.WHITESPACE_DIFFERENT;
        }
        else if (p_caseDifferent
                && m_leverageOptions.isCaseSensitiveLeveraging())
        {
            state = MatchState.CASE_DIFFERENT;
        }
        else if (p_codeDifferent
                && m_leverageOptions.isCodeSensitiveLeveraging())
        {
            state = MatchState.CODE_DIFFERENT;
        }
        else
        {
            // shouldn't reach here. Use NOT_A_MATCH to indicate an
            // error state
            state = MatchState.NOT_A_MATCH;
        }

        return state;
    }

    private MatchState getMatchStateForQuasiExact(boolean p_wsDifferent,
            boolean p_caseDifferent, boolean p_codeDifferent)
    {
        MatchState state = null;

        if (p_wsDifferent)
        {
            state = MatchState.WHITESPACE_DIFFERENT;
        }
        else if (p_caseDifferent)
        {
            state = MatchState.CASE_DIFFERENT;
        }
        else if (p_codeDifferent)
        {
            state = MatchState.CODE_DIFFERENT;
        }
        else
        {
            // shouldn't reach here. Use NOT_A_MATCH to indicate an
            // error state
            state = MatchState.NOT_A_MATCH;
        }

        return state;
    }

    private int getMatchScore(boolean p_wsDifferent, boolean p_caseDifferent,
            boolean p_codeDifferent)
    {
        int score = 100;

        if (p_wsDifferent
                && m_leverageOptions.isWhiteSpaceSensitiveLeveraging())
        {
            score = score - m_leverageOptions.getWhiteSpaceDifferencePenalty();
        }

        if (p_caseDifferent && m_leverageOptions.isCaseSensitiveLeveraging())
        {
            score = score - m_leverageOptions.getCaseDifferencePenalty();
        }

        if (p_codeDifferent && m_leverageOptions.isCodeSensitiveLeveraging())
        {
            score = score - m_leverageOptions.getCodeDifferencePenalty();
        }

        return score;
    }

    private boolean isWhitespaceDifferent(SegmentTmTuv p_sourceTuv,
            SegmentTmTuv p_matchedTuv) throws Exception
    {
        String sourceString;
        String matchString;

        // remove code
        sourceString = p_sourceTuv.getNoCodeFormat();
        matchString = p_matchedTuv.getNoCodeFormat();

        // normalize case
        sourceString = sourceString.toLowerCase(((SegmentTmTu) p_sourceTuv
                .getTu()).getSourceLocale().getLocale());
        matchString = matchString.toLowerCase(((SegmentTmTu) p_matchedTuv
                .getTu()).getSourceLocale().getLocale());

        return !sourceString.equals(matchString);
    }

    private boolean isCaseDifferent(SegmentTmTuv p_sourceTuv,
            SegmentTmTuv p_matchedTuv) throws Exception
    {
        String sourceString;
        String matchString;

        // remove code
        sourceString = p_sourceTuv.getNoCodeFormat();
        matchString = p_matchedTuv.getNoCodeFormat();

        // normalize whitespace
        sourceString = Text.normalizeWhiteSpaceForTm(" " + sourceString + " ");
        matchString = Text.normalizeWhiteSpaceForTm(" " + matchString + " ");

        return !sourceString.equals(matchString);
    }

    private boolean isCodeDifferent(SegmentTmTuv p_sourceTuv,
            SegmentTmTuv p_matchedTuv) throws Exception
    {
        String sourceString;
        String matchString;

        // get exact match string
        sourceString = p_sourceTuv.getExactMatchFormat();
        matchString = p_matchedTuv.getExactMatchFormat();

        // normalize case
        sourceString = sourceString.toLowerCase(((SegmentTmTu) p_sourceTuv
                .getTu()).getSourceLocale().getLocale());
        matchString = matchString.toLowerCase(((SegmentTmTu) p_matchedTuv
                .getTu()).getSourceLocale().getLocale());

        // normalize whitespace
        sourceString = Text.normalizeWhiteSpaceForTm(" " + sourceString + " ");
        matchString = Text.normalizeWhiteSpaceForTm(" " + matchString + " ");

        return !sourceString.equals(matchString);
    }

    private boolean canCodeDifferenceBe100Percent(
            SegmentTmTuv p_originalSourceTuv, SegmentTmTuv p_matchedSourceTuv)
            throws Exception
    {
        TmxTagStatistics originalStatistics = p_originalSourceTuv
                .getTmxTagStatistics();
        TmxTagStatistics matchedStatistics = p_matchedSourceTuv
                .getTmxTagStatistics();

        if (c_logger.isDebugEnabled())
        {
            c_logger.debug("originalStatistics = "
                    + originalStatistics.toDebugString());
            c_logger.debug("matchedStatistics = "
                    + matchedStatistics.toDebugString());            
        }

        return originalStatistics.areSame(matchedStatistics);
    }

    private float ngramScore(String p_originalText, String p_matchedText)
            throws Exception
    {
        NgramTokenizer tokenizer = new NgramTokenizer();

        // parameter 2 to 6 of NgramTokenizer.tokenize() method are
        // all dummy.
        List originalTokens = tokenizer.tokenize(p_originalText, 1, 1, 1, null,
                true);
        List matchedTokens = tokenizer.tokenize(p_matchedText, 1, 1, 1, null,
                true);

        int originalTokensCount = ((Token) originalTokens.get(0))
                .getTotalTokenCount();
        int matchedTokensCount = ((Token) matchedTokens.get(0))
                .getTotalTokenCount();

        // build a map of original tokens
        Map originalTokensMap = new HashMap();
        Iterator it = originalTokens.iterator();
        while (it.hasNext())
        {
            Token token = (Token) it.next();
            originalTokensMap.put(token.getTokenString(), token);
        }

        int sharedTokenCount = 0;

        // find out the shared token count
        it = matchedTokens.iterator();
        while (it.hasNext())
        {
            Token matchToken = (Token) it.next();
            Token orgToken = (Token) originalTokensMap.get(matchToken
                    .getTokenString());

            if (orgToken != null)
            {
                sharedTokenCount += Math.min(matchToken.getRepetition(),
                        orgToken.getRepetition());
            }
        }

        float result = ((float) (2 * sharedTokenCount)
                / (originalTokensCount + matchedTokensCount) * 100);

        return result;
    }

    public LeverageOptions getLeverageOptions()
    {
        return m_leverageOptions;
    }

    private void setRefTmScores(LeveragedTu p_tu) throws Exception
    {
        if (m_leverageOptions.isRefTm())
        {
            if (m_leverageOptions.getRefTmPenalty() != 0)
            {
                String refTms = m_leverageOptions.getRefTMsToLeverageFrom();
                if (!StringUtil.isEmpty(refTms))
                {
                    List<Long> refTmIdsForPenalty = new ArrayList<Long>(); 
                    String[] ids = refTms.split(",");
                    for (int i=0; i<ids.length; i++)
                    {
                        try {
                            refTmIdsForPenalty.add(Long.parseLong(ids[i]));
                        } catch (Exception ignore) {

                        }
                    }
                    if (refTmIdsForPenalty.contains(p_tu.getTmId()))
                    {
                        float score = (p_tu.getScore() - m_leverageOptions
                                .getRefTmPenalty());
                        p_tu.setScore(score);
                        p_tu.setMatchState(MatchState.FUZZY_MATCH);
                    }
                }
            }
        }
    }

    /**
     * Try to replace all placeholders of the specified tu.
     * <p>
     * To qualify for this type of repairing, a TM match must meet the following
     * criteria:
     * <p>
     * 1. The TM match must be identified as a WorldServer TM entry (in the new
     * column).
     * <p>
     * 2. The match must be exact except for the differences between
     * placeholders/tags. In other words, if you removed the tags/placeholders
     * from the source content and the TM match, they would be identical.
     * <p>
     * 3. The TM match source segment placeholders must be in the same positions
     * as the tags in the source content segment.
     * 
     * @param tu
     * @throws Exception
     */
    private void repairPlaceholders(LeveragedTu tu) throws Exception
    {
        SegmentTmTuv sourceTuv = (SegmentTmTuv) m_originalSourceTuv;
        SegmentTmTuv matchedTuv = (SegmentTmTuv) tu.getSourceTuv();

        if (isCodeDifferent(sourceTuv, matchedTuv))
        {
            PlaceholderHander hander = new PlaceholderHander(
                    (SegmentTmTuv) m_originalSourceTuv, (SegmentTmTu) tu,
                    m_leverageOptions.getSaveTmId());
            hander.repair();
        }
    }

    public Job getJob()
    {
        return job;
    }

    public void setJob(Job job)
    {
        this.job = job;
    }

    public void removeTargetLocale(GlobalSightLocale gsl)
    {
        m_orderedMatchSegments = null;

        for (Iterator it = m_leveragedTus.iterator(); it.hasNext();)
        {
            LeveragedTu tu = (LeveragedTu) it.next();
            tu.removeTuvsForLocale(gsl);
        }
        
    }
}
