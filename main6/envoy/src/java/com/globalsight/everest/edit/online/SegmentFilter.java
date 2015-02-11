package com.globalsight.everest.edit.online;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.globalsight.everest.comment.Issue;
import com.globalsight.everest.edit.CommentHelper;
import com.globalsight.everest.edit.online.OnlineEditorManagerLocal.PageCache;
import com.globalsight.everest.integration.ling.tm2.MatchTypeStatistics;
import com.globalsight.everest.tuv.Tu;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.webapp.pagehandler.edit.online.EditorState;
import com.globalsight.ling.tm.LeverageMatchLingManager;
import com.globalsight.ling.tm2.leverage.LeverageUtil;

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
    
    public static Map<String, List<Tuv>> operateForSegmentFilter(PageCache p_pageCache,
            List<Tuv> p_sourceTuvs, List<Tuv> p_targetTuvs,
            EditorState p_state, MatchTypeStatistics p_tuvMatchTypes,
            ArrayList<Issue> p_comments, long p_companyId,
            Vector p_excludedItemTypes, long p_trgPageId)
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
                    p_pageCache.getTuICE(), result, p_sourceTuvs, p_targetTuvs,
                    p_state, p_tuvMatchTypes, p_comments, p_companyId,
                    p_excludedItemTypes, -1);
            excludeSrcTuvs = (List<Tuv>) result.get(KEY_SOURCE);
            
            operateForSegmentFilter(OnlineEditorConstants.SEGMENT_FILTER_100,
                    p_pageCache.getTu100(), result, p_sourceTuvs, p_targetTuvs,
                    p_state, p_tuvMatchTypes, p_comments, p_companyId,
                    p_excludedItemTypes, -1);
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
                    p_pageCache.getTuICE(), result, p_sourceTuvs, p_targetTuvs,
                    p_state, p_tuvMatchTypes, p_comments, p_companyId,
                    p_excludedItemTypes, -1);
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
        else if (OnlineEditorConstants.SEGMENT_FILTER_ICE.equals(filterType))
        {
            operateForSegmentFilter(OnlineEditorConstants.SEGMENT_FILTER_ICE,
                    p_pageCache.getTuICE(), result, p_sourceTuvs, p_targetTuvs,
                    p_state, p_tuvMatchTypes, p_comments, p_companyId,
                    p_excludedItemTypes, -1);
        }
        else if (OnlineEditorConstants.SEGMENT_FILTER_100.equals(filterType))
        {
            operateForSegmentFilter(OnlineEditorConstants.SEGMENT_FILTER_100,
                    p_pageCache.getTu100(), result, p_sourceTuvs, p_targetTuvs,
                    p_state, p_tuvMatchTypes, p_comments, p_companyId,
                    p_excludedItemTypes, -1);
        }
        else if (OnlineEditorConstants.SEGMENT_FILTER_REPEATED.equals(filterType))
        {
            operateForSegmentFilter(OnlineEditorConstants.SEGMENT_FILTER_REPEATED,
                    p_pageCache.getTuRepeated(), result, p_sourceTuvs, p_targetTuvs,
                    p_state, p_tuvMatchTypes, p_comments, p_companyId,
                    p_excludedItemTypes, -1);
        }
        else if (OnlineEditorConstants.SEGMENT_FILTER_REPETITIONS.equals(filterType))
        {
            operateForSegmentFilter(OnlineEditorConstants.SEGMENT_FILTER_REPETITIONS,
                    p_pageCache.getTuRepeations(), result, p_sourceTuvs, p_targetTuvs,
                    p_state, p_tuvMatchTypes, p_comments, p_companyId,
                    p_excludedItemTypes, -1);
        }
        else if (OnlineEditorConstants.SEGMENT_FILTER_MODIFIED.equals(filterType))
        {
            operateForSegmentFilter(OnlineEditorConstants.SEGMENT_FILTER_MODIFIED,
                    p_pageCache.getTuModified(), result, p_sourceTuvs, p_targetTuvs,
                    p_state, p_tuvMatchTypes, p_comments, p_companyId,
                    p_excludedItemTypes, -1);
        }
        else if (OnlineEditorConstants.SEGMENT_FILTER_COMMENTED.equals(filterType))
        {
            operateForSegmentFilter(OnlineEditorConstants.SEGMENT_FILTER_COMMENTED,
                    p_pageCache.getTuCommonted(), result, p_sourceTuvs, p_targetTuvs,
                    p_state, p_tuvMatchTypes, p_comments, p_companyId,
                    p_excludedItemTypes, p_trgPageId);
        }
        
        return result;
    }
    
    private static void operateForSegmentFilter(String p_segmentFilter, List<Tu> p_tus,
            Map<String, List<Tuv>> p_result, List<Tuv> p_sourceTuvs,
            List<Tuv> p_targetTuvs, EditorState p_state,
            MatchTypeStatistics p_tuvMatchTypes, ArrayList<Issue> p_comments,
            long p_companyId, Vector p_excludedItemTypes, long p_targetPageId)
    {
        if (p_tus != null)
            return;
        
        List<Tuv> srcTuvs = new ArrayList<Tuv>();
        List<Tuv> trgTuvs = new ArrayList<Tuv>();
        List<Tu> tus = new ArrayList<Tu>();
        String companyID = String.valueOf(p_companyId);
        for (int i = 0; i < p_sourceTuvs.size(); i++)
        {
            Tuv srcTuv = p_sourceTuvs.get(i);
            Tuv trgTuv = p_targetTuvs.get(i);
            Tu tu = trgTuv.getTu(companyID);
            boolean isMatch = isMatchSegmentFilter(p_segmentFilter, srcTuv, trgTuv, tu,
                    p_tuvMatchTypes, p_comments,
                    companyID, p_excludedItemTypes, i,
                    p_sourceTuvs, p_targetTuvs, p_targetPageId);
            if (isMatch)
            {
                srcTuvs.add(srcTuv);
                trgTuvs.add(trgTuv);
                tus.add(tu);
            }
        }
        
        p_result.put(KEY_SOURCE, srcTuvs);
        p_result.put(KEY_TARGET, trgTuvs);
        if (srcTuvs.size() > 0)
        {
            p_tus = new ArrayList<Tu>();
            p_tus.addAll(tus);
        }
    }
    
    // Check whether the TUV match the Segment Filter.
    private static boolean isMatchSegmentFilter(String p_segFilter, Tuv p_srcTuv, Tuv p_trgTuv, Tu p_tu,
            MatchTypeStatistics p_tuvMatchTypes, ArrayList<Issue> p_comments,
            String p_companyId, Vector p_excludedItemTypes, int p_index,
            List<Tuv> p_sourceTuvs, List<Tuv> p_targetTuvs, long p_targetPageId)
    {
        if (p_segFilter.equals(OnlineEditorConstants.SEGMENT_FILTER_ICE))
        {
            return LeverageUtil.isIncontextMatch(p_index, p_sourceTuvs,
                    p_targetTuvs, p_tuvMatchTypes, p_excludedItemTypes,
                    p_companyId);
        }
        else if (p_segFilter.equals(OnlineEditorConstants.SEGMENT_FILTER_100))
        {
            int lmType = p_tuvMatchTypes.getLingManagerMatchType(
                    p_srcTuv.getId(), "0");
            boolean incontextMatch = LeverageUtil.isIncontextMatch(p_index,
                    p_sourceTuvs, p_targetTuvs, p_tuvMatchTypes,
                    p_excludedItemTypes, p_companyId);
            return (LeverageMatchLingManager.EXACT == lmType) && !incontextMatch;
        }
        else if (p_segFilter.equals(OnlineEditorConstants.SEGMENT_FILTER_REPEATED))
        {
            return p_tu.isRepeated()
                    && (LeverageMatchLingManager.EXACT != p_tuvMatchTypes
                            .getLingManagerMatchType(p_srcTuv.getId(), "0"));
        }
        else if (p_segFilter.equals(OnlineEditorConstants.SEGMENT_FILTER_REPETITIONS))
        {
            return p_tu.getRepetitionOfId() > 0
                    && (LeverageMatchLingManager.EXACT != p_tuvMatchTypes
                            .getLingManagerMatchType(p_srcTuv.getId(), "0"));
        }
        else if (p_segFilter.equals(OnlineEditorConstants.SEGMENT_FILTER_MODIFIED))
        {
            String modifiedUser = p_trgTuv.getLastModifiedUser();
            return modifiedUser != null && !modifiedUser.endsWith("_MT");
        }
        else if (p_segFilter.equals(OnlineEditorConstants.SEGMENT_FILTER_COMMENTED))
        {
            return haveCommentForSegment(p_targetPageId, p_tu.getId(),
                    p_trgTuv.getId(), "0", p_comments);
        }

        return false;
    }
    
    private static boolean haveCommentForSegment(long p_targetPageId, long p_tuId, long p_tuvId,
            String p_subId, List p_comments)
    {
        String key = CommentHelper.makeLogicalKey(p_targetPageId, p_tuId, p_tuvId, p_subId);

        for (int i = 0, max = p_comments.size(); i < max; i++)
        {
            Issue issue = (Issue) p_comments.get(i);

            if (issue.getLogicalKey().equals(key))
            {
                return true;
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
