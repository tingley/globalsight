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

package com.globalsight.everest.tuv;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.globalsight.ling.docproc.extractor.xliff.XliffAlt;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.gxml.GxmlElement;

/**
 * The Tuv interface represents a translation unit variant. A translation unit
 * variant specifies text in a given Locale. The text is commonly referred to as
 * a segment. Tuv also contains meta-data about the text.
 */
public interface Tuv
{
    // merge state
    public static final String NOT_MERGED = "NOT_MERGED";
    public static final String MERGE_START = "MERGE_START";
    public static final String MERGE_MIDDLE = "MERGE_MIDDLE";
    public static final String MERGE_END = "MERGE_END";

    /**
     * Get Tuv unique identifier.
     * 
     * @return Tuv unique identifier.
     */
    public long getId();

    /**
     * <p>
     * Return the persistent object's id as a Long object.
     * <p>
     * 
     * <p>
     * This is a convenience method that simply wraps the id as an object, so
     * that, for example, the idAsLong can be used as a Hashtable key.
     * </p>
     * 
     * @return the unique identifier as a Long object.
     */
    public Long getIdAsLong();

    /**
     * Get Tuv content in Gxml format. The string will be a segment or
     * localizable element including it's tags.
     * 
     * @return Tuv content in Gxml format.
     */
    public String getGxml();

    /**
     * Return true if the Tuv has been indexed; false otherwise.
     * 
     * @return whether the Tuv has been indexed
     */
    public boolean isIndexed();

    /**
     * Indicate that the Tuv has been indexed. After executing this method, a
     * call to isIndexed() will always return true for this Tuv.
     */
    public void makeIndexed();

    /**
     * Get Tuv content in Gxml format. The string will be a segment or
     * localizable element excluding the top level tags.
     * 
     * @return Tuv content in Gxml format excluding the top level tags.
     */
    public String getGxmlExcludeTopTags();

    /**
     * Get Tuv content in format of a GxmlElement. The element will be a segment
     * or localizable element.
     * 
     * @return Tuv content in format of a GxmlElement.
     */
    public GxmlElement getGxmlElement();

    /**
     * Set Tuv content in format of a GxmlElement. It should be a segment or
     * localizable element.
     * 
     * @param Tuv
     *            content in format of a GxmlElement.
     */
    public void setGxmlElement(GxmlElement p_gxmlElement);

    /**
     * Get Tuv DataType.
     * 
     * @return Tuv DataType.
     */
    public String getDataType(long jobId);

    /**
     * Get Tuv Locale identifier.
     * 
     * @return Tuv Locale identifier.
     */
    public long getLocaleId();

    /**
     * Get Tuv word count.
     * 
     * @return Tuv word count.
     */
    public int getWordCount();

    /**
     * Set the order of this Tuv - with regards to the page.
     */
    public void setOrder(long p_order);

    public long getOrder();

    /**
     * Set timestamp when user last modified the segment.
     */
    public void setLastModified(Date p_now);

    /**
     * Get timestamp when user last modified the segment.
     */
    public Date getLastModified();

    public void setLastModifiedUser(String p_user);

    public String getLastModifiedUser();

    public void setCreatedDate(Date p_date);

    public Date getCreatedDate();

    public void setCreatedUser(String p_user);

    public String getCreatedUser();

    public void setUpdatedProject(String p_project);

    public String getUpdatedProject();

    /**
     * Get subflow elements.
     * 
     * @return List of GxmlElements of subflow elements.
     */
    public List getSubflowsAsGxmlElements();

    public List getSubflowsAsGxmlElements(boolean fromPage);

    /**
     * Get a subflow element by subflowId.
     * 
     * @return GxmlElement of subflow element.
     */
    public GxmlElement getSubflowAsGxmlElement(String p_subflowId);

    /**
     * Get subflow element parents.
     * 
     * @return List of GxmlElements of subflow parent elements.
     */
    public List getSubflowParentsAsGxmlElements();

    /**
     * <p>
     * Set subflow elements as a Map of subflow content not including the sub
     * tags, keyed by the sub id attribute.
     * </p>
     * 
     * <p>
     * Whatever subflows are in the Map will overwrite the current ones, based
     * on the id attribute. Current ones that are not in the Map remain the
     * same.
     * </p>
     * 
     * @param p_map
     *            Map of subflow content not including the sub tags, keyed by
     *            the sub id attribute. Cannot be empty or null.
     */
    public void setSubflowsGxml(Map p_map);

    /**
     * Set a subflow Gxml content.
     * <p>
     * 
     * @param p_subflowId
     *            the subflow id attribute.
     * @param p_subflowContent
     *            the subflow content Gxml String not including the sub tags.
     */
    public void setSubflowGxml(String p_subflowId, String p_subflowContent);

    /**
     * Set the text of the segment with a Gxml string of a segment or
     * localizable element including it's tags, but ignore any subflows in the
     * string. Any subflows in the string must have id attributes set.
     * 
     * @param p_gxml
     *            Gxml String.
     */
    public void setGxmlIgnoreSubflows(String p_gxml);

