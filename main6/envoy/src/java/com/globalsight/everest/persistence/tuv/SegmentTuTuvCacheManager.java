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
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.globalsight.everest.tuv.TuImpl;
import com.globalsight.everest.tuv.TuvImpl;
import com.globalsight.ling.tm2.persistence.DbUtil;

public abstract class SegmentTuTuvCacheManager implements TuvQueryConstants
{
    static private final Logger logger = Logger
            .getLogger(SegmentTuTuvCacheManager.class);

    // TU constants
    public static final String REMOVED_TAG = "removed_tag";
    public static final String REMOVED_PREFIX_TAG = "removed_prefix_tag";
    public static final String REMOVED_SUFFIX_TAG = "removed_suffix_tag";
    // tuId : TuImpl
//    private static ConcurrentHashMap<Long, TuImpl> tuCache = new ConcurrentHashMap<Long, TuImpl>();
    // tuId : time
//    private static ConcurrentHashMap<Long, Long> tuLastAccessTimeCache = new ConcurrentHashMap<Long, Long>();
    // tuId : Set<String>
    private static ConcurrentHashMap<Long, Set<String>> tuExtraDataCache = new ConcurrentHashMap<Long, Set<String>>();

    // TUV constants
    public static final String XLIFF_ALT = "xliff_alt";
    public static final String ISSUE_EDITION_RELATION = "issue_edition_relation";
    // tuvId : TuvImpl
//    private static ConcurrentHashMap<Long, TuvImpl> tuvCache = new ConcurrentHashMap<Long, TuvImpl>();
    // tuvId : time
//    private static ConcurrentHashMap<Long, Long> tuvLastAccessTimeCache = new ConcurrentHashMap<Long, Long>();
    // tuvId : Set<String>
//    private static ConcurrentHashMap<Long, Set<String>> tuvExtraDataCache = new ConcurrentHashMap<Long, Set<String>>();
    
    private static ThreadLocal<ConcurrentHashMap<Long, TuvImpl>> TUV_CACHE = new ThreadLocal<ConcurrentHashMap<Long, TuvImpl>>();
    private static ThreadLocal<ConcurrentHashMap<Long, TuImpl>> TU_CACHE = new ThreadLocal<ConcurrentHashMap<Long, TuImpl>>();
    private static ThreadLocal<ConcurrentHashMap<Long, Set<String>>> TUV_EXTAR_DATA_CACHE = new ThreadLocal<ConcurrentHashMap<Long, Set<String>>>();

    /**
     * Get cached Tu by tuId.
     * 
     * @param p_tuId
     * @return TuImpl
     */
    public static TuImpl getTuFromCache(long p_tuId)
    {
    	ConcurrentHashMap<Long, TuImpl> tuCache = getTuCache();
    	
        TuImpl tu = tuCache.get(p_tuId);
        
        return tu;
    }
    
    private static ConcurrentHashMap<Long, TuvImpl> getTuvCache()
    {
    	ConcurrentHashMap<Long, TuvImpl> map = TUV_CACHE.get();
    	if (map == null)
    	{
    		map = new ConcurrentHashMap<Long, TuvImpl>();
    		TUV_CACHE.set(map);
    	}
    	
    	return map;
    }
    
    private static ConcurrentHashMap<Long, TuImpl> getTuCache()
    {
    	ConcurrentHashMap<Long, TuImpl> map = TU_CACHE.get();
    	if (map == null)
    	{
    		map = new ConcurrentHashMap<Long, TuImpl>();
    		TU_CACHE.set(map);
    	}
    	
    	return map;
    }
    
    private static ConcurrentHashMap<Long, Set<String>> getTuvExtraDataCache()
    {
    	ConcurrentHashMap<Long, Set<String>> map = TUV_EXTAR_DATA_CACHE.get();
    	if (map == null)
    	{
    		map = new ConcurrentHashMap<Long, Set<String>>();
    		TUV_EXTAR_DATA_CACHE.set(map);
    	}
    	
    	return map;
    }

