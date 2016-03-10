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

import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.globalsight.everest.edit.EditHelper;
import com.globalsight.everest.edit.online.OnlineEditorManagerLocal;
import com.globalsight.everest.integration.ling.tm2.LeverageMatch;
import com.globalsight.everest.integration.ling.tm2.MatchTypeStatistics;
import com.globalsight.everest.integration.ling.tm2.Types;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.persistence.tuv.BigTableUtil;
import com.globalsight.everest.persistence.tuv.SegmentTuvUtil;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.tuv.Tu;
import com.globalsight.everest.tuv.TuImpl;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.tuv.TuvImpl;
import com.globalsight.everest.tuv.TuvState;
import com.globalsight.everest.webapp.pagehandler.edit.online.EditorHelper;
import com.globalsight.ling.inprogresstm.DynamicLeveragedSegment;
import com.globalsight.ling.tm.LeverageMatchLingManager;
import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.ling.tm2.SegmentTmTu;
import com.globalsight.ling.tm2.SegmentTmTuv;
import com.globalsight.ling.tm2.leverage.DateComparable;

public class LeverageUtil
{
    public static final String DUMMY_SUBID = "0";
    static private final Logger logger = Logger.getLogger(LeverageUtil.class);

    public static final int ICE_TYPE_NOT_ICE = 0;
    public static final int ICE_TYPE_SID_MATCHING = 1;
    public static final int ICE_TYPE_HASH_MATCHING = 2;
    public static final int ICE_TYPE_BRACKETED_MATCHING = 3;
    public static final int ICE_TYPE_PASSOLO_MATCHING = 4;
    public static final int ICE_TYPE_PO_XLF_MATCHING = 5;

    /**
     * Compares the sid of the two {@link LeveragedTuv} instances.
     * <p>
     * {@link LeveragedTuv} instance has original sid and matched sid, if the
     * original sid is same as matched sid, the priority will be higher.
     * <p>
     * In other words, the return value will be -1 only if the first original
     * sid is same as the matched sid, and the second {@link LeveragedTuv}
     * instance not.
     * 
     * @param tuv1
     *            The first {@link LeveragedTuv} instance
     * @param tuv2
     *            The second {@link LeveragedTuv} instance
     * @return -1, 0, or 1, depends on the sids of the two {@link LeveragedTuv}
     *         instances
     */
    public static int compareSid(SidComparable tuv1, SidComparable tuv2,
            long p_jobId)
    {
        return getSidCompareRusult(tuv2, p_jobId)
                - getSidCompareRusult(tuv1, p_jobId);
    }

    /**
     * Compares orgSid with sid of the specified <code>LeveragedTuv</code>.
     * 
     * @param tuv
     *            The specified <code>LeveragedTuv</code>.
     * @return 0 or 1. Returns 1 if the orgSid matched sid, others 0.
     */
    public static int getSidCompareRusult(SidComparable tuv, long p_jobId)
    {
        String orgSid = tuv.getOrgSid(p_jobId);

        // If the dynamic leveraged segment is NOT from gold TM, will not
        // compare SID.
        boolean flag = true;
        if (tuv instanceof DynamicLeveragedSegment)
        {
            DynamicLeveragedSegment dynamicLevSegment = (DynamicLeveragedSegment) tuv;
            int matchCategory = dynamicLevSegment.getMatchCategory();
            flag = (matchCategory == DynamicLeveragedSegment.FROM_GOLD_TM);
        }

        if (flag && orgSid != null && orgSid.equals(tuv.getSid()))
        {
            return 1;
        }

        return 0;
    }

	public static int compareLastUsageDate(DateComparable tuv1,
			DateComparable tuv2, LeverageOptions leverageOptions)
	{
		int mode = getDateMode(leverageOptions);

		Date time1 = tuv1.getLastUsageDate();
		Date time2 = tuv2.getLastUsageDate();

		return compareTime(time1, time2, mode);
	}

