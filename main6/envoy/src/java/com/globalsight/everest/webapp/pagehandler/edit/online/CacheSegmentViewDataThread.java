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
package com.globalsight.everest.webapp.pagehandler.edit.online;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;

import com.globalsight.everest.company.MultiCompanySupportedThread;
import com.globalsight.everest.edit.online.SegmentView;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.persistence.hibernate.HibernateUtil;

public class CacheSegmentViewDataThread extends MultiCompanySupportedThread
{
    static private final Logger CATEGORY = Logger
            .getLogger(CacheSegmentViewDataThread.class);

    /**
     * Cache segmentView number in session at most.This avoids memory overflow.
     */
    public static final int CACHE_SEGMENTVIEW_MAX_NUM = 100;
    
    private SessionManager sessionMgr = null;
    private CopyOnWriteArrayList tuTuvSubIDList = null;
    private ConcurrentHashMap segmentViewMap = null;
    private EditorState editorState = null;
    private long targetPageId = -1;
    private long sourceLocaleId = -1;
    private long targetLocaleId = -1;
    private boolean releverage = false;
    
    public CacheSegmentViewDataThread(SessionManager p_sessionMgr,
            EditorState p_state, long p_targetPageId, long p_sourceLocaleId,
            long p_targetLocaleId, boolean b_releverage)
    {
        sessionMgr = p_sessionMgr;
        
        tuTuvSubIDList = (CopyOnWriteArrayList) sessionMgr
                .getAttribute(WebAppConstants.PAGE_TU_TUV_SUBID_SET);
        if (tuTuvSubIDList == null)
        {
            tuTuvSubIDList = new CopyOnWriteArrayList();
        }
        
        // it is no use to initiate "segmentViewMap" here.
//        segmentViewMap = (ConcurrentHashMap) sessionMgr
//                .getAttribute(WebAppConstants.SEGMENT_VIEW_MAP);
//        if (segmentViewMap == null)
//        {
//            segmentViewMap = new ConcurrentHashMap();
//        }

        editorState = p_state;
        targetPageId = p_targetPageId;
        sourceLocaleId = p_sourceLocaleId;
        targetLocaleId = p_targetLocaleId;
        releverage = b_releverage;
    }
    