    /**
     * Put Tu into Cache.
     * 
     * @param p_tu
     * @param p_synchronizeCachedTuv
     *            -- If Tu is changed, true; if no change, false.
     */
    public static void setTuIntoCache(final TuImpl p_tu,
            boolean p_synchronizeCachedTuv, long jobId)
    {
    	ConcurrentHashMap<Long, TuvImpl> tuvCache = getTuvCache();
    	ConcurrentHashMap<Long, TuImpl> tuCache = getTuCache();
    	
        if (p_tu != null)
        {
            tuCache.put(p_tu.getIdAsLong(), p_tu);
            if (String.valueOf(tuCache.size()).endsWith("0000"))
            {
                logger.debug("Cached TU object number is approaching "
                        + tuCache.size());
            }

            if (p_synchronizeCachedTuv)
            {
                Iterator tuvIter = p_tu.getTuvs(false, jobId).iterator();
                while (tuvIter.hasNext())
                {
                    TuvImpl tuv = (TuvImpl) tuvIter.next();
                    tuv.setTu(p_tu);
                    tuvCache.put(tuv.getIdAsLong(), tuv);
                    if (String.valueOf(tuvCache.size()).endsWith("0000"))
                    {
                        logger.debug("Cached TUV object number is approaching "
                                + tuvCache.size());
                    }
                }
            }
        }
    }

    /**
     * Get cached Tuv by tuvId.
     * 
     * @param p_tuvId
     * @return TuvImpl
     */
    public static TuvImpl getTuvFromCache(long p_tuvId)
    {
    	ConcurrentHashMap<Long, TuvImpl> tuvCache = getTuvCache();
    	
        TuvImpl tuv = tuvCache.get(p_tuvId);
        if (tuv != null)
        {
            return tuv;
        }

        return null;
    }

    /**
     * Put Tuv into cache.
     * 
     * @param p_tuv
     */
    public static void setTuvIntoCache(final TuvImpl p_tuv)
    {
    	ConcurrentHashMap<Long, TuvImpl> tuvCache = getTuvCache();
    	ConcurrentHashMap<Long, TuImpl> tuCache = getTuCache();
    	
        if (p_tuv != null && !"OUT_OF_DATE".equals(p_tuv.getState().getName()))
        {
            tuvCache.put(p_tuv.getIdAsLong(), p_tuv);
            if (String.valueOf(tuvCache.size()).endsWith("0000"))
            {
                logger.debug("Cached TUV object number is approaching "
                        + tuvCache.size());
            }
            TuImpl tu = tuCache.get(p_tuv.getTuId());
            if (tu != null)
            {
                tu.addTuv(p_tuv);
                p_tuv.setTu(tu);
            }
        }
    }

    public static void removeTuFromCache(long p_tuId)
    {
    	ConcurrentHashMap<Long, TuImpl> tuCache = getTuCache();
        tuCache.remove(p_tuId);
        tuExtraDataCache.remove(p_tuId);
    }

    public static void removeTuvFromCache(long p_tuvId)
    {
    	ConcurrentHashMap<Long, TuvImpl> tuvCache = getTuvCache();
    	ConcurrentHashMap<Long, Set<String>> tuvExtraDataCache = getTuvExtraDataCache();
    	
        tuvCache.remove(p_tuvId);
        tuvExtraDataCache.remove(p_tuvId);
    }

    /**
     * Record what data have been loaded into TU.
     * 
     * @param p_tuId
     * @param p_extraInfoName
     *            -- options:REMOVED_TAG, REMOVED_PREFIX_TAG, REMOVED_SUFFIX_TAG
     */
    public static void recordWhichTuExtraDataAlreadyLoaded(Long p_tuId,
            String p_extraInfoName)
    {
        Set<String> whatHaveLoaded = tuExtraDataCache.get(p_tuId);
        if (whatHaveLoaded == null)
        {
            whatHaveLoaded = new HashSet<String>();
        }

        whatHaveLoaded.add(p_extraInfoName);
        tuExtraDataCache.put(p_tuId, whatHaveLoaded);
    }