    /**
     * This method compares the last modified time of the two Tuvs. The order of
     * the sort depends on the option the user chose.
     * 
     * @param tuv1
     * @param tuv2
     * @param leverageOptions
     * @return
     */
    public static int compareModifyTime(DateComparable tuv1,
            DateComparable tuv2, LeverageOptions leverageOptions)
    {
        int mode = getDateMode(leverageOptions);

        Date time1 = tuv1.getModifyDate();
        Date time2 = tuv2.getModifyDate();

        return compareTime(time1, time2, mode);
    }

    private static int compareTime(Date time1, Date time2, int mode)
    {
    	if (time2 == null || time1 == null)
    		return 0;

    	int result = time2.compareTo(time1);
        if (mode == LeverageOptions.PICK_OLDEST)
        {
            result = -result;
        }

        return result;
    }

    public static int getDateMode(LeverageOptions leverageOptions)
    {
        if (leverageOptions == null)
        {
            return LeverageOptions.PICK_LATEST;
        }

        int mode;
        if (leverageOptions.isLatestLeveragingForReimport())
        {
            mode = LeverageOptions.PICK_LATEST;
        }
        else
        {
            mode = leverageOptions.getMultipleExactMatcheMode();
        }

        return mode;
    }

    @SuppressWarnings("rawtypes")
	public static boolean isIncontextMatch(Tuv srcTuv,
            List<TuvImpl> sourceTuvs, List targetTuvs,
            MatchTypeStatistics tuvMatchTypes,
            Vector<String> excludedItemTypes, long p_jobId)
    {
        if (srcTuv == null)
            return false;

        try
        {
            int index = -1;

            for (int i = 0; i < sourceTuvs.size(); i++)
            {
                if (srcTuv.getId() == sourceTuvs.get(i).getId())
                {
                    index = i;
                    break;
                }
            }

            if (index == -1)
                return false;

            return isIncontextMatch(index, sourceTuvs, targetTuvs,
                    tuvMatchTypes, excludedItemTypes, p_jobId);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
        }

        return false;
    }

    /**
     * Checks the match type is in-context match or not. Note that this only
     * check "DUMMY_SUBID" segment.
     */
    @SuppressWarnings("rawtypes")
	public static boolean isIncontextMatch(int index, List p_sourceTuvs,
            List p_targetTuvs, MatchTypeStatistics p_matchTypes,
            Vector<String> p_excludedItemTypes, long p_jobId)
    {
    	String subId = getSubId(index, p_sourceTuvs);
        return isIncontextMatch(index, p_sourceTuvs, p_targetTuvs,
                p_matchTypes, p_excludedItemTypes, subId, p_jobId);
    }

	@SuppressWarnings("rawtypes")
	public static boolean isIncontextMatch(int index, List p_sourceTuvs,
			List p_targetTuvs, MatchTypeStatistics p_matchTypes,
			Vector<String> p_excludedItemTypes, String p_subId, long p_jobId)
	{
		int iceType = getIsIncontextMatchType(index, p_sourceTuvs,
				p_targetTuvs, p_matchTypes,
				p_excludedItemTypes, p_subId, p_jobId);
		if (iceType > 0)
			return true;
		else
			return false;
	}

