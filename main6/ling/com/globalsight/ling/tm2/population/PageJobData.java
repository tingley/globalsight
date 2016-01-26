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
package com.globalsight.ling.tm2.population;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.globalsight.everest.integration.ling.LingServerProxy;
import com.globalsight.everest.integration.ling.tm2.LeverageMatch;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.persistence.tuv.SegmentTuTuvAttributeUtil;
import com.globalsight.everest.persistence.tuv.SegmentTuvUtil;
import com.globalsight.everest.tuv.TuTuvAttributeImpl;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.tuv.TuvMerger;
import com.globalsight.everest.tuv.TuvState;
import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.ling.tm2.PageTmTu;
import com.globalsight.ling.tm2.PageTmTuv;
import com.globalsight.ling.tm2.leverage.LeverageOptions;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.SortUtil;
import com.globalsight.util.StringUtil;
import com.globalsight.util.edit.GxmlUtil;
import com.globalsight.util.gxml.GxmlElement;
import com.globalsight.util.gxml.GxmlFragmentReader;
import com.globalsight.util.gxml.GxmlFragmentReaderPool;

/**
 * A repository of job data for a page. This class holds all source and target
 * job data segments (from translation_unit_variant and translation_unit table)
 * that belong to a page.
 */

public class PageJobData
{
    private static final Logger c_logger = Logger.getLogger(PageJobData.class
            .getName());

    private static final boolean EXCLUDE_STATE = true;
    private static final boolean INCLUDE_STATE = false;

    // a list of merged PageTmTu
    private List<PageTmTu> m_mergedTus = null;

    // tentative list of PageTmTu (unmerged yet)
    private List<PageTmTu> m_tentativeTus = null;

    private GlobalSightLocale m_sourceLocale;
    private long m_jobId;
    private String m_jobName = null;
    private Timestamp now = null;

    /**
     * Constructor.
     * 
     * @param p_sourceLocale
     *            Source locale (GlobalSightLocale)
     */
	public PageJobData(GlobalSightLocale p_sourceLocale, long p_jobId,
			String p_jobName)
    {
        m_mergedTus = null; // initially null

        m_tentativeTus = new ArrayList<PageTmTu>();
        m_sourceLocale = p_sourceLocale;
        m_jobId = p_jobId;
        m_jobName = p_jobName;
        now = new Timestamp(System.currentTimeMillis());
    }

    public void addTu(PageTmTu p_tu, GlobalSightLocale sourceLocale)
    {
    	PageTmTu previousTu = null;
        if (m_tentativeTus.size() > 0)
        {
        	previousTu = m_tentativeTus.get(m_tentativeTus.size() - 1);
        }
    	m_tentativeTus.add(p_tu);
        // Use current date as "creationDate", "modifyDate" and "lastUsageDate".
    	BaseTmTuv curSrcTuv = p_tu.getFirstTuv(sourceLocale);
    	for (BaseTmTuv curTuv : p_tu.getTuvs())
        {
        	curTuv.setJobId(m_jobId);
        	curTuv.setJobName(m_jobName);
        	curTuv.setLastUsageDate(now);
        	curTuv.setCreationDate(now);
        	curTuv.setModifyDate(now);

			// TUV uses its previous source tuv's hash value as previous
			// hash, and use next source tuv's hash value as next hash. Never
			// store target tuv's hash value.
        	// Will this be an issue in the further?
        	curTuv.setPreviousHash(BaseTmTuv.FIRST_HASH);
    		curTuv.setNextHash(BaseTmTuv.LAST_HASH);
        	if (previousTu != null)
        	{
        		BaseTmTuv preSrcTuv = previousTu.getFirstTuv(sourceLocale);
				BaseTmTuv preTuv = previousTu.getFirstTuv(curTuv.getLocale());
				preTuv.setNextHash(SegmentTuvUtil.getHashValue(curSrcTuv.getSegment()));
				curTuv.setPreviousHash(SegmentTuvUtil.getHashValue(preSrcTuv.getSegment()));
        	}
        }
    }

