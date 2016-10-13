package com.globalsight.everest.edit.online;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.globalsight.everest.comment.Issue;
import com.globalsight.everest.edit.CommentHelper;
import com.globalsight.everest.edit.online.OnlineEditorManagerLocal.PageCache;
import com.globalsight.everest.integration.ling.tm2.MatchTypeStatistics;
import com.globalsight.everest.integration.ling.tm2.Types;
import com.globalsight.everest.tuv.Tu;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.tuv.TuvState;
import com.globalsight.everest.webapp.pagehandler.edit.online.EditorState;
import com.globalsight.ling.tm.LeverageMatchLingManager;
import com.globalsight.ling.tm2.leverage.LeverageUtil;
import com.globalsight.util.gxml.GxmlElement;
import com.globalsight.util.gxml.GxmlNames;

/**
 * This class is related to Segment Filter.
 * 
 * @author Joey Jiang
 *
 */
public class SegmentFilter
{
    public static final String KEY_SOURCE = "srcTuvs";
    public static final String KEY_TARGET = "trgTuvs";
    
    /**
     * Do filter for Source TUV and Target TUV following the segment filter.
     * 
     * @param p_pageCache
     *            - The Object contained page data.
     * @param p_sourceTuvs
     *            - All Source TUV List
     * @param p_targetTuvs
     *            - All Target TUV List
     * @param p_state
     *            - Editor State
     * @param p_tuvMatchTypes
     *            - TUV Match Types
     * @param p_comments
     *            - TUV Comment List
     * @param p_excludedItemTypes
     *            - The item type excluded by Editor.
     * @param p_trgPageId
     *            - Target Page ID
     * @param p_jobId
     */
    public static Map<String, List<Tuv>> operateForSegmentFilter(
            PageCache p_pageCache, List<Tuv> p_sourceTuvs,
            List<Tuv> p_targetTuvs, EditorState p_state,
            MatchTypeStatistics p_tuvMatchTypes, ArrayList<Issue> p_comments,
            Vector p_excludedItemTypes, long p_trgPageId, long p_jobId)
    {
        if (!isFilterSegment(p_state))
            return null;

        Map<String, List<Tuv>> result = new HashMap<String, List<Tuv>>();
        String filterType = p_state.getSegmentFilter();
        if (OnlineEditorConstants.SEGMENT_FILTER_ALL_EXCEPT_ICE_AND_100.equals(filterType))
        {
            List<Tuv> srcTuvs = new ArrayList<Tuv>();
            List<Tuv> trgTuvs = new ArrayList<Tuv>();
            List<Tuv> excludeSrcTuvs = new ArrayList<Tuv>();
            
            operateForSegmentFilter(OnlineEditorConstants.SEGMENT_FILTER_ICE,
                    result, p_sourceTuvs, p_targetTuvs, p_state,
                    p_tuvMatchTypes, p_comments, p_excludedItemTypes, -1,
                    p_jobId);
            excludeSrcTuvs = (List<Tuv>) result.get(KEY_SOURCE);
            
            operateForSegmentFilter(OnlineEditorConstants.SEGMENT_FILTER_100,
                    result, p_sourceTuvs, p_targetTuvs, p_state,
                    p_tuvMatchTypes, p_comments, p_excludedItemTypes, -1,
                    p_jobId);
            excludeSrcTuvs.addAll((List<Tuv>) result.get(KEY_SOURCE));
            
            for (int i = 0; i < p_sourceTuvs.size(); i++)
            {
                Tuv srcTuv = p_sourceTuvs.get(i);
                if (!excludeSrcTuvs.contains(srcTuv))
                {
                    Tuv trgTuv = p_targetTuvs.get(i);
                    srcTuvs.add(srcTuv);
                    trgTuvs.add(trgTuv);
                }
            }
            
            result.put(KEY_SOURCE, srcTuvs);
            result.put(KEY_TARGET, trgTuvs);
        }
        else if (OnlineEditorConstants.SEGMENT_FILTER_ALL_EXCEPT_ICE.equals(filterType))
        {
            List<Tuv> srcTuvs = new ArrayList<Tuv>();
            List<Tuv> trgTuvs = new ArrayList<Tuv>();
            List<Tuv> excludeSrcTuvs = new ArrayList<Tuv>();
            operateForSegmentFilter(OnlineEditorConstants.SEGMENT_FILTER_ICE,
                    result, p_sourceTuvs, p_targetTuvs, p_state,
                    p_tuvMatchTypes, p_comments, p_excludedItemTypes, -1,
                    p_jobId);
            excludeSrcTuvs = (List<Tuv>) result.get(KEY_SOURCE);
            
            for (int i = 0; i < p_sourceTuvs.size(); i++)
            {
                Tuv srcTuv = p_sourceTuvs.get(i);
                if (!excludeSrcTuvs.contains(srcTuv))
                {
                    Tuv trgTuv = p_targetTuvs.get(i);
                    srcTuvs.add(srcTuv);
                    trgTuvs.add(trgTuv);
                }
            }
            
            result.put(KEY_SOURCE, srcTuvs);
            result.put(KEY_TARGET, trgTuvs);
        }
        else
        {
            operateForSegmentFilter(filterType, result, p_sourceTuvs,
                    p_targetTuvs, p_state, p_tuvMatchTypes, p_comments,
                    p_excludedItemTypes, p_trgPageId, p_jobId);
        }
        
        return result;
    }
    