	/**
     * Checks the match type is in-context match or not.
     * 
     * <p>
     * Please note that the class of tuv may be <code>Tuv</code> or
     * <code>SegmentTmTuv</code>. The <code>p_targetTuvs</code> can be null If
     * the class of tuv is <code>SegmentTmTuv</code>.
     * 
     * @param index
     * @param p_sourceTuvs
     * @param p_targetTuvs
     * @param p_matchTypes
     * @param p_excludedItemTypes
     * @return
     */
	@SuppressWarnings("rawtypes")
	public static int getIsIncontextMatchType(int index, List p_sourceTuvs,
			List p_targetTuvs, MatchTypeStatistics p_matchTypes,
			Vector<String> p_excludedItemTypes, String p_subId, long p_jobId)
    {
        if (isSIDContextMatch(index, p_sourceTuvs, p_matchTypes))
        {
            return ICE_TYPE_SID_MATCHING;
        }

        if (isPassoloIncontextMatch(p_sourceTuvs.get(index), p_matchTypes, p_jobId))
        {
            return ICE_TYPE_PASSOLO_MATCHING;
        }

        // For PO segment,if its target is valid, count it as "ICE" directly.
        if (isPoXlfICE(index, p_sourceTuvs, p_matchTypes, p_jobId))
        {
            return ICE_TYPE_PO_XLF_MATCHING;
        }

        if (isSidExistsAndNotEqual(p_sourceTuvs.get(index), p_matchTypes))
        {
            return ICE_TYPE_NOT_ICE;
        }

        if (isApplySidMatchToIceOnly(p_jobId))
        {
        	return ICE_TYPE_NOT_ICE;
        }

        // Check hash values.
        if (isKeyMatchICE(index, p_sourceTuvs, p_targetTuvs, p_matchTypes, p_subId, p_jobId))
        {
        	return ICE_TYPE_HASH_MATCHING;
        }

        // Don't apply "bracketed" ICE promotion.
		if (isBracketIceMatchesDisabled(p_jobId))
		{
			return ICE_TYPE_NOT_ICE;
		}

		// Check current tuv is exact match.
        if (!isExactMatchLocalized(index, p_sourceTuvs, p_targetTuvs, p_matchTypes, p_subId,
                p_jobId))
        {
            return ICE_TYPE_NOT_ICE;
        }

		// Check if it is bracketed ICE:
		// Check previous tuv is exact match.
        int previousIndex;
        for (previousIndex = index - 1; previousIndex >= 0; previousIndex--)
        {
            String tuType = getTuType(p_sourceTuvs.get(previousIndex), p_jobId);
            if (!p_excludedItemTypes.contains(tuType))
            {
                break;
            }
        }

        // No previous tuv.
        if (previousIndex < 0)
        {
            return ICE_TYPE_NOT_ICE;
        }

        // Previous tuv is not exact match.
        if (!isExactMatch(previousIndex, p_sourceTuvs, p_matchTypes))
        {
            return ICE_TYPE_NOT_ICE;
        }

        // Check next tuv is exact match.
        int nextIndex;
        for (nextIndex = index + 1; nextIndex < p_sourceTuvs.size(); nextIndex++)
        {
            String tuType = getTuType(p_sourceTuvs.get(previousIndex), p_jobId);
            if (!p_excludedItemTypes.contains(tuType))
            {
                break;
            }
        }

        // No next.
        if (nextIndex >= p_sourceTuvs.size())
        {
            return ICE_TYPE_NOT_ICE;
        }

        // Next tuv is not exact match.
        if (!isExactMatch(nextIndex, p_sourceTuvs, p_matchTypes))
        {
            return ICE_TYPE_NOT_ICE;
        }

        return ICE_TYPE_BRACKETED_MATCHING;
    }

    private static String getTuType(Object tuv, long p_jobId)
    {
        if (tuv instanceof Tuv)
        {
            Tuv sourceTuv = (Tuv) tuv;
            return sourceTuv.getTu(p_jobId).getTuType();
        }
        else
        {
            SegmentTmTuv sourceTuv = (SegmentTmTuv) tuv;
            return sourceTuv.getTu().getType();
        }
    }

    private static boolean isPassoloIncontextMatch(Object o,
            MatchTypeStatistics p_matchTypes, long p_jobId)
    {
        boolean result = false;
        if (isExactMatch(o, p_matchTypes))
        {
            if (o instanceof Tuv)
            {
                Tuv sourceTuv = (Tuv) o;
                Tu tu = sourceTuv.getTu(p_jobId);

                if (tu instanceof TuImpl)
                {
                    String state = ((TuImpl) tu).getPassoloState();
                    result = "Translated and reviewed".equals(state);
                }
            }
            else if (o instanceof SegmentTmTuv)
            {
                SegmentTmTuv sourceTuv = (SegmentTmTuv) o;
                TuvImpl tuv = null;
                try
                {
                    tuv = SegmentTuvUtil.getTuvById(sourceTuv.getId(), p_jobId);
                }
                catch (Exception e)
                {
                    logger.error(e.getMessage(), e);
                }

                TuImpl tu = (TuImpl) tuv.getTu(p_jobId);
                String state = ((TuImpl) tu).getPassoloState();
                result = "Translated and reviewed".equals(state);
            }
        }

        return result;
    }

