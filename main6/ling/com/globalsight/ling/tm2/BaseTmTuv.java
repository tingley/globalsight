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
package com.globalsight.ling.tm2;

import com.globalsight.util.GlobalSightLocale;
import com.globalsight.ling.tm.LingManagerException;

import java.util.Collection;
import java.sql.Timestamp;

/**
 * BaseTmTuv is an interface that defines interfaces of various
 * Translation Unit Variant representations. Translation Unit Variant
 * has a text in a given locale and its meta data. One source and one
 * or more Translation Unit Variants form a Translation Unit.  The
 * term Translation Unit and Translation Unit Variant are taken from
 * TMX (http://www.lisa.org/tmx) specification.
 */

public interface BaseTmTuv
{
	public static final int FIRST_HASH = 0;
	public static final int LAST_HASH = 1;

	public long getId();
    
    public void setId(long p_id);
    
    /**
     * Get the tu that this tuv belongs to.
     * @return The Tu that this tuv belongs to.
     */
    public BaseTmTu getTu();

    /**
     * Set the Tu that this tuv belongs to.
     * @param p_tu - The Tu to be set.
     */
    public void setTu(BaseTmTu p_tu);

    /**
     * Get the segment string.
     * @return segment string
     */
    public String getSegment();
    
    /**
     * Get the segment string without top tag
     * @return segment string
     */
    public String getSegmentNoTopTag()
        throws LingManagerException;
    
    /**
     * Set the segment string.
     * @param p_segment segment string
     */
    public void setSegment(String p_segment);

    /**
     * Get the exact match key.
     * @return exact match key
     */
    public long getExactMatchKey();
    
    /**
     * Set the exact match key.
     * @param p_exactMatchKey exact match key
     */
    public void setExactMatchKey(long p_exactMatchKey);
    
    /**
     * Set the exact match key. This method calculates the exact match
     * key based on m_segment and set the key.
     */
    public void setExactMatchKey()
        throws LingManagerException;
    
    /**
     * Get the locale
     * @return GlobalSightLocale
     */
    public GlobalSightLocale getLocale();
    
    /**
     * Set the locale
     * @param p_locale GlobalSightLocale
     */
    public void setLocale(GlobalSightLocale p_locale);
    
    public String getType();

    public boolean isTranslatable();
    
    public String getCreationUser();
    
    public void setCreationUser(String p_creationUser);
    
    public Timestamp getCreationDate();
    
    public void setCreationDate(Timestamp p_creationDate);
    
    public String getModifyUser();
    
    public void setModifyUser(String p_modifyUser);

    public Timestamp getModifyDate();
    
    public void setModifyDate(Timestamp p_modifyDate);
    
    public void setUpdatedProject(String p_project);
    
    public String getUpdatedProject();

    /**
     * Get the native formatted string - used to generate CRCs.
     *
     * @return The native formatted string.
     */
    public String getExactMatchFormat()
        throws LingManagerException;

    /**
     * Get translatable and localizable content only from the Tuv -
     * used to generate fuzzy indexes. That is, the text plus subflows
     * in order.
     *
     * @return tuv without formatting.
     */
    public String getFuzzyIndexFormat()
        throws LingManagerException;

    /** 
     * Prepare the segment string for saving to or leveraging from
     * Segment TM. This method does the followings.
     *
     * 1) remove native formatting codes. For example, '<bpt
     * type="link" x="1">some code</bpt>' becomes '<bpt type="link"
     * x="1"/>'.
     *
     * 2) separates subflows out from the main text and make its own
     * segment. Subflows segment will have <segment> or <localizable>
     * top level tag according to its localizable type.
     *
     * <sub> elements in segment text in this object must have id
     * attribute set with unique sub id values. This is supposed to be
     * already done if the text comes from job data
     * (translation_unit_variant).
     *
     * @return Processed strings are returned in a SegmentAttributes
     * object. The object has an attribute of sub id. For the main
     * text, its sub id is "0".
     */
    public Collection prepareForSegmentTm()
        throws LingManagerException;

    public boolean isClobSegment();

    public Object clone();

    public String toDebugString();
    
    public String getSid();

    public void setSid(String sid);

    public void setLastUsageDate(Timestamp lastUsageDate);

    public Timestamp getLastUsageDate();

    public void setJobId(long jobId);

    public long getJobId();

    public void setJobName(String jobName);

    public String getJobName();

    public void setPreviousHash(long previousHash);

    public long getPreviousHash();

    public void setNextHash(long nextHash);

    public long getNextHash();
}