    /**
     * Do filter for Source TUV and Target TUV with the segment filter.
     * 
     * @param p_segmentFilter
     *            - Segment Filter Type
     * @param p_result
     *            - The Map contains the filted Source TUV and Target TUV
     * @param p_sourceTuvs
     *            - All Source TUV List
     * @param p_targetTuvs
     *            - All Target TUV List
     * @param p_state
     *            - Editor State
     * @param p_tuvMatchTypes
     *            - TUV Match Types
     * @param p_comments
     *            - TUV Comment List
     * @param p_excludedItemTypes
     *            - The item type excluded by Editor.
     * @param p_targetPageId
     *            - Target Page ID
     * @param p_jobId
     */
    private static void operateForSegmentFilter(String p_segmentFilter,
            Map<String, List<Tuv>> p_result, List<Tuv> p_sourceTuvs,
            List<Tuv> p_targetTuvs, EditorState p_state,
            MatchTypeStatistics p_tuvMatchTypes, ArrayList<Issue> p_comments,
            Vector p_excludedItemTypes, long p_targetPageId, long p_jobId)
    {
        List<Tuv> srcTuvs = new ArrayList<Tuv>();
        List<Tuv> trgTuvs = new ArrayList<Tuv>();
        
        List<String> commentKeys = null;
        if (p_segmentFilter.equals(OnlineEditorConstants.SEGMENT_FILTER_COMMENTED)
                && p_comments != null && p_comments.size() > 0)
        {
            commentKeys = new ArrayList<String>();
            for (Issue issue : p_comments)
            {
                commentKeys.add(issue.getLogicalKey());
            }
        }
        
        for (int i = 0; i < p_sourceTuvs.size(); i++)
        {
            Tuv srcTuv = p_sourceTuvs.get(i);
            Tuv trgTuv = p_targetTuvs.get(i);
            Tu tu = trgTuv.getTu(p_jobId);
            boolean isMatch = isMatchSegmentFilter(p_segmentFilter, srcTuv,
                    trgTuv, tu, p_tuvMatchTypes, commentKeys,
                    p_excludedItemTypes, i, p_sourceTuvs, p_targetTuvs,
                    p_targetPageId, p_jobId);
            if (isMatch)
            {
                srcTuvs.add(srcTuv);
                trgTuvs.add(trgTuv);
            }
        }
        
        p_result.put(KEY_SOURCE, srcTuvs);
        p_result.put(KEY_TARGET, trgTuvs);
    }
    