    /**
     * Record what data have been loaded into TUV.
     * 
     * @param p_tuvId
     * @param p_extraInfoName
     *            -- options:XLIFF_ALT, ISSUE_EDITION_RELATION
     */
    public static void recordWhichTuvExtraDataAlreadyLoaded(Long p_tuvId,
            String p_extraInfoName)
    {
    	ConcurrentHashMap<Long, Set<String>> tuvExtraDataCache = getTuvExtraDataCache();
    	
        Set<String> whatHaveLoaded = tuvExtraDataCache.get(p_tuvId);
        if (whatHaveLoaded == null)
        {
            whatHaveLoaded = new HashSet<String>();
        }

        whatHaveLoaded.add(p_extraInfoName);
        tuvExtraDataCache.put(p_tuvId, whatHaveLoaded);
    }

    /**
     * Judge if specified extra data have been loaded into TUV.If not and
     * required,need load it manually.
     * 
     * @param p_tuvId
     * @param p_extraInfoName
     * @return boolean
     */
    public static boolean isTuvExtraDataLoaded(Long p_tuvId,
            String p_extraInfoName)
    {
    	ConcurrentHashMap<Long, Set<String>> tuvExtraDataCache = getTuvExtraDataCache();
    	
        Set<String> whatHaveLoaded = tuvExtraDataCache.get(p_tuvId);
        if (whatHaveLoaded == null || whatHaveLoaded.size() == 0)
        {
            return false;
        }

        if (whatHaveLoaded.contains(p_extraInfoName))
        {
            return true;
        }

        return false;
    }

    /**
     * Judge if specified extra data have been loaded into TU.If not and
     * required,need load it manually.
     * 
     * @param p_tuId
     * @param p_extraInfoName
     * @return boolean
     */
    public static boolean isTuExtraDataLoaded(Long p_tuId,
            String p_extraInfoName)
    {
        Set<String> whatHaveLoaded = tuExtraDataCache.get(p_tuId);
        if (whatHaveLoaded == null || whatHaveLoaded.size() == 0)
        {
            return false;
        }

        if (whatHaveLoaded.contains(p_extraInfoName))
        {
            return true;
        }

        return false;
    }

    /**
     * Reduce the cache. For now, this is triggered when someone log out.And the
     * expired time is 30 minutes.
     */
    public static void clearUnTouchedTuTuvs()
    {
    	//do nothing
    }
    
    public static void clearCache()
    {
    	getTuCache().clear();
    	getTuvCache().clear();
    	getTuvExtraDataCache().clear();
    }

    /**
     * Valid string means not-null and not-empty after trimmed.
     * 
     * @param p_string
     * @return
     */
    public static boolean isValidString(String p_string)
    {
        if (p_string != null && !"".equals(p_string.trim()))
        {
            return true;
        }

        return false;
    }

    /**
     * Valid number means it is a number in String.
     * 
     * @param p_string
     * @return
     */
    public static boolean isValidNumber(String p_string)
    {
        if (p_string == null)
            return false;

        try
        {
            Double.parseDouble(p_string);
        }
        catch (NumberFormatException e)
        {
            return false;
        }

        return true;
    }

    /**
     * Release ResultSet, Statement, Connection resources.
     * 
     * @param rs
     * @param ps
     * @param connection
     */
    public static void releaseRsPsConnection(ResultSet rs, Statement ps,
            Connection connection)
    {
        DbUtil.silentClose(rs);
        DbUtil.silentClose(ps);
        if (connection != null)
        {
            DbUtil.silentReturnConnection(connection);
        }
    }
}