    /**
     * Get a Collection of Tus that have all Tuvs to be saved in Page Tm
     * 
     * @param p_options
     *            LeverageOptions object
     */
    public Collection<PageTmTu> getTusToSaveToPageTm(LeverageOptions p_options)
            throws Exception
    {
        boolean saveUntranslated = p_options.savesUntranslatedInPageTm();
        return getTusToSave(saveUntranslated);
    }

    /**
     * Get a Collection of Tus that have all Tuvs to be saved in Segment Tm
     * 
     * @param p_options
     *            LeverageOptions object
     */
	public Collection<PageTmTu> getTusToSaveToSegmentTm(
			LeverageOptions p_options, Set<GlobalSightLocale> p_targetLocales,
			SourcePage p_page) throws Exception
	{
		boolean saveUntranslated = p_options.savesUntranslatedInSegmentTm();
		boolean saveLocailzed = p_options.saveLocalizedInSegmentTM();
		boolean saveApproved = p_options.savesApprovedInSegmentTm();
		boolean saveExactMatch = p_options.savesExactMatchInSegmentTm();
		boolean saveWhollyInternalText = p_options.saveWhollyInternalTextSegmentTm();
		return getTusToSave(saveUntranslated, saveLocailzed, saveApproved,
				saveExactMatch, saveWhollyInternalText, p_targetLocales, p_page);
	}

    /**
     * Get a Collection of Tus of which all Tuvs' state is COMPLETE
     */
    public Collection<PageTmTu> getCompleteTus() throws Exception
    {
        Set<String> states = new HashSet<String>();
        states.add(TuvState.COMPLETE.getName());
        return getTusByState(states, INCLUDE_STATE);
    }

    // Returns a Collection of Tus in this object that have Tuvs that
    // are saved in the TM. If p_saveUntranslated is true, all Tus and
    // Tuvs are returned. If not, only Tuvs that are not NOT_LOCALIZED
    // are returned.
    //To Page TM
    private Collection<PageTmTu> getTusToSave(boolean p_saveUntranslated)
            throws Exception
    {
        populateMergedTus();

        Collection<PageTmTu> tuList = null;
        if (p_saveUntranslated)
        {
            tuList = m_mergedTus;
        }
        else
        {
            Set<String> states = new HashSet<String>();
            states.add(TuvState.NOT_LOCALIZED.getName());
            states.add(TuvState.APPROVED.getName());
            states.add(TuvState.DO_NOT_TRANSLATE.getName());
            tuList = getTusByState(states, EXCLUDE_STATE);
        }

        return tuList;
    }
    
    //To Project TM
	private Collection<PageTmTu> getTusToSave(boolean p_saveUntranslated,
			boolean p_saveLocalized, boolean p_saveApproved,
			boolean p_saveExactMatch, boolean saveWhollyInternalText,
			Set<GlobalSightLocale> p_targetLocales, SourcePage p_page)
			throws Exception
	{
		populateMergedTus();

		Collection<PageTmTu> tuList = null;
		Set<String> excludeStates = new HashSet<String>();
		excludeStates.add(TuvState.COMPLETE.getName());

		if (!p_saveUntranslated)
		{
			excludeStates.add(TuvState.NOT_LOCALIZED.getName());
		}
		if (!p_saveLocalized)
		{
			excludeStates.add(TuvState.LOCALIZED.getName());
		}
		if (!saveWhollyInternalText)
		{
			excludeStates.add(TuvState.DO_NOT_TRANSLATE.getName());
		}
		if (!p_saveApproved)
		{
			excludeStates.add(TuvState.APPROVED.getName());
		}
		
		tuList = getTusByState(excludeStates, EXCLUDE_STATE);

		if (!p_saveExactMatch)
			return filterExactMatchData(tuList, p_targetLocales, p_page.getId());
		else
			return tuList;
	}