    public static boolean isSidContextMatch(Object o,
            MatchTypeStatistics p_matchTypes)
    {
        boolean result = false;
        if (isExactMatch(o, p_matchTypes))
        {
            String sid = null;
            String matchedSid = null;

            if (o instanceof Tuv)
            {
                Tuv sourceTuv = (Tuv) o;
                sid = sourceTuv.getSid();
                matchedSid = p_matchTypes
                        .getSid(sourceTuv.getId(), DUMMY_SUBID);
            }
            else if (o instanceof SegmentTmTuv)
            {
                SegmentTmTuv sourceTuv = (SegmentTmTuv) o;
                sid = sourceTuv.getSid();
                matchedSid = p_matchTypes
                        .getSid(sourceTuv.getId(), DUMMY_SUBID);
            }

            if (sid != null && sid.equals(matchedSid))
            {
                result = true;
            }
        }

        return result;
    }

    @SuppressWarnings("rawtypes")
	public static boolean isSIDContextMatch(int index, List p_sourceTuvs,
            MatchTypeStatistics p_matchTypes)
    {
        return isSidContextMatch(p_sourceTuvs.get(index), p_matchTypes);
    }

    /**
     * Check if they are Exact match but have different sid
     * 
     * @param o
     * @param p_matchTypes
     * @return
     */
    public static boolean isSidExistsAndNotEqual(Object o,
            MatchTypeStatistics p_matchTypes)
    {
        boolean result = false;
        if (isExactMatch(o, p_matchTypes))
        {
            String sid = null;
            String matchedSid = null;

            if (o instanceof Tuv)
            {
                Tuv sourceTuv = (Tuv) o;
                sid = sourceTuv.getSid();
                matchedSid = p_matchTypes
                        .getSid(sourceTuv.getId(), DUMMY_SUBID);
            }
            else if (o instanceof SegmentTmTuv)
            {
                SegmentTmTuv sourceTuv = (SegmentTmTuv) o;
                sid = sourceTuv.getSid();
                matchedSid = p_matchTypes
                        .getSid(sourceTuv.getId(), DUMMY_SUBID);
            }

            if (sid != null && matchedSid != null && !sid.equals(matchedSid)
                    && !"".equals(sid) && !"".equals(matchedSid))
            {
                result = true;
            }
        }

        return result;
    }

    public static boolean isExactMatch(Object o,
            MatchTypeStatistics p_matchTypes)
    {
        int state = 0;

        if (o instanceof Tuv)
        {
            Tuv sourceTuv = (Tuv) o;
            state = p_matchTypes.getLingManagerMatchType(sourceTuv.getId(),
                    DUMMY_SUBID);
        }
        else if (o instanceof SegmentTmTuv)
        {
            SegmentTmTuv sourceTuv = (SegmentTmTuv) o;
            state = p_matchTypes.getLingManagerMatchType(sourceTuv.getId(),
                    DUMMY_SUBID);
        }

        return state == LeverageMatchLingManager.EXACT;
    }

    @SuppressWarnings("rawtypes")
	public static boolean isExactMatch(int index, List p_sourceTuvs,
            MatchTypeStatistics p_matchTypes)
    {
        Object o = p_sourceTuvs.get(index);
        long id;
        if (o instanceof Tuv)
        {
            Tuv sourceTuv = (Tuv) o;
            id = sourceTuv.getId();
        }
        else
        {
            SegmentTmTuv sourceTuv = (SegmentTmTuv) o;
            id = sourceTuv.getId();
        }

        int state = p_matchTypes.getLingManagerMatchType(id, DUMMY_SUBID);
        int matchType = p_matchTypes.getStatisticsMatchType(id, DUMMY_SUBID);

        return state == LeverageMatchLingManager.EXACT && matchType != 6;
    }