    // Check whether the TUV match the Segment Filter.
    private static boolean isMatchSegmentFilter(String p_segFilter,
            Tuv p_srcTuv, Tuv p_trgTuv, Tu p_tu,
            MatchTypeStatistics p_tuvMatchTypes, List<String> p_commentKeys,
            Vector p_excludedItemTypes, int p_index, List<Tuv> p_sourceTuvs,
            List<Tuv> p_targetTuvs, long p_targetPageId, long p_jobId)
    {
        if (p_segFilter.equals(OnlineEditorConstants.SEGMENT_FILTER_ICE))
        {
            return LeverageUtil.isIncontextMatch(p_index, p_sourceTuvs,
                    p_targetTuvs, p_tuvMatchTypes, p_excludedItemTypes,
                    p_jobId);
        }
        else if (p_segFilter.equals(OnlineEditorConstants.SEGMENT_FILTER_100))
        {
            boolean isExactMatch = false;
            Collection<Types> types = 
                    p_tuvMatchTypes.getLingManagerMatchType(p_srcTuv).values();
            for (Types type : types)
            {
                if (type != null
                        && type.getLingManagerMatchType() == LeverageMatchLingManager.EXACT)
                {
                    isExactMatch = true;
                    break;
                }
            }
            
            return isExactMatch
                    && !(LeverageUtil.isIncontextMatch(p_index, p_sourceTuvs,
                            p_targetTuvs, p_tuvMatchTypes, p_excludedItemTypes,
                            p_jobId));
        }
        else if (p_segFilter.equals(OnlineEditorConstants.SEGMENT_FILTER_REPEATED))
        {
            return p_trgTuv.isRepeated();
        }
        else if (p_segFilter.equals(OnlineEditorConstants.SEGMENT_FILTER_REPETITIONS))
        {
            return p_trgTuv.getRepetitionOfId() > 0;
        }
        else if (p_segFilter.equals(OnlineEditorConstants.SEGMENT_FILTER_MODIFIED))
        {
            String modifiedUser = p_trgTuv.getLastModifiedUser();
            return modifiedUser != null && !modifiedUser.endsWith("_MT");
        }
        else if (p_segFilter.equals(OnlineEditorConstants.SEGMENT_FILTER_NO_TRANSLATED))
        {
            return !isTreatAsTranslated(p_srcTuv, p_trgTuv, p_tuvMatchTypes);
        }
        else if (p_segFilter.equals(OnlineEditorConstants.SEGMENT_FILTER_APPROVED))
        {
            return TuvState.APPROVED.equals(p_trgTuv.getState());
        }
        else if (p_segFilter.equals(OnlineEditorConstants.SEGMENT_FILTER_COMMENTED))
        {
            return haveCommentForSegment(p_commentKeys, p_trgTuv, p_targetPageId);
        }
        else if (p_segFilter.equals(OnlineEditorConstants.SEGMENT_FILTER_MACHINETRANSLATION))
        {
            return p_tuvMatchTypes.isMachineTranslation(p_srcTuv);
        }

        return false;
    }

    /**
     * Judge if a target TUV can be treated as "translated".
     * 
     * @param p_srcTuv
     * @param p_trgTuv
     * @param p_tuvMatchTypes
     * @return boolean
     */
    public static boolean isTreatAsTranslated(Tuv p_srcTuv, Tuv p_trgTuv,
            MatchTypeStatistics p_tuvMatchTypes)
    {
        boolean isTranslated = false;

        TuvState trgState = p_trgTuv.getState();
        if (TuvState.DO_NOT_TRANSLATE.equals(trgState)
                || TuvState.LOCALIZED.equals(trgState)
                || TuvState.APPROVED.equals(trgState)
                || TuvState.EXACT_MATCH_LOCALIZED.equals(trgState))
        {
            isTranslated = true;
        }
        else
        {
            int state = p_tuvMatchTypes.getLingManagerMatchType(
                    p_srcTuv.getId(), OnlineEditorManagerLocal.DUMMY_SUBID);
            if (state == LeverageMatchLingManager.EXACT
                    || state == LeverageMatchLingManager.UNVERIFIED)
            {
                isTranslated = true;
            }
        }

        return isTranslated;
    }

    public static boolean haveCommentForSegment(List<String> p_commentKeys,
            Tuv p_tuv, long p_targetPageId)
    {
        if(p_commentKeys == null || p_commentKeys.size() == 0)
            return false;
        
        long tuId = p_tuv.getTuId();
        long tuvId = p_tuv.getId();
        String subId = "0";
        String key = CommentHelper.makeLogicalKey(p_targetPageId, tuId, tuvId, subId);
        if(p_commentKeys.contains(key))
        {
            return true;
        }
        
        List<GxmlElement> subs = p_tuv.getSubflowsAsGxmlElements();
        if(subs!= null && subs.size() > 0)
        {
            for(GxmlElement sub : subs)
            {
                subId = sub.getAttribute(GxmlNames.SUB_ID);
                key = CommentHelper.makeLogicalKey(p_targetPageId, tuId, tuvId, subId);
                if(p_commentKeys.contains(key))
                {
                    return true;
                }
            }
        }

        return false;
    }
    
    // Checks whether Segment Filter value is default value.
    public static boolean isFilterSegment(EditorState p_state)
    {
        String segFilter = p_state.getSegmentFilter();
        if (segFilter == null || segFilter.trim().length() == 0
                || segFilter.equals(OnlineEditorConstants.SEGMENT_FILTER_ALL))
        {
            return false;
        }
        
        return OnlineEditorConstants.SEGMENT_FILTERS.contains(segFilter);
    }

}