	/**
	 * GBS-4068 : No new TU (with SID) created in the storage TM for AuthorIT SID
	 * */
	private Collection<PageTmTu> filterExactMatchData(
			Collection<PageTmTu> tuList,
			Set<GlobalSightLocale> p_targetLocales, long pageId)
	{
		ArrayList<PageTmTu> returnTuList = null;
		Map<String, Set<LeverageMatch>> leverageMatchMap = new HashMap<String, Set<LeverageMatch>>();
		List<LeverageMatch> leverageMatches = null;
		for (GlobalSightLocale targetLocale : p_targetLocales)
		{
			leverageMatches = LingServerProxy.getLeverageMatchLingManager()
					.getExactLeverageMatches(pageId, targetLocale.getId());
			for (LeverageMatch match : leverageMatches)
			{
				String key = match.getOriginalSourceTuvId() + "_"
						+ targetLocale.getId();
				Set<LeverageMatch> set = (TreeSet<LeverageMatch>) leverageMatchMap
						.get(key);
				if (set == null)
				{
					set = new TreeSet<LeverageMatch>();
					leverageMatchMap.put(key, set);
				}
				set.add(match);
			}
		}
		
		if (!leverageMatchMap.isEmpty())
		{
			returnTuList = new ArrayList<PageTmTu>();
			Set<LeverageMatch> leverageMatchSet = null;
			for (PageTmTu tu : tuList)
			{
				PageTmTu clonedTu = (PageTmTu) tu.clone();
				PageTmTuv sourceTuv = (PageTmTuv) tu
						.getFirstTuv(m_sourceLocale);
				clonedTu.addTuv(sourceTuv);

				Iterator itLocale = tu.getAllTuvLocales().iterator();
				while (itLocale.hasNext())
				{
					GlobalSightLocale tuvLocale = (GlobalSightLocale) itLocale
							.next();
					if (!tuvLocale.equals(m_sourceLocale))
					{
						PageTmTuv tuv = (PageTmTuv) tu.getFirstTuv(tuvLocale);
						leverageMatchSet = leverageMatchMap.get(sourceTuv
								.getId() + "_" + tuvLocale.getId());
						if (leverageMatchSet != null)
						{
							if (TuvState.EXACT_MATCH_LOCALIZED.getName()
									.equals(tuv.getState()))
							{
								for (LeverageMatch match : leverageMatchSet)
								{
									String mathcText = GxmlUtil
											.stripRootTag(match
													.getMatchedText());
									if (mathcText.equals(tuv.getSegmentNoTopTag()))
									{
										if (match.getSid() != null && tuv.getSid() != null
												&& !match.getSid().equals(tuv.getSid()))
										{
											clonedTu.addTuv(tuv);
										}
										else if (match.getSid() == null && tuv.getSid() != null)
										{
											clonedTu.addTuv(tuv);
										}
									}
								}
							}
							else
							{
								clonedTu.addTuv(tuv);
							}
						}
						else
						{
							clonedTu.addTuv(tuv);
						}
					}
				}

				if (clonedTu.getTuvSize() > 1)
				{
					returnTuList.add(clonedTu);
				}
			}
		}
		if (returnTuList != null)
			return returnTuList;
		else 
			return tuList;
	}
    /**
     * Returns a Collection of Tus in this object that have Tuvs that satisfies
     * the condition specified by the parameters. If p_excludeState is true,
     * Tuvs with the state not the same as p_state are returned. If
     * p_excludeState is false, Tuvs with the same state as p_state are
     * returned.
     * 
     * @param p_state
     *            state of Tuv
     * @param p_excludeState
     *            indicates whether Tuvs returned have p_state or not have
     *            p_state
     */
    @SuppressWarnings("rawtypes")
	private Collection<PageTmTu> getTusByState(Set<String> p_states,
            boolean p_excludeState) throws Exception
    {
        populateMergedTus();

        ArrayList<PageTmTu> tuList = new ArrayList<PageTmTu>();
        for (PageTmTu tu : m_mergedTus)
        {
            PageTmTu clonedTu = (PageTmTu) tu.clone();

            Iterator itLocale = tu.getAllTuvLocales().iterator();
            while (itLocale.hasNext())
            {
                GlobalSightLocale tuvLocale = (GlobalSightLocale) itLocale
                        .next();
                PageTmTuv tuv = (PageTmTuv) tu.getFirstTuv(tuvLocale);

                // Source Tuvs are added regardless of its state
                if (tuvLocale.equals(m_sourceLocale)
                        || (p_excludeState ^ p_states.contains(tuv.getState())))
                {
                    clonedTu.addTuv(tuv);
                }
            }

            // only Tus that have some target Tuvs are added to
            // the list. Source Tuv is always added to the Tu so
            // the smallest amount of Tuv is 1.
            if (clonedTu.getTuvSize() > 1)
            {
                tuList.add(clonedTu);
            }
        }

        return tuList;
    }

