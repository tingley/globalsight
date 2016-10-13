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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.globalsight.everest.page.ExtractedFile;
import com.globalsight.everest.page.ExtractedSourceFile;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.tuv.RemovedPrefixTag;
import com.globalsight.everest.tuv.RemovedSuffixTag;
import com.globalsight.everest.tuv.RemovedTag;
import com.globalsight.everest.tuv.TuImpl;
import com.globalsight.ling.docproc.IFormatNames;
import com.globalsight.persistence.hibernate.HibernateUtil;

/**
 * Helper for maintain removed tags.
 * 
 * @author york.jin
 * @since 2012-03-22
 * @version 8.2.3
 */
public class RemovedTagsUtil extends SegmentTuTuvCacheManager implements
        TuvQueryConstants
{
    static private final Logger logger = 
            Logger.getLogger(RemovedTagsUtil.class);
    
    private static final String LOAD_REMOVED_TAGS_BY_SPID_SQL =
            "SELECT tag.* from removed_tag tag, "
            + TU_TABLE_PLACEHOLDER + " tu, "
            + "source_page_leverage_group splg "
            + "WHERE tag.tu_id = tu.id "
            + "AND tu.leverage_group_id = splg.lg_id "
            + "AND splg.sp_id = ? ";
    
    private static final String LOAD_REMOVED_PREFIX_TAG_BY_SPID_SQL =
            "SELECT tag.* from removed_prefix_tag tag, "
            + TU_TABLE_PLACEHOLDER + " tu, "
            + "source_page_leverage_group splg "
            + "WHERE tag.tu_id = tu.id "
            + "AND tu.leverage_group_id = splg.lg_id "
            + "AND splg.sp_id = ? ";

    private static final String LOAD_REMOVED_SUFFIX_TAG_BY_SPID_SQL =
            "SELECT tag.* from removed_suffix_tag tag, "
            + TU_TABLE_PLACEHOLDER + " tu, "
            + "source_page_leverage_group splg "
            + "WHERE tag.tu_id = tu.id "
            + "AND tu.leverage_group_id = splg.lg_id "
            + "AND splg.sp_id = ? ";
    
    /**
     * Save removed tags, prefix tags and suffix tags for current TU.
     * @param p_tus
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static void saveAllRemovedTags(Collection p_tus) throws Exception
    {
        List<RemovedTag> removedTagList = new ArrayList<RemovedTag>();
        List<RemovedPrefixTag> removedPreTagList = new ArrayList<RemovedPrefixTag>();
        List<RemovedSuffixTag> removedSufTagList = new ArrayList<RemovedSuffixTag>();
        
        for (Iterator it = p_tus.iterator(); it.hasNext();) {
            TuImpl tu = (TuImpl) it.next();
            removedTagList.addAll(tu.getRemovedTags());
            if (tu.getPrefixTag() != null) {
                removedPreTagList.add(tu.getPrefixTag());
            }
            if (tu.getSuffixTag() != null) {
                removedSufTagList.add(tu.getSuffixTag());
            }
        }
        
        HibernateUtil.save(removedTagList);
        HibernateUtil.save(removedPreTagList);
        HibernateUtil.save(removedSufTagList);
    }
    
    /**
     * Load "RemovedTag", "RemovedPrefixTag", "RemovedSuffixTag" data for
     * specified TUs.
     * 
     * @param p_tus
     * @param p_sourcePageId
     * @param p_tuTableName -- TU table name job data in.
     */
    public static void loadAllRemovedTagsForTus(List<TuImpl> p_tus,
            long p_sourcePageId, String p_tuTableName)
    {
        loadRemovedTags(p_tus, p_sourcePageId, p_tuTableName);
        loadPrefixTag(p_tus, p_sourcePageId, p_tuTableName);
        loadSuffixTag(p_tus, p_sourcePageId, p_tuTableName);
    }

    private static void loadRemovedTags(List<TuImpl> p_tus,
            long p_sourcePageId, String p_tuTableName)
    {
        String sql = LOAD_REMOVED_TAGS_BY_SPID_SQL.replace(
                TU_TABLE_PLACEHOLDER, p_tuTableName);
        List<RemovedTag> removedTags = HibernateUtil.searchWithSql(
                RemovedTag.class, sql, p_sourcePageId);
        
        // tuId : Set<RemovedTag>
        Map<Long, Set<RemovedTag>> tagMap = new HashMap<Long, Set<RemovedTag>>();
        for (RemovedTag tag : removedTags){
            Set<RemovedTag> myTags = tagMap.get(tag.getTuId());
            if (myTags == null){
                myTags = new HashSet<RemovedTag>();
                myTags.add(tag);
                tagMap.put(tag.getTuId(), myTags);
            } else {
                myTags.add(tag);
                tagMap.put(tag.getTuId(), myTags);
            }
        }

        // Loop TUs
        if (tagMap.size() > 0){
            for (TuImpl tu : p_tus){
                Set<RemovedTag> mytags = tagMap.get(tu.getIdAsLong());
                if (mytags != null && mytags.size() > 0){
                    tu.setRemovedTags(mytags);
                }
            }
        }

        //
        for (TuImpl tu : p_tus){
            recordWhichTuExtraDataAlreadyLoaded(tu.getIdAsLong(), REMOVED_TAG);
        }
    }
    
    private static void loadPrefixTag(List<TuImpl> p_tus, long p_sourcePageId,
            String p_tuTableName)
    {
        String sql = LOAD_REMOVED_PREFIX_TAG_BY_SPID_SQL.replace(
                TU_TABLE_PLACEHOLDER, p_tuTableName);
        List<RemovedPrefixTag> prefixTags = HibernateUtil.searchWithSql(
                RemovedPrefixTag.class, sql, p_sourcePageId);
        
        // tuId : RemovedPrefixTag>
        Map<Long, RemovedPrefixTag> tagMap = new HashMap<Long, RemovedPrefixTag>();
        for (RemovedPrefixTag tag : prefixTags){
//            RemovedPrefixTag myPrefixTag = tagMap.get(tag.getTuId());
            tagMap.put(tag.getTuId(), tag);
        }

        // Loop TUs
        if (tagMap.size() > 0){
            for (TuImpl tu : p_tus){
                RemovedPrefixTag myPrefixTag = tagMap.get(tu.getIdAsLong());
                if (myPrefixTag != null){
                    tu.setPrefixTag(myPrefixTag);
                }
            }
        }
        
        //
        for (TuImpl tu : p_tus){
            recordWhichTuExtraDataAlreadyLoaded(tu.getIdAsLong(), REMOVED_PREFIX_TAG);
        }
    }
    
    private static void loadSuffixTag(List<TuImpl> p_tus, long p_sourcePageId,
            String p_tuTableName)
    {
        String sql = LOAD_REMOVED_SUFFIX_TAG_BY_SPID_SQL.replace(
                TU_TABLE_PLACEHOLDER, p_tuTableName);
        List<RemovedSuffixTag> suffixTags = HibernateUtil.searchWithSql(
                RemovedSuffixTag.class, sql, p_sourcePageId);
        
        // tuId : RemovedPrefixTag>
        Map<Long, RemovedSuffixTag> tagMap = new HashMap<Long, RemovedSuffixTag>();
        for (RemovedSuffixTag tag : suffixTags){
//            RemovedSuffixTag mySuffixTag = tagMap.get(tag.getTuId());
            tagMap.put(tag.getTuId(), tag);
        }

        // Loop TUs
        if (tagMap.size() > 0){
            for (TuImpl tu : p_tus){
                RemovedSuffixTag mySuffixTag = tagMap.get(tu.getIdAsLong());
                if (mySuffixTag != null){
                    tu.setSuffixTag(mySuffixTag);
                }
            }
        }

        //
        for (TuImpl tu : p_tus){
            recordWhichTuExtraDataAlreadyLoaded(tu.getIdAsLong(), REMOVED_SUFFIX_TAG);
        }
    }
    
    /**
     * Load "RemovedTag", "RemovedPrefixTag", "RemovedSuffixTag" data for
     * specified TU.
     * 
     * @param p_tu
     */
    public static void loadAllRemovedTagsForTu(TuImpl p_tu)
    {
        loadRemovedTags(p_tu);
        loadRemovedPrefixTag(p_tu);
        loadRemovedSuffixTag(p_tu);
    }
    
    /**
     * Load RemovedTags for TU.
     * 
     * @param p_tu
     */
    @SuppressWarnings("unchecked")
    private static void loadRemovedTags(TuImpl p_tu)
    {
        boolean isRemovedTagLoaded = isTuExtraDataLoaded(p_tu.getIdAsLong(), REMOVED_TAG);
        if (!isRemovedTagLoaded){
            String hql = "from RemovedTag rt where rt.tuId = " + p_tu.getId();
            List<RemovedTag> tags = (List<RemovedTag>) HibernateUtil.search(hql);

            if (tags != null && tags.size() > 0){
                for (RemovedTag tag : tags){
                    tag.setTu(p_tu);
                }

                Set<RemovedTag> removedTags = new HashSet<RemovedTag>(tags);
                p_tu.setRemovedTags(removedTags);
            }

            recordWhichTuExtraDataAlreadyLoaded(p_tu.getIdAsLong(), REMOVED_TAG);
        }
    }
    
    /**
     * Load RemovedPrefixTag for TU.
     * 
     * @param p_tu
     */
    private static void loadRemovedPrefixTag(TuImpl p_tu)
    {
        if (p_tu == null) return;

        boolean isPrefixTagLoaded = isTuExtraDataLoaded(p_tu.getIdAsLong(), REMOVED_PREFIX_TAG);
        if (!isPrefixTagLoaded){
            String hql = "from RemovedPrefixTag rpt where rpt.tuId = " + p_tu.getId();
            Object obj = HibernateUtil.getFirst(hql);

            if (obj != null) {
                p_tu.setPrefixTag((RemovedPrefixTag) obj);
            }

            recordWhichTuExtraDataAlreadyLoaded(p_tu.getIdAsLong(), REMOVED_PREFIX_TAG);
        }
    }

    /**
     * Load RemovedSuffixTag for TU.
     * 
     * @param p_tu
     */
    private static void loadRemovedSuffixTag(TuImpl p_tu)
    {
        boolean isSuffixTagLoaded = isTuExtraDataLoaded(p_tu.getIdAsLong(), REMOVED_SUFFIX_TAG);
        if (!isSuffixTagLoaded){
            String hql = "from RemovedSuffixTag rst where rst.tuId = " + p_tu.getId();
            Object obj = HibernateUtil.getFirst(hql);

            if (obj != null) {
                p_tu.setSuffixTag((RemovedSuffixTag) obj);
            }
            
            recordWhichTuExtraDataAlreadyLoaded(p_tu.getIdAsLong(), REMOVED_SUFFIX_TAG);
        }
    }

    /**
     * Office2010("docx", "xlsx" and "pptx"), openoffice("odt", "odp", and
     * "ods") and IDML file formats will generate removed tags into
     * "removed_tag", "removed_suffix_tag" and "removed_prefix_tag".
     * 
     * @param p_sourcePageId
     * @return boolean
     */
    public static boolean isGenerateRemovedTags(Long p_sourcePageId)
    {
        try
        {
            SourcePage sp = ServerProxy.getPageManager().getSourcePage(
                    p_sourcePageId);
            if (sp.getPrimaryFileType() == ExtractedFile.EXTRACTED_FILE)
            {
                ExtractedFile ef = (ExtractedFile) sp.getPrimaryFile();
                String dataType = ((ExtractedSourceFile) ef).getDataType();
                boolean isOffice2010 = IFormatNames.FORMAT_OFFICE_XML
                        .equalsIgnoreCase(dataType);
                boolean isOpenOffice = IFormatNames.FORMAT_OPENOFFICE_XML
                        .equalsIgnoreCase(dataType);
                boolean isIdml = (IFormatNames.FORMAT_XML
                        .equalsIgnoreCase(dataType) && sp.getExternalPageId()
                        .toLowerCase().endsWith(".idml"));
                if (isOffice2010 || isOpenOffice || isIdml)
                {
                    return true;
                }
            }
        }
        catch (Exception ignore)
        {
            return false;
        }
        return false;
    }
}