    /**
     * Exact match localized means the segment is same as matched segment. It
     * will be show not with green text format. And only this kind exact match
     * can be in-context match.
     * 
     * @param index
     * @param p_sourceTuvs
     * @param p_targetTuvs
     * @param p_matchTypes
     * @return
     */
    @SuppressWarnings("rawtypes")
	public static boolean isExactMatchLocalized(int index, List p_sourceTuvs,
            List p_targetTuvs, MatchTypeStatistics p_matchTypes,
            String p_subId, long p_jobId)
    {
        Object o = p_sourceTuvs.get(index);
        long id;
        boolean isLocalized = false;
        if (o instanceof Tuv)
        {
            Tuv sourceTuv = (Tuv) o;
            id = sourceTuv.getId();
            Tuv targetTuv = (Tuv) p_targetTuvs.get(index);
            isLocalized = EditorHelper.isRealExactMatchLocalied(sourceTuv, targetTuv, p_matchTypes,
                    p_subId, p_jobId);
        }
        else
        {
            SegmentTmTuv sourceTuv = (SegmentTmTuv) o;
            id = sourceTuv.getId();
            isLocalized = p_matchTypes.isExactMatchLocalized(id, p_subId, p_jobId);
        }

        int state = p_matchTypes.getLingManagerMatchType(id, p_subId);
        int matchType = p_matchTypes.getStatisticsMatchType(id, p_subId);

        return state == LeverageMatchLingManager.EXACT && isLocalized
                && matchType != MatchTypeStatistics.CONTEXT_EXACT
                && matchType != MatchTypeStatistics.SEGMENT_MT_EXACT
                && matchType != MatchTypeStatistics.IN_PROGRESS_TM_EXACT;
    }

    /**
     * For PO segment, if it has a valid(not empty,different from source,
     * language matched) target from source file, take this TUV as ICE directly.
     */
    @SuppressWarnings("rawtypes")
	private static boolean isPoXlfICE(int index, List p_sourceTuvs,
            MatchTypeStatistics p_matchTypes, long p_jobId)
    {
        if (p_sourceTuvs == null || p_sourceTuvs.size() == 0 || index < 0
                || index >= p_sourceTuvs.size())
        {
            return false;
        }

        Object o = p_sourceTuvs.get(index);
        long sourceTuvId = -1;
        String dataType = null;
        if (o instanceof Tuv)
        {
            Tuv sourceTuv = (Tuv) o;
            sourceTuvId = sourceTuv.getId();
            dataType = sourceTuv.getTu(p_jobId).getDataType();
        }
        else
        {
            SegmentTmTuv sourceTuv = (SegmentTmTuv) o;
            sourceTuvId = sourceTuv.getId();
            dataType = sourceTuv.getTu().getFormat();
        }
        // PO segments need not segmentation, no sub segments.
        int statisMatchType = p_matchTypes.getStatisticsMatchType(sourceTuvId,
                OnlineEditorManagerLocal.DUMMY_SUBID);

        if ("po".equalsIgnoreCase(dataType)
                && statisMatchType == MatchTypeStatistics.SEGMENT_PO_EXACT)
        {
            return true;
        }

        if (("xlf".equalsIgnoreCase(dataType) || "xliff"
                .equalsIgnoreCase(dataType))
                && statisMatchType == MatchTypeStatistics.SEGMENT_XLIFF_EXACT)
        {
            return true;
        }

        return false;
    }