    public void run()
    {
        super.run();
        
        try
        {
            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.info("****** BEGIN : cache segment data in separate thread ******");
            }
            if (editorState == null)
            {
                return;
            }

            //
            long curTuID = editorState.getTuId();
            long curTuvID = editorState.getTuvId();
            long curSubID = editorState.getSubId();
            // Make sure "tuID" and "TuvID" are valid.
            // Before translator "accept" task,open pop-up editor,"tuID" and
            // "tuvID" are both "0".
            if (curTuID > 0 && curTuvID > 0)
            {
                String curKey = curTuID + "_" + curTuvID + "_" + curSubID;
                String nextKey = null;
                String preKey = null;

                SegmentKey curSegKey = null;
                SegmentKey preSegKey = null;
                SegmentKey nextSegKey = null;

                curSegKey = findSegmentKey(tuTuvSubIDList, curKey);
                if (curSegKey == null)
                {
                    curSegKey = new SegmentKey(curKey);
                }
                else
                {
                    nextKey = curSegKey.getNextKey();
                    preKey = curSegKey.getPreviousKey();
                }

                // If "nextKey" is null, get it.
                if (nextKey == null)
                {
                    resetTuTuvSubIDs(editorState, curKey);
                    EditorHelper.nextSegment(editorState);
                    nextKey = generateTuTuvSubIDKey(editorState);
                    curSegKey.setNextKey(nextKey);
                    // Update session in time.
                    updateSessionWithSegmentKey(sessionMgr,
                            tuTuvSubIDList, curSegKey);
                }
                // At this time,"nextKey" is not null.
                if (nextKey != null)
                {
                    nextSegKey = findSegmentKey(tuTuvSubIDList, nextKey);
                    if (nextSegKey != null)
                    {
                        nextSegKey.setPreviousKey(curKey);
                    }
                    else
                    {
                        nextSegKey = new SegmentKey(curKey, nextKey, null);
                    }

                    // Update session in time.
                    updateSessionWithSegmentKey(sessionMgr,
                            tuTuvSubIDList, nextSegKey);
                }

                if (preKey == null)
                {
                    resetTuTuvSubIDs(editorState, curKey);
                    EditorHelper.previousSegment(editorState);
                    preKey = generateTuTuvSubIDKey(editorState);
                    curSegKey.setPreviousKey(preKey);

                    // Update session in time.
                    updateSessionWithSegmentKey(sessionMgr,
                            tuTuvSubIDList, curSegKey);
                }
                // At this time,"preKey" is not null.
                if (preKey != null)
                {
                    preSegKey = findSegmentKey(tuTuvSubIDList, preKey);
                    if (preSegKey != null)
                    {
                        preSegKey.setNextKey(curKey);
                    }
                    else
                    {
                        preSegKey = new SegmentKey(null, preKey, curKey);
                    }

                    // Update session in time.
                    updateSessionWithSegmentKey(sessionMgr,
                            tuTuvSubIDList, preSegKey);
                }

                // Get segmentView one by one to save them into cache if
                // not exist in cache.
                Iterator keyIter = tuTuvSubIDList.iterator();
                while (keyIter.hasNext())
                {
                    // Catch exception one by one to ensure successful
                    // segmentView can be put in cache.
                    try
                    {
                        // Retrieve the map from session to avoid repeated work
                        // as other thread may have updated this map in session.
                        segmentViewMap = (ConcurrentHashMap) sessionMgr
                                .getAttribute(WebAppConstants.SEGMENT_VIEW_MAP);
                        if (segmentViewMap == null)
                        {
                            segmentViewMap = new ConcurrentHashMap();
                        }

                        SegmentKey segKey = (SegmentKey) keyIter.next();
                        String key = segKey.getCurrentKey();
                        if (segmentViewMap.get(key) == null)
                        {
                            String[] subKeys = key.split("_");
                            long tuId = Long.parseLong(subKeys[0]);
                            long tuvId = Long.parseLong(subKeys[1]);
                            long subId = Long.parseLong(subKeys[2]);
                            editorState.setTuId(tuId);
                            editorState.setTuvId(tuvId);
                            editorState.setSubId(subId);

                            SegmentView segmentView = EditorHelper
                                    .getSegmentView(editorState, tuId, tuvId,
                                            subId, targetPageId,
                                            sourceLocaleId, targetLocaleId,
                                            releverage);
                            segmentViewMap.put(key, segmentView);
                            sessionMgr.setAttribute(
                                    WebAppConstants.SEGMENT_VIEW_MAP,
                                    segmentViewMap);
                            
                            if (CATEGORY.isDebugEnabled())
                            {
                                CATEGORY.info("SegmentView data for " + key
                                        + "(tuID_tuvID_subID) is cached.");
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        CATEGORY.error(
                                "Error occurs when cache SegmentView data.", e);
                        // Any exception occurs, break the loop.
                        break;
                    }
                }

                /**
                 * Remove some "remote" segmentViews whose key is far from
                 * current key from cache to avoid too much data in session.
                 * When user transfer from one page to another page, the
                 * segmentView data won't be removed from session.But once the
                 * total number is more than double
                 * "CACHE_SEGMENTVIEW_MOST_NUM",below action will remove all the
                 * useless data and "remote" data,including the data that are
                 * not in the "previous-next" chain and data from another page.
                 */
                if (CATEGORY.isDebugEnabled())
                {
                    CATEGORY.info("SegmentViewMap size before clean: "
                            + segmentViewMap.size());                
                }
                if (segmentViewMap != null
                        && segmentViewMap.size() > 2*CACHE_SEGMENTVIEW_MAX_NUM )
                {
                    ConcurrentHashMap newSegmentViewMap = new ConcurrentHashMap();
                    SegmentView sv = (SegmentView) segmentViewMap.get(curKey);
                    if (sv != null)
                    {
                        newSegmentViewMap.put(curKey, sv);
                    }

                    // Find 100 next SegmentKey.
                    int count = 0;
                    int sizeCount = 0;
                    SegmentKey segKey = curSegKey;
                    while (count < CACHE_SEGMENTVIEW_MAX_NUM
                            && sizeCount < (segmentViewMap.size() / 2))
                    {
                        if (segKey != null)
                        {
                            segKey = findSegmentKey(tuTuvSubIDList, segKey
                                    .getNextKey());
                            if (segKey != null)
                            {
                                String strKey = segKey.getCurrentKey();
                                sv = (SegmentView) segmentViewMap.get(strKey);
                                if (sv != null)
                                {
                                    newSegmentViewMap.put(strKey, sv);
                                    count++;
                                }
                            }
                        }

                        // this will increase anyway to avoid dead loop.
                        sizeCount++;
                    }

                    // Find 100 previous SegmentKey
                    count = 0;
                    sizeCount = 0;
                    segKey = curSegKey;
                    while (count < CACHE_SEGMENTVIEW_MAX_NUM
                            && sizeCount < (segmentViewMap.size() / 2))
                    {
                        if (segKey != null)
                        {
                            segKey = findSegmentKey(tuTuvSubIDList, segKey
                                    .getPreviousKey());
                            if (segKey != null)
                            {
                                String strKey = segKey.getCurrentKey();
                                sv = (SegmentView) segmentViewMap.get(strKey);
                                if (sv != null)
                                {
                                    newSegmentViewMap.put(strKey, sv);
                                    count++;
                                }
                            }
                        }

                        // this will increase anyway to avoid dead loop.
                        sizeCount++;
                    }
                    // Use the new map to replace the old one.
                    sessionMgr.setAttribute(
                            WebAppConstants.SEGMENT_VIEW_MAP,
                            newSegmentViewMap);

                    if (CATEGORY.isDebugEnabled())
                    {
                        CATEGORY.info("SegmentViewMap size after clean: "
                                + newSegmentViewMap.size());
                    }
                }                
            }
        }
        catch (Throwable e)
        {
            CATEGORY.error("Error while cache segmentView data.", e);
        }
        finally
        {
            HibernateUtil.closeSession();
            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.info("****** END : cache segment data in separate thread ******");
            }
        }
    }
    