    /**
     * Set the text of the segment with a Gxml string of a segment or
     * localizable element excluding it's top level tags, but ignore any
     * subflows in the string. Any subflows in the string must have id
     * attributes set.
     * 
     * @param p_gxml
     *            Gxml String.
     */
    public void setGxmlExcludeTopTagsIgnoreSubflows(String p_gxml, long jobId);

    /**
     * Set the text of the segment with a Gxml string. The string must be a
     * segment or localizable element including it's tags. All the sub elements
     * must not have the id attribute set. This method will set them.
     * 
     * @param p_gxml
     *            Gxml String.
     */
    public void setGxml(String p_gxml);

    /**
     * Set the text of the segment with a Gxml string. The string must be a
     * segment or localizable element excluding it's top level tags.
     * 
     * @param p_gxml
     *            Gxml String.
     */
    public void setGxmlExcludeTopTags(String p_gxml, long jobId);

    /**
     * Set the text of the segment with a Gxml string. The string must be a
     * segment or localizable element including its tags. All the sub elements
     * must have the id attribute set. This method will set them.
     * 
     * @param p_gxml
     *            Gxml String.
     */
    public void setGxmlWithSubIds(String p_gxml);

    /**
     * Get the tu that this tuv belongs to.
     * 
     * @return The Tu that this tuv belongs to.
     */
    public Tu getTu(long p_jobId);

    /**
     * Set the Tu that this tuv belongs to.
     * 
     * @param p_tu
     *            - The Tu to be set.
     */
    public void setTu(Tu p_tu);

    public long getTuId();

    public void setTuId(long p_tuId);

    /**
     * Returns true if it is in state LEVERAGE_GROUP_EXACT_MATCH_LOCALIZED. This
     * means that the Tuv was localized from the leveraging process by the fact
     * that there was an exact match in a previous version of the leverage
     * group.
     * 
     * @return true if it is in state LEVERAGE_GROUP_EXACT_MATCH_LOCALIZED.
     */
    public boolean isLeverageGroupExactMatchLocalized();

    /**
     * @return true if state is LEVERAGE_GROUP_EXACT_MATCH_LOCALIZED or
     *         EXACT_MATCH_LOCALIZED
     */
    public boolean isExactMatchLocalized(long jobId);

    /**
     * @return true if state the segment, including it's subflows, has not been
     *         localized.
     */
    public boolean isNotLocalized();

    /**
     * @return true if the state of the segment (including it's subflows?) has
     *         been localized.
     */
    public boolean isLocalized();

    /**
     * Set the LeverageMatchType. When content is copied into Tuv from a
     * leverage matching Tuv, the LeverageMatchType is set.
     * 
     * @see com.globalsight.ling.tm.LeverageMatchType
     * @see com.globalsight.everest.page.PagePersistenceAccessor
     * @param p_matchType
     *            the LeverageMatchType when content comes from a leverage
     *            matching Tuv.
     */
    public void setMatchType(String p_matchType);

    /**
     * Get the LeverageMatchType. When content is copied into Tuv from a
     * leverage matching Tuv, the LeverageMatchType is set.
     * 
     * @see com.globalsight.ling.tm.LeverageMatchType
     * @see com.globalsight.everest.page.PagePersistenceAccessor
     * @return the LeverageMatchType when content comes from a leverage matching
     *         Tuv.
     */
    public String getMatchType();

    /**
     * Returns true if it is localizable, false if translatable.
     * 
     * @return true if localizable, false if translatable
     */
    public boolean isLocalizable(long jobId);

    /**
     * Get Tuv GlobalSightLocale.
     * 
     * @return Tuv GlobalSightLocale.
     */
    public GlobalSightLocale getGlobalSightLocale();

    /**
     * @see com.globalsight.ling.tm.TuvLing#setExactMatchKey(long)
     */
    public void setExactMatchKey(long p_exactMatchKey);

    /**
     * @see com.globalsight.ling.tm.TuvLing#getExactMatchKey()
     */
    public long getExactMatchKey();

    public void setMergeState(String p_mergeState);

    public String getMergeState();

    /**
     * Get/Set state of Tuv
     */
    public TuvState getState();

    public String setState(TuvState p_state);

    public String getSid();

    public void setSid(String sid);

    public Set<XliffAlt> getXliffAlt(boolean p_loadDataFromDB);

    public void setXliffAlt(Set<XliffAlt> p_alt);

    public void addXliffAlt(XliffAlt alt);

    public void setSrcComment(String srcComment);

    public String getSrcComment();

    public boolean isRepeated();

    public void setRepeated(boolean repeated);

    public long getRepetitionOfId();

    public void setRepetitionOfId(long repetitionOfId);

	public long getPreviousHash();

	public void setPreviousHash(long previousHash);

	public long getNextHash();

	public void setNextHash(long nextHash);
}