    @SuppressWarnings("rawtypes")
	private static boolean isKeyMatchICE(int index, List p_sourceTuvs,
			List p_targetTuvs, MatchTypeStatistics p_matchTypes,
			String p_subId, long p_jobId)
    {
		LeverageMatch lm = getLeverageMatchObject(index, p_sourceTuvs,
				p_matchTypes, p_subId);
		if (lm == null)
			return false;

        long preHash = -1;
        long nextHash = -1;
        Object o = p_sourceTuvs.get(index);
    	if (isExactMatch(o, p_matchTypes))
    	{
            if (o instanceof Tuv)
            {
            	Tuv sourceTuv = (Tuv) o;
                preHash = sourceTuv.getPreviousHash();
                nextHash = sourceTuv.getNextHash();
                if (lm.getMatchState().equals(MatchState.MULTIPLE_TRANSLATION))
                {
                    Tuv targetTuv = (Tuv) p_targetTuvs.get(index);
                    if (targetTuv.getState().equals(TuvState.NOT_LOCALIZED))
                    {
                    	return false;
                    }
                }
            }
            else
            {
                SegmentTmTuv sourceTuv = (SegmentTmTuv) o;
                preHash = sourceTuv.getPreviousHash();
                nextHash = sourceTuv.getNextHash();
            }

            if (preHash != -1 && nextHash != -1 && preHash != BaseTmTuv.FIRST_HASH
    				&& nextHash != BaseTmTuv.LAST_HASH
    				&& preHash == lm.getPreviousHash()
    				&& nextHash == lm.getNextHash())
    		{
    			return true;
    		}
    	}

        return false;
    }

    @SuppressWarnings("rawtypes")
	private static String getSubId(int index, List p_sourceTuvs)
    {
        String subId = DUMMY_SUBID;
        Object o = p_sourceTuvs.get(index);
        if (o instanceof Tuv)
        {
            subId = DUMMY_SUBID;
        }
        else
        {
            SegmentTmTuv sourceTuv = (SegmentTmTuv) o;
            subId = ((SegmentTmTu) sourceTuv.getTu()).getSubId();
        }

        return subId;
    }

    @SuppressWarnings("rawtypes")
	private static LeverageMatch getLeverageMatchObject(int index,
			List p_sourceTuvs, MatchTypeStatistics p_matchTypes,
			String p_subId)
    {
        if (p_sourceTuvs == null || p_sourceTuvs.size() == 0 || index < 0
                || index >= p_sourceTuvs.size())
        {
            return null;
        }

        long tuvId = -1;
        Object o = p_sourceTuvs.get(index);
        if (o instanceof Tuv)
        {
        	Tuv sourceTuv = (Tuv) o;
            tuvId = sourceTuv.getId();
        }
        else
        {
            SegmentTmTuv sourceTuv = (SegmentTmTuv) o;
            tuvId = sourceTuv.getId();
        }
        Types types = p_matchTypes.getTypes(tuvId, p_subId);
        if (types != null)
        {
        	return types.getLeverageMatch();
        }

        return null;
    }

    private static boolean isApplySidMatchToIceOnly(long jobId)
    {
		try
		{
			TranslationMemoryProfile tmp = BigTableUtil.getJobById(jobId)
					.getL10nProfile().getTranslationMemoryProfile();
			if (tmp.getIsContextMatchLeveraging()
					&& tmp.getIcePromotionRules() == TranslationMemoryProfile.ICE_PROMOTION_SID_ONLY)
			{
				return true;
			}
		}
		catch (Exception e)
		{
			logger.error(e);
		}

		return false;
    }

    private static boolean isBracketIceMatchesDisabled(long p_jobId)
    {
		try
		{
			Job job = ServerProxy.getJobHandler().getJobById(p_jobId);
			TranslationMemoryProfile tmp = job.getL10nProfile()
					.getTranslationMemoryProfile();
			int icePromotionRules = tmp.getIcePromotionRules();
			if (tmp.getIsContextMatchLeveraging()
					&& (icePromotionRules == TranslationMemoryProfile.ICE_PROMOTION_SID_ONLY 
					|| icePromotionRules == TranslationMemoryProfile.ICE_PROMOTION_SID_HASH_MATCHES))
	    	{
	    		return true;
	    	}
		}
		catch (Exception e)
		{
			logger.error(e);
		}

    	return false;
    }
}