    public CopyOnWriteArrayList getTuTuvSubIDList()
    {
        return tuTuvSubIDList;
    }

    public void setTuTuvSubIDList(CopyOnWriteArrayList tuTuvSubIDList)
    {
        this.tuTuvSubIDList = tuTuvSubIDList;
    }

    public ConcurrentHashMap getSegmentViewMap()
    {
        return segmentViewMap;
    }

    public void setSegmentViewMap(ConcurrentHashMap segmentViewMap)
    {
        this.segmentViewMap = segmentViewMap;
    }


    public EditorState getEditorState()
    {
        return editorState;
    }

    public void setEditorState(EditorState state)
    {
        this.editorState = state;
    }

    public long getTargetPageId()
    {
        return targetPageId;
    }

    public void setTargetPageId(long targetPageId)
    {
        this.targetPageId = targetPageId;
    }

    public long getSourceLocaleId()
    {
        return sourceLocaleId;
    }

    public void setSourceLocaleId(long sourceLocaleId)
    {
        this.sourceLocaleId = sourceLocaleId;
    }

    public long getTargetLocaleId()
    {
        return targetLocaleId;
    }

    public void setTargetLocaleId(long targetLocaleId)
    {
        this.targetLocaleId = targetLocaleId;
    }

    public boolean isReleverage()
    {
        return releverage;
    }

    public void setReleverage(boolean releverage)
    {
        this.releverage = releverage;
    }
    
    //
    // Utility methods
    //
    
    /**
     * Get a matched SegmentKey by current key in string.
     * 
     * @param p_tuTuvSubIDSet
     * @param p_currentKey
     * @return
     */
    public static SegmentKey findSegmentKey(
            CopyOnWriteArrayList p_tuTuvSubIDList, String p_currentKey)
    {
        if (p_tuTuvSubIDList == null || p_tuTuvSubIDList.size() == 0
                || p_currentKey == null || "".equals(p_currentKey.trim()))
        {
            return null;
        }

        SegmentKey result = null;

        Iterator keyIter = p_tuTuvSubIDList.iterator();
        while (keyIter.hasNext())
        {
            SegmentKey segKey = (SegmentKey) keyIter.next();
            String currKey = segKey.getCurrentKey();
            if (p_currentKey.equals(currKey))
            {
                result = segKey;
                break;
            }
        }

        return result;
    }
    
    /**
     * Reset tuID,tuvID,subID in the "EditorState" object.
     * 
     * @param p_state
     * @param p_key
     */
    public static void resetTuTuvSubIDs(EditorState p_state, String p_key)
    {
        if (p_state == null || p_key == null)
        {
            return;
        }
        
        try
        {
            String[] subKeys = p_key.split("_");
            long tuId = Long.parseLong(subKeys[0]);
            long tuvId = Long.parseLong(subKeys[1]);
            long subId = Long.parseLong(subKeys[2]);
            
            p_state.setTuId(tuId);
            p_state.setTuvId(tuvId);
            p_state.setSubId(subId);
        }
        catch (Exception e)
        {
            CATEGORY.error("Failed to reset tuID,tuvID and subID for key : "
                    + p_key);
            CATEGORY.error(e.getMessage());
        }
    }
    
    /**
     * Generate the unique key.
     * 
     * @param p_state
     * @return
     */
    public static String generateTuTuvSubIDKey(EditorState p_state)
    {
        if (p_state == null)
        {
            return null;
        }

        StringBuffer strBuff = new StringBuffer();
        strBuff.append(p_state.getTuId()).append("_")
                .append(p_state.getTuvId()).append("_").append(
                        p_state.getSubId());
        
        return strBuff.toString();
    }
    
    /**
     * Update the session.
     * 
     * @param sessionMgr
     * @param p_tuTuvSubIDSet
     * @param p_segmentKey
     */
    public static void updateSessionWithSegmentKey(SessionManager sessionMgr,
            CopyOnWriteArrayList p_tuTuvSubIDList, SegmentKey p_segmentKey)
    {
        if (p_tuTuvSubIDList == null || p_segmentKey == null)
        {
            return;
        }

        p_tuTuvSubIDList.remove(p_segmentKey);
        p_tuTuvSubIDList.add(p_segmentKey);

        sessionMgr.setAttribute(WebAppConstants.PAGE_TU_TUV_SUBID_SET,
                p_tuTuvSubIDList);
    }

}