    // populate m_mergedTus if it's null
    @SuppressWarnings({ "rawtypes", "unchecked" })
	private void populateMergedTus() throws Exception
    {
        if (m_mergedTus == null)
        {
            m_mergedTus = new ArrayList<PageTmTu>();

            Map<GlobalSightLocale, List<TuvPair>> trgTuvMap = new HashMap<GlobalSightLocale, List<TuvPair>>();

            for (PageTmTu tu : m_tentativeTus)
            {
                // make a new HashSet to avoid concurrent modification
                // exception. And also remove source locale.
                HashSet trgLocales = new HashSet(tu.getAllTuvLocales());
                trgLocales.remove(m_sourceLocale);

                Iterator itLocale = trgLocales.iterator();
                while (itLocale.hasNext())
                {
                    GlobalSightLocale locale = (GlobalSightLocale) itLocale
                            .next();

                    PageTmTuv tuv = (PageTmTuv) tu.getFirstTuv(locale);
                    String mergeState = tuv.getMergeState();

                    if (!mergeState.equals(Tuv.NOT_MERGED))
                    {
                        List<TuvPair> mergeTuvs = trgTuvMap.get(locale);
                        if (mergeTuvs == null)
                        {
                            mergeTuvs = new ArrayList<TuvPair>();
                            trgTuvMap.put(locale, mergeTuvs);
                        }

                        PageTmTuv srcTuv = (PageTmTuv) tu
                                .getFirstTuv(m_sourceLocale);
                        mergeTuvs.add(new TuvPair(srcTuv, tuv));
                        tu.removeTuv(tuv);

                        if (mergeState.equals(Tuv.MERGE_END))
                        {
                            PageTmTu mergedTu = createMergedTu(mergeTuvs);
                            mergeTuvs.clear();

                            m_mergedTus.add(mergedTu);
                        }
                    }
                }

                // if there are any target segment remains in the TU
                if (tu.getTuvSize() > 1)
                {
                    m_mergedTus.add(tu);
                }
            }

            // fix missing x attribute
            List<Long> tuvIds = new ArrayList<Long>();
            for (PageTmTu tu : m_mergedTus)
            {
                TmxTagRepairer.fixMissingX(tu, m_sourceLocale);
                for (BaseTmTuv tuv :tu.getTuvs())
                {
                	tuvIds.add(tuv.getId());
                }
            }

            // load SID from "translation_tu_tuv_attr_xx" table
    		List<TuTuvAttributeImpl> sidAttrs = SegmentTuTuvAttributeUtil
    				.getSidAttributesByTuvIds(tuvIds, m_jobId);
    		HashMap<Long, String> sidAttrMap = new HashMap<Long, String>();
    		for (TuTuvAttributeImpl sidAttr : sidAttrs)
    		{
    			sidAttrMap.put(sidAttr.getObjectId(), sidAttr.getTextValue());
    		}

            for (PageTmTu tu : m_mergedTus)
    		{
                for (BaseTmTuv tuv :tu.getTuvs())
                {
        			if (StringUtil.isNotEmpty(sidAttrMap.get(tuv.getId())))
        			{
        				tuv.setSid(sidAttrMap.get(tuv.getId()));
        			}
                }
    		}

    		// renumber sub ids. Fix for 12870
            // Need to do this to all segments (not only merged
            // segments) so that leveraged segments will also be
            // corrected.
            renumberSubIds();
        }

    }

    // create merged TU from a list of TUVs
    private PageTmTu createMergedTu(List p_tuvs) throws Exception
    {
        // get target segment
        TuvPair firstTuvPair = (TuvPair) p_tuvs.get(0);
        PageTmTuv targetTuv = firstTuvPair.getTargetTuv();

        // get source segment
        List<String> sourceTextList = new ArrayList<String>();

        Iterator it = p_tuvs.iterator();
        while (it.hasNext())
        {
            TuvPair tuvPair = (TuvPair) it.next();
            PageTmTuv srcTuv = tuvPair.getSourceTuv();
            sourceTextList.add(srcTuv.getSegment());
        }

        String sourceText = TuvMerger.mergeStrings(sourceTextList);
        PageTmTuv firstSourceTuv = firstTuvPair.getSourceTuv();
        PageTmTuv sourceTuv = (PageTmTuv) firstSourceTuv.clone();
        sourceTuv.setSegment(sourceText);
        sourceTuv.setExactMatchKey();

        PageTmTu tu = (PageTmTu) firstSourceTuv.getTu().clone();
        tu.addTuv(sourceTuv);
        tu.addTuv(targetTuv);

        return tu;
    }

    /**
     * This method tries to renumber the sub ids that are not consecutive due to
     * the segment merge. The sub ids can be 1, 2, 101, 102 when two segments
     * are merged. This method renumbers them to 1, 2, 3, 4.
     * 
     * This method should be called on matches from Page TM.
     */
    private void renumberSubIds() throws Exception
    {
        GxmlFragmentReader reader = null;
        try
        {
            reader = GxmlFragmentReaderPool.instance().getGxmlFragmentReader();

            for (Iterator itTu = m_mergedTus.iterator(); itTu.hasNext();)
            {
                PageTmTu tu = (PageTmTu) itTu.next();

                for (Iterator itLocale = tu.getAllTuvLocales().iterator(); itLocale
                        .hasNext();)
                {
                    GlobalSightLocale locale = (GlobalSightLocale) itLocale
                            .next();

                    for (Iterator itTuv = tu.getTuvList(locale).iterator(); itTuv
                            .hasNext();)
                    {
                        PageTmTuv tuv = (PageTmTuv) itTuv.next();
                        doRenumberSubIds(tuv, reader);
                    }
                }
            }
        }

        finally
        {
            GxmlFragmentReaderPool.instance().freeGxmlFragmentReader(reader);
        }
    }

    private void doRenumberSubIds(PageTmTuv p_tuv,
            GxmlFragmentReader p_gxmlReader) throws Exception
    {
        String segment = p_tuv.getSegment();
        GxmlElement gxmlElement = p_gxmlReader.parseFragment(segment);
        List subs = gxmlElement.getDescendantElements(new int[]
        { GxmlElement.SUB });

        ArrayList subIdList = new ArrayList();
        HashMap subIdMap = new HashMap();
        for (Iterator it = subs.iterator(); it.hasNext();)
        {
            GxmlElement sub = (GxmlElement) it.next();
            Integer subId = sub.getAttributeAsInteger("id");
            if (subId != null)
            {
                subIdMap.put(subId, sub);
                subIdList.add(subId);
            }
        }

        SortUtil.sort(subIdList);
        for (int i = 0; i < subIdList.size(); i++)
        {
            Integer subId = (Integer) subIdList.get(i);
            GxmlElement sub = (GxmlElement) subIdMap.get(subId);
            sub.setAttribute("id", Integer.toString(i + 1));
        }

        segment = gxmlElement.toGxml();
        p_tuv.setSegment(segment);
    }

    private class TuvPair
    {
        private PageTmTuv m_sourceTuv;
        private PageTmTuv m_targetTuv;

        private TuvPair(PageTmTuv p_sourceTuv, PageTmTuv p_targetTuv)
        {
            m_sourceTuv = p_sourceTuv;
            m_targetTuv = p_targetTuv;
        }

        private PageTmTuv getSourceTuv()
        {
            return m_sourceTuv;
        }

        private PageTmTuv getTargetTuv()
        {
            return m_targetTuv;
        }
    }

}
