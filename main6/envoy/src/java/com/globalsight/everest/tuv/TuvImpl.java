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

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.globalsight.everest.comment.IssueImpl;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.ling.docproc.extractor.xliff.XliffAlt;
import com.globalsight.ling.tm.LeverageMatchType;
import com.globalsight.ling.tm.TuLing;
import com.globalsight.ling.tm.TuvLing;
import com.globalsight.log.GlobalSightCategory;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.edit.GxmlUtil;
import com.globalsight.util.gxml.GxmlElement;
import com.globalsight.util.gxml.GxmlFragmentReader;
import com.globalsight.util.gxml.GxmlFragmentReaderPool;
import com.globalsight.util.gxml.GxmlNames;

/**
 * Implements Tuv.
 * 
 * @see Tuv
 */
public final class TuvImpl extends TuvLing implements Tuv, Serializable
{
    private static final long serialVersionUID = 1L;

    private static GlobalSightCategory c_category = (GlobalSightCategory) GlobalSightCategory
            .getLogger(TuvImpl.class);

    // TOPLink query names
    public static final String M_STATE = "m_state";
    public static final String M_GLOBAL_SIGHT_LOCALE = "m_globalSightLocale";
    public static final String M_TUV_ID = PersistentObject.M_ID;
    static final String M_SOURCE_PAGE = "m_sourcePage";
    public static final String M_TU = "m_tu";
    public static final String M_ORDER = "m_order";
    public static final String M_EXACT_MATCH_KEY = "m_exactMatchKey";

    /**
     * TUV orders range from 1 - n. The default value is 0.
     */
    private long m_order = 0;
    private GlobalSightLocale m_globalSightLocale = null;
    private Tu m_tu = null;

    private Date m_lastModified = new Date();
    private String m_segmentClob = null;
    private String m_segmentString = null;
    private boolean m_isIndexed = false;

    private GxmlElement m_gxmlElement = null;
    private int m_wordCount = 0;
    private long m_exactMatchKey = 0;
    private String m_state = setState(TuvState.NOT_LOCALIZED);
    private String m_mergeState = NOT_MERGED;
    private SourcePage m_sourcePage = null;

    private List m_subflowParents = null;
    private Boolean m_dummySubflowParentsLock = new Boolean(true);
    private List m_subflows = null;

    private Date m_createdDate = new Date();
    private String m_createdUser = null;
    private String m_lastModifiedUser = null;
    private String m_updatedByProject = null;
    private String sid = null;
    private Set xliff_alt = null;
    private Set issue_edition_relation = null;

    /**
     * Holds the LeverageMatchType.
     * 
     * @see com.globalsight.ling.tm.LeverageMatchType
     */
    private String m_leverageMatchType = LeverageMatchType.UNKNOWN_NAME;

    public TuvImpl()
    {
    }

    public TuvImpl(GlobalSightLocale p_locale, Tu p_tu)
    {
        m_globalSightLocale = p_locale;
        m_tu = p_tu;
    }

    /**
     * Copy constructor.
     */
    public TuvImpl(TuvImpl p_other)
    {
        // Wed Jun 18 17:06:41 2003 CvdL: This used to performs a
        // shallow copy (pointer copy) of all fields and caused
        // multiple TUVs to work with the same GxmlElement which in
        // addition was out of sync with the segment string.
        setSegment(p_other.getSegment());
        m_gxmlElement = null;

        m_wordCount = p_other.m_wordCount;
        m_exactMatchKey = p_other.m_exactMatchKey;
        m_state = p_other.m_state;
        m_mergeState = p_other.m_mergeState;
        m_sourcePage = p_other.m_sourcePage;
        m_tu = p_other.m_tu;
        m_subflowParents = p_other.m_subflowParents;
        m_subflows = p_other.m_subflows;
        m_globalSightLocale = p_other.m_globalSightLocale;
        m_leverageMatchType = p_other.m_leverageMatchType;
        m_order = p_other.m_order;
        m_lastModified = p_other.m_lastModified;
        m_dummySubflowParentsLock = p_other.m_dummySubflowParentsLock;
        m_isIndexed = p_other.m_isIndexed;
        m_createdDate = p_other.m_createdDate;
        m_createdUser = p_other.m_createdUser;
        m_lastModifiedUser = p_other.m_lastModifiedUser;
        m_updatedByProject = p_other.m_updatedByProject;
        this.sid = p_other.sid;
    }

    //
    // Tuv interface methods
    //

    /**
     * Get Tuv content in Gxml format. The string will be a segment or
     * localizable element including its tags.
     * 
     * @return Tuv content in Gxml format.
     */
    public String getGxml()
    {
        String segment = getSegment();

        if (segment == null && m_gxmlElement != null)
        {
            segment = m_gxmlElement.toGxml();
            setSegment(segment);
        }

        return segment;
    }

    /**
     * Get Tuv content in Gxml format. The string will be a segment or
     * localizable element excluding the top level tags.
     * 
     * @return Tuv content in Gxml format excluding the top level tags.
     */
    public String getGxmlExcludeTopTags()
    {
        try
        {
            return GxmlUtil.stripRootTag(getGxml());
        }
        catch (Throwable t)
        {
            c_category
                    .info(
                            "getGxmlExcludeTopTags(): TUV ID "
                                    + getId()
                                    + " has a segment string with the old format of an empty element.  It is being handled.  Segment string = `"
                                    + getGxml() + "'.", t);

            return getGxmlElement().toGxmlExcludeTopTags();
        }
    }
    
    /*
     * If xml attribute has "<" in it ,it will parse error. 
     * This method replaces the attribute "<" into "&lt;"
     */
    public String encodeGxmlAttributeEntities(String segement) {
        //this flag for recording the xml element begin.
        boolean flagXML = false;
        //this flag for recording the attribute begin, because if in "<  >", 
        //and begin as double quote, it will be a attribute.
        boolean flagQuote = false;
        StringBuffer sb = new StringBuffer();
        
        for (int i = 0; i < segement.length(); i++)
        {
            char c = segement.charAt(i);
            
            if(!flagXML && !flagQuote && c == '<') {
                flagXML = true;
            }
            else if(flagXML && !flagQuote && c == '"') {
                flagQuote = true;
            }
            else  if(flagXML && !flagQuote && c == '>') {
                flagXML = false;
            }
            else if(flagXML && flagQuote && c == '"') {
                flagQuote = false;
            }
            
            if(flagXML && flagQuote && c == '<') {
                sb.append("&lt;");
            }
            else {
                sb.append(c);
            }
        }
        
        return sb.toString();
    }

    /**
     * Get Tuv content in format of a GxmlElement. The element will be a segment
     * or localizable element.
     * 
     * @return Tuv content in format of a GxmlElement.
     */
    public GxmlElement getGxmlElement()
    {
        if (m_gxmlElement == null)
        {
            String segment = encodeGxmlAttributeEntities(getSegment());
            GxmlFragmentReader reader = null;

            try
            {
                reader = GxmlFragmentReaderPool.instance()
                        .getGxmlFragmentReader();

                m_gxmlElement = reader.parseFragment(segment);
            }
            catch (Exception e)
            {
                c_category.error("Error in TuvImpl: " + toString(), e);
                // Can't have Tuv in inconsistent state, throw runtime
                // exception.
                throw new RuntimeException("Error in TuvImpl: "
                        + GeneralException.getStackTraceString(e));
            }
            finally
            {
                GxmlFragmentReaderPool.instance()
                        .freeGxmlFragmentReader(reader);
            }
        }

        return m_gxmlElement;
    }

    /**
     * Set Tuv content in format of a GxmlElement. It should be a segment or
     * localizable element.
     * 
     * @param p_element
     *            content in format of a GxmlElement.
     */
    public void setGxmlElement(GxmlElement p_element)
    {
        refreshContent(p_element);
    }

    /**
     * Return the value of the m_isIndexed flag.
     */
    public boolean isIndexed()
    {
        return m_isIndexed;
    }

    /**
     * Set the value of the m_isIndexed flag to true.
     */
    public void makeIndexed()
    {
        m_isIndexed = true;
    }

    /**
     * Get Tuv DataType.
     * 
     * @return Tuv DataType.
     */
    public String getDataType()
    {
        return m_tu == null ? null : m_tu.getDataType();
    }

    /**
     * Get Tuv Locale identifier.
     * 
     * @return Tuv Locale identifier.
     */
    public long getLocaleId()
    {
        return m_globalSightLocale == null ? 0 : m_globalSightLocale.getId();
    }

    /**
     * Get Tuv word count.
     * 
     * @return Tuv word count.
     */
    public int getWordCount()
    {
        return m_wordCount;
    }

    /**
     * Set the order of this Tuv - with regards to the page.
     */
    public void setOrder(long p_order)
    {
        m_order = p_order;
    }

    public long getOrder()
    {
        return m_order;
    }

    /**
     * Set timestamp when user last modified the segment.
     */
    public void setLastModified(Date p_now)
    {
        m_lastModified = p_now;
    }

    /**
     * Get timestamp when user last modified the segment.
     */
    public Date getLastModified()
    {
        return m_lastModified;
    }

    public void setLastModifiedUser(String p_user)
    {
        m_lastModifiedUser = p_user;
    }

    public String getLastModifiedUser()
    {
        return m_lastModifiedUser;
    }

    public void setCreatedDate(Date p_date)
    {
        m_createdDate = p_date;
    }

    public Date getCreatedDate()
    {
        return m_createdDate;
    }

    public void setCreatedUser(String p_user)
    {
        m_createdUser = p_user;
    }

    public String getCreatedUser()
    {
        return m_createdUser;
    }

    public void setUpdatedProject(String p_project)
    {
        m_updatedByProject = p_project;
    }

    public String getUpdatedProject()
    {
        return m_updatedByProject;
    }

    /**
     * Get subflow elements.
     * 
     * @return List of GxmlElements of subflow elements.
     */
    public List getSubflowsAsGxmlElements()
    {
        if (m_subflows == null)
        {
            m_subflows = getGxmlElement().getDescendantElements(
                    GxmlElement.SUB_TYPE);
        }

        return m_subflows;
    }

    public List getSubflowsAsGxmlElements(boolean fromPage)
    {
        if(m_subflows == null)
        {
            m_subflows = getGxmlElement().getDescendantElements(
                    GxmlElement.SUB_TYPE, fromPage);
        }
        return m_subflows;
    }
    /**
     * Get a subflow element by subflowId.
     * 
     * @return GxmlElement of subflow element.
     */
    public GxmlElement getSubflowAsGxmlElement(String p_subflowId)
    {
        GxmlElement subflow = null;
        List subflows = getSubflowsAsGxmlElements();

        if (subflows != null)
        {
            for (int i = 0; i < subflows.size(); i++)
            {
                GxmlElement sub = (GxmlElement) subflows.get(i);

                if (p_subflowId.equals(sub.getAttribute(GxmlNames.SUB_ID)))
                {
                    subflow = sub;
                    break;
                }
            }
        }

        return subflow;
    }

    /**
     * Get subflow element parents.
     * 
     * @return List of GxmlElements of subflow parent elements.
     */
    public List getSubflowParentsAsGxmlElements()
    {
        // Threads must obtain a lock from the member variable
        // m_dummySubflowParentsLock
        // This ensures that the m_subflowParents list is fully built before any
        // other threads
        // return a reference to the list.
        //
        // Note: The advantage to this type of synchronization (on a member
        // variable) is that other synchronized methods in this class are not
        // blocked.
        synchronized (m_dummySubflowParentsLock)
        {
            if (m_subflowParents == null)
            {
                List subflows = getSubflowsAsGxmlElements();

                if (subflows == null)
                {
                    m_subflowParents = new ArrayList(0);
                }
                else
                {
                    GxmlElement parent;
                    m_subflowParents = new ArrayList(subflows.size());

                    for (Iterator it = subflows.iterator(); it.hasNext();)
                    {
                        parent = ((GxmlElement) it.next()).getParent();
                        if (!m_subflowParents.contains(parent))
                        {
                            m_subflowParents.add(parent);
                        }
                    }
                }
            }
        }

        return m_subflowParents;
    }

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
     *            the sub id attribute. Cannot be null. If empty, nothing is
     *            done.
     */
    public void setSubflowsGxml(Map p_map)
    {
        if (p_map.size() == 0) { return; }

        Set keys = p_map.keySet();
        Iterator it = keys.iterator();
        Collection subs = new HashSet(p_map.size());
        GxmlFragmentReader reader = null;
        GxmlElement subElement = null;

        try
        {
            reader = GxmlFragmentReaderPool.instance().getGxmlFragmentReader();

            while (it.hasNext())
            {
                Object key = it.next();
                if (key == null)
                {
                    continue;
                }

                Object value = p_map.get(key);
                if (value == null)
                {
                    continue;
                }

                subElement = reader.parseFragment(getSubAsGxml((String) key,
                        (String) value));

                subs.add(subElement);
            }

            // don't clear m_gxmlElement here
            m_subflows = null;
            m_subflowParents = null;
            m_leverageMatchType = LeverageMatchType.UNKNOWN_NAME;
            setSegment(null);
            nullifyCacheFields();

            refreshContent(replaceSubs(subs, getGxmlElement()));
        }
        catch (Exception e)
        {
            c_category.error("p_map=" + p_map.toString() + " " + toString(), e);

            // Throw unchecked exception so clients don't have to
            // catch rare case.
            throw new RuntimeException("Error in Tuv: "
                    + GeneralException.getStackTraceString(e));
        }
        finally
        {
            GxmlFragmentReaderPool.instance().freeGxmlFragmentReader(reader);
        }
    }

    /**
     * Set a subflow Gxml content.
     * 
     * @param p_subflowId
     *            the subflow id attribute.
     * @param p_subflowContent
     *            the subflow content Gxml String not including the sub tags.
     */
    public void setSubflowGxml(String p_subflowId, String p_subflowContent)
    {
        GxmlFragmentReader reader = null;

        try
        {
            reader = GxmlFragmentReaderPool.instance().getGxmlFragmentReader();

            GxmlElement subElement = reader.parseFragment(getSubAsGxml(
                    p_subflowId, p_subflowContent));

            Collection subs = new HashSet(1);
            subs.add(subElement);

            GxmlElement root = getGxmlElement();
            refreshContent(replaceSubs(subs, root));
        }
        catch (Exception e)
        {
            c_category.error("p_subflowId=" + p_subflowId.toString()
                    + " p_subflowContent=" + p_subflowContent + " "
                    + toString(), e);
            // Throw unchecked exception so clients don't have to
            // catch rare case.
            throw new RuntimeException("Error in Tuv: "
                    + GeneralException.getStackTraceString(e));
        }
        finally
        {
            GxmlFragmentReaderPool.instance().freeGxmlFragmentReader(reader);
        }
    }

    /**
     * Set the text of the segment with a Gxml string of a segment or
     * localizable element including its tags, but ignore any subflows in the
     * string. Any subflows in the string must have id attributes set.
     * 
     * @param p_gxml
     *            Gxml String.
     */
    public void setGxmlIgnoreSubflows(String p_gxml)
    {
        GxmlFragmentReader reader = null;
        // GxmlElement subElement = null;
        GxmlElement gxmlRootElement = null;

        try
        {
            reader = GxmlFragmentReaderPool.instance().getGxmlFragmentReader();
            gxmlRootElement = reader.parseFragment(p_gxml);

            replaceSubs(getSubflowsAsGxmlElements(), gxmlRootElement);

            refreshContent(gxmlRootElement);
        }
        catch (Exception e)
        {
            c_category.error("p_gxml=" + p_gxml + " " + toString(), e);
            // Throw unchecked exception so clients don't have to
            // catch rare case.
            throw new RuntimeException("Error in Tuv: "
                    + GeneralException.getStackTraceString(e));
        }
        finally
        {
            GxmlFragmentReaderPool.instance().freeGxmlFragmentReader(reader);
        }
    }

    /**
     * Set the text of the segment with a Gxml string of a segment or
     * localizable element excluding its top level tags, but ignore any subflows
     * in the string. Any subflows in the string must have id attributes set.
     * 
     * @param p_gxml
     *            Gxml String.
     */
    public void setGxmlExcludeTopTagsIgnoreSubflows(String p_gxml)
    {
        setGxmlIgnoreSubflows(addTopTags(p_gxml));
    }

    /**
     * Set the text of the segment with a Gxml string. The string must be a
     * segment or localizable element including its tags. All the sub elements
     * must not have the id attribute set. This method will set them.
     * 
     * @param p_gxml
     *            Gxml String.
     */
    public void setGxml(String p_gxml)
    {
        setGxmlWithSubIds(p_gxml);
        boolean modified = setSubIdAttributes();

        if (modified)
        {
            // setSubIdAttributes() modifies the gxml element object
            // bringing it out of sync with the string. Reinitialize
            // this object from the current gxml element.
            //
            // Note that this way of initializing the object is very
            // hard to understand and should be changed. Setting the
            // sub ids should probably be moved to GxmlReader (which
            // reads an entire GXML file during import; not
            // GxmlFragmentReader, which should not default
            // unspecified attributes but only read what is in the
            // input), or PageImporter as a post-parse event to fill
            // in _all_ default attributes. (Like a normal XML reader
            // honoring the DTD would do, grmblfz!)
            refreshContent(m_gxmlElement);
        }
    }

    /**
     * Set the text of the segment with a Gxml string. The string must be a
     * segment or localizable element excluding its top level tags.
     * 
     * @param p_gxml
     *            Gxml String.
     */
    public void setGxmlExcludeTopTags(String p_gxml)
    {
        setGxml(addTopTags(p_gxml));
    }

    /**
     * Get the tu that this tuv belongs to.
     * 
     * @return The Tu that this tuv belongs to.
     */
    public Tu getTu()
    {
        return m_tu;
    }

    /**
     * Set the Tu that this tuv belongs to.
     * 
     * @param p_tu -
     *            The Tu to be set.
     */
    public void setTu(Tu p_tu)
    {
        m_tu = p_tu;
    }

    /**
     * Returns true if it is in state LEVERAGE_GROUP_EXACT_MATCH_LOCALIZED. This
     * means that the Tuv was localized from the leveraging process by the fact
     * that there was an exact match in a previous version of the leverage
     * group.
     * 
     * @return true if it is in state LEVERAGE_GROUP_EXACT_MATCH_LOCALIZED.
     */
    public boolean isLeverageGroupExactMatchLocalized()
    {
        return (m_state.equals(TuvState.LEVERAGE_GROUP_EXACT_MATCH_LOCALIZED
                .getName()));
    }

    /**
     * @return true if state is LEVERAGE_GROUP_EXACT_MATCH_LOCALIZED or
     *         EXACT_MATCH_LOCALIZED
     */
    public boolean isExactMatchLocalized()
    {
        if ("Xliff".equals(getLastModifiedUser()))
        {
            TuImpl tu = (TuImpl)getTu(); 
            if (tu != null && tu.isXliffLocked())
            {
                return true;
            }
        }
        
        // For PO file, if the last modified user is not changed since job is
        // created, that indicates the target is from PO "msgstr".
        if ("PO".equalsIgnoreCase(getLastModifiedUser()))
        {
            return true;
        }
        
        return (m_state.equals(TuvState.LEVERAGE_GROUP_EXACT_MATCH_LOCALIZED
                .getName()) || m_state.equals(TuvState.EXACT_MATCH_LOCALIZED
                .getName()));
    }

    /**
     * @return true if the state of the segment, including its subflows, has not
     *         been localized.
     */
    public boolean isNotLocalized()
    {
        boolean b = (!m_state.equals(TuvState.LOCALIZED.getName())
                && !m_state.equals(TuvState.EXACT_MATCH_LOCALIZED.getName())
                && !m_state
                        .equals(TuvState.LEVERAGE_GROUP_EXACT_MATCH_LOCALIZED
                                .getName())
                && !m_state.equals(TuvState.COMPLETE.getName())
                && !m_state.equals(TuvState.ALIGNMENT_LOCALIZED.getName()) && !m_state
                .equals(TuvState.UNVERIFIED_EXACT_MATCH));
        return b;
    }

    /**
     * @return true if the state of the segment (including its subflows?) has
     *         been localized.
     */
    public boolean isLocalized()
    {
        return (m_state.equals(TuvState.LOCALIZED.getName()));
    }

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
    public void setMatchType(String p_matchType)
    {
        m_leverageMatchType = p_matchType;

        if (LeverageMatchType.LEVERAGE_GROUP_EXACT_MATCH_NAME
                .equals(p_matchType))
        {
            setState(TuvState.LEVERAGE_GROUP_EXACT_MATCH_LOCALIZED);
        }
        else if (LeverageMatchType.EXACT_MATCH_SAME_TM_NAME.equals(p_matchType)
                || LeverageMatchType.EXACT_MATCH_NAME.equals(p_matchType))
        {
            setState(TuvState.EXACT_MATCH_LOCALIZED);
        }
        else if (LeverageMatchType.UNVERIFIED_EXACT_MATCH_NAME
                .equals(p_matchType))
        {
            setState(TuvState.UNVERIFIED_EXACT_MATCH);
        }
    }

    /**
     * Get the LeverageMatchType. When content is copied into Tuv from a
     * leverage matching Tuv, the LeverageMatchType is set.
     * 
     * @see com.globalsight.ling.tm.LeverageMatchType
     * @see com.globalsight.everest.page.PagePersistenceAccessor
     * @return the LeverageMatchType when content comes from a leverage matching
     *         Tuv.
     */
    public String getMatchType()
    {
        return m_leverageMatchType;
    }

    /**
     * Returns true if it is localizable, false if translatable.
     * 
     * @return true if localizable, false if translatable
     */
    public boolean isLocalizable()
    {
        Tu tu = getTu();
        return (tu != null && tu.isLocalizable());
    }

    //
    // TuvLing interface methods
    //

    /**
     * @see com.globalsight.ling.tm.TuvLing#getGlobalSightLocale()
     */
    public GlobalSightLocale getGlobalSightLocale()
    {
        return m_globalSightLocale;
    }

    /**
     * @see com.globalsight.ling.tm.TuvLing#getState()
     */
    // public long getState()
    // {
    // return stateName2StateId(m_state);
    // }

    /**
     * @see com.globalsight.ling.tm.TuvLing#getLocType()
     */
    public long getLocType()
    {
        return m_tu.isLocalizable() ? LOCALIZABLE : TRANSLATABLE;
    }

    /**
     * @see com.globalsight.ling.tm.TuvLing#getExactMatchKey()
     */
    public long getExactMatchKey()
    {
        return m_exactMatchKey;
    }

    /**
     * @see com.globalsight.ling.tm.TuvLing#setExactMatchKey(long)
     */
    public void setExactMatchKey(long p_exactMatchKey)
    {
        m_exactMatchKey = p_exactMatchKey;
    }

    /**
     * @see com.globalsight.ling.tm.TuvLing#getTuLing()
     */
    public TuLing getTuLing()
    {
        return m_tu;
    }

    /**
     * @see com.globalsight.ling.tm.TuvLing#isCompleted()
     */
    public boolean isCompleted()
    {
        if (m_state.equals(TuvState.COMPLETE.getName())
                || m_state.equals(TuvState.EXACT_MATCH_LOCALIZED.getName())
                || m_state.equals(TuvState.LEVERAGE_GROUP_EXACT_MATCH_LOCALIZED
                        .getName())
                || m_state.equals(TuvState.ALIGNMENT_LOCALIZED.getName())) { return true; }

        return false;
    }

    /**
     * Set the text of the segment with a Gxml string. The string must be a
     * segment or localizable element including its tags. All the sub elements
     * must have the id attribute set. This method will set them.
     * 
     * @param p_gxml
     *            Gxml String.
     */
    public void setGxmlWithSubIds(String p_gxml)
    {
        refreshContent(p_gxml);
    }

    //
    // TOPLINK SUPPORT METHODS
    //
    /**
     * Return the contents of the attribute that is mapped to the segment clob
     * field.
     */
    public String getSegmentClob()
    {
        return m_segmentClob;
    }

    //
    // other public methods
    //
    /**
     * Return a string representation of the object.
     * 
     * @return a string representation of the object.
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append(super.toString());
        sb.append(", (m_segmentString=");
        sb.append(m_segmentString);
        sb.append(", m_segmentClob=");
        sb.append(m_segmentClob);
        sb.append(", m_gxmlElement=");
        sb.append(m_gxmlElement);
        sb.append(", state=");
        sb.append(m_state);
        if (m_tu == null)
        {
            sb.append(", tu=null");
        }
        else
        {
            sb.append(", (tu type=");
            sb.append(m_tu.getTuType());
            sb.append(", tu id=");
            sb.append(m_tu.getId());
            sb.append(m_tu.getLocalizableType());
            sb.append(")");
        }
        sb.append(", locale=");
        sb.append(m_globalSightLocale);
        sb.append(", wordCount=");
        sb.append(m_wordCount);
        sb.append(", leverageMatchType=");
        sb.append(m_leverageMatchType);
        sb.append(")");
        return sb.toString();
    }

    //
    // package methods
    //

    /**
     * Set the word count of the segment text.
     * 
     * @param p_wordCount
     *            word count of the segment text.
     */
    public void setWordCount(int p_wordCount)
    {
        m_wordCount = p_wordCount;
    }

    /**
     * Set the GlobalSightLocale.
     * 
     * @param p_globalSightLocale
     *            GlobalSightLocale.
     */
    public void setGlobalSightLocale(GlobalSightLocale p_globalSightLocale)
    {
        m_globalSightLocale = p_globalSightLocale;
    }

    /**
     * Set the SourcePage.
     * 
     * @param p_sourcePage
     *            SourcePage.
     */
    public void setSourcePage(SourcePage p_sourcePage)
    {
        m_sourcePage = p_sourcePage;
    }

    public String setState(TuvState p_state)
    {
        m_state = p_state.getName();
        return m_state;
    }

    public TuvState getState()
    {
        try
        {
            return TuvState.valueOf(m_state);
        }
        catch (TuvException te)
        {
            c_category.error("getState " + m_state, te);
            return TuvState.UNSPECIFIED;
        }
    }

    public void setMergeState(String p_mergeState)
    {
        m_mergeState = p_mergeState;
    }

    public String getMergeState()
    {
        return m_mergeState;
    }

    //
    // private methods
    //

    /**
     * Sets all intermediate content related data members to null.
     */
    private void clearContent()
    {
        m_gxmlElement = null;
        m_subflowParents = null;
        m_subflows = null;
        m_leverageMatchType = LeverageMatchType.UNKNOWN_NAME;
        nullifyCacheFields();
    }

    private void refreshContent(String p_string)
    {
        clearContent();
        setSegment(p_string);
    }

    private void refreshContent(GxmlElement p_element)
    {
        clearContent();
        // setSegment(null);
        m_gxmlElement = p_element;

        // bring string representation in sync with element
        setSegment(p_element.toGxml());
    }

    /**
     * Set the sub element id attribute to a unique value within the tuv.
     * 
     * @return true when the gxml element has been modified, else false
     */
    private boolean setSubIdAttributes()
    {
        boolean result = false;

        // GxmlElement gxmlRootElement = getGxmlElement();
        List subflows = getSubflowsAsGxmlElements();

        // Get all the already set sub flow ID attribute values.
        // Don't use these values when setting un-set ID attribute
        // values.
        ArrayList setIdValues = new ArrayList(subflows.size());
        for (int i = 0; i < subflows.size(); i++)
        {
            GxmlElement sub = (GxmlElement) subflows.get(i);
            setIdValues.add(sub.getAttribute(GxmlNames.SUB_ID));
        }

        int idValue = 1;
        for (int i = 0; i < subflows.size(); i++, idValue++)
        {
            while (setIdValues.contains(Integer.toString(idValue)))
            {
                idValue++;
            }

            GxmlElement sub = (GxmlElement) subflows.get(i);

            if (sub.getAttribute(GxmlNames.SUB_ID) == null)
            {
                sub.setAttribute(GxmlNames.SUB_ID, Integer.toString(idValue));
                result = true;
            }
        }

        return result;
    }

    /**
     * This method is overwritten for TOPLink. TOPLink doesn't query all
     * collections of objects within an object. So if a tu is serialized - the
     * associated tuvs may not be available (because they haven't been queried
     * yet). Overwriting the method forces the query to happen so when it is
     * serialized all pieces of the object are serialized and availble to the
     * client.
     */
    protected void writeObject(ObjectOutputStream out) throws IOException
    {
        // touch tu - since it is set up to only populate when needed
        if (m_tu != null)
        {
            m_tu.getTuId();
        }

        // call the default writeObject
        out.defaultWriteObject();
    }

    /**
     * <p>
     * Whatever subflows are in the Collection will overwrite the current ones,
     * based on the id attribute. Current ones that are not in the Collection
     * remain the same.
     * </p>
     * 
     * <p>
     * Must replace parent subs in p_subs before its child subs. Otherwise the
     * parent sub will overwrite the new child sub.
     * </p>
     * 
     * @param p_subs
     *            GxmlElements of type SUB
     * @param p_gxmlElement
     *            GxmlElement to replace subflows in.
     * @return the p_gxmlElement that was passed in.
     */
    private static GxmlElement replaceSubs(Collection p_subs,
            GxmlElement p_gxmlElement)
    {
        if (p_subs == null || p_subs.isEmpty()) { return p_gxmlElement; }

        Collection orderedList = p_subs;
        if (p_subs.size() > 1)
        {
            // order list so parents come before children
            orderedList = GxmlElement.orderByParent(p_subs);
        }

        GxmlElement gxmlElement;
        GxmlElement sub;
        String id;

        for (Iterator it = orderedList.iterator(); it.hasNext();)
        {
            gxmlElement = (GxmlElement) it.next();
            id = gxmlElement.getAttribute(GxmlNames.SUB_ID);
            sub = p_gxmlElement.getDescendantByAttributeValue(GxmlNames.SUB_ID,
                    id, GxmlElement.SUB);

            if (sub == null)
            {
                // Throw unchecked exception so clients don't have to
                // catch rare case.
                RuntimeException re = new RuntimeException(
                        "Tuv.replaceSubs: can't find sub id "
                                + ((id == null || id.length() <= 0) ? "null"
                                        : id) + " in "
                                + p_gxmlElement.toString());
                c_category.error(re, re);
                throw re;
            }

            // Sometimes the editor only sends the sub with the ID
            // attribute, not the other attributes. Copy the old
            // attributes to the new sub for attributes not set in new
            // sub.
            GxmlElement.copyShallowUnsetAttributes(gxmlElement, sub,
                    GxmlElement.SUB);

            // replace the sub with the new one
            GxmlElement.replace(sub, gxmlElement);
        }

        return p_gxmlElement;
    }

    /**
     * Construct a subflow Gxml String including the tags using the content and
     * id attribute value.
     * 
     * @param p_subflowId
     *            the subflow id attribute.
     * @param p_subflowContent
     *            the subflow content Gxml String not including the sub tags.
     */
    private String getSubAsGxml(String p_id, String p_content)
    {
        return "<" + GxmlNames.SUB + " " + GxmlNames.SUB_ID + "=\"" + p_id
                + "\" >" + p_content + "</" + GxmlNames.SUB + ">";
    }

    // This method must be called whenever the value of the segment changes.
    private void nullifyCacheFields()
    {
        m_exactMatchFormat = null;
        m_fuzzyMatchFormat = null;
        m_gxmWithoutTagsFormat = null;
    }

    /*
     * Add the top level start tag and end tags to a Gxml string that is missing
     * them. @param a Gxml string that is missing the top level tags. @return a
     * Gxml string with top level tags.
     */
    private String addTopTags(String p_gxml)
    {
        String elementName = GxmlNames.SEGMENT;

        if (getTu().isLocalizable())
        {
            elementName = GxmlNames.LOCALIZABLE;
        }

        return "<" + elementName + " " + getGxmlElement().attributesToGxml()
                + ">" + p_gxml + "</" + elementName + ">";
    }

    /* Return whichever segment field is not null. */
    private String getSegment()
    {
        String s = m_segmentString;

        if (s == null)
        {
            s = m_segmentClob;
        }

        return s == null ? "" : s;
    }

    /* Set the value of one of the segment fields to the given value. */
    private void setSegment(String p_segment)
    {
        m_segmentClob = null;
        m_segmentString = null;

        if (p_segment != null)
        {
            if (EditUtil.getUTF8Len(p_segment) > CLOB_THRESHOLD)
            {
                m_segmentClob = p_segment;
            }
            else
            {
                m_segmentString = p_segment;
            }
        }
    }

    public boolean getIsIndexed()
    {
        return m_isIndexed;
    }

    public void setIsIndexed(boolean indexed)
    {
        m_isIndexed = indexed;
    }

    public void setState(String m_state)
    {
        this.m_state = m_state;
    }

    public void setSegmentClob(String clob)
    {
        m_segmentClob = clob;
    }

    public String getSegmentString()
    {
        return m_segmentString;
    }

    public void setSegmentString(String string)
    {
        m_segmentString = string;
    }

    public String getSid()
    {
        return sid;
    }

    public void setSid(String sid)
    {
        this.sid = sid;
    }
    
    /**
     * Gets referenced target page.
     * 
     * @return TargetPage
     */
    public TargetPage getTargetPage()
    {
        String sql = " select tp.* from TRANSLATION_UNIT_VARIANT tuv, "
                + "TRANSLATION_UNIT tu,TARGET_PAGE_LEVERAGE_GROUP tplg, "
                + "WORKFLOW w, TARGET_PAGE tp where tuv.id = ? "
                + "and tuv.TU_ID = tu.id "
                + "and tu.LEVERAGE_GROUP_ID = tplg.LG_ID "
                + "and tplg.TP_ID = tp.id "
                + "and tp.WORKFLOW_IFLOW_INSTANCE_ID = w.IFLOW_INSTANCE_ID "
                + "and tuv.STATE != 'OUT_OF_DATE' "
                + "and w.TARGET_LOCALE_ID = tuv.LOCALE_ID";

        List<TargetPage> tPages = HibernateUtil.searchWithSql(TargetPage.class,
                sql, getId());
        if (tPages.size() == 0)
        {
            return null;
        }

        return tPages.get(0);
    }
    
    /**
     * Gets referenced comment.
     * 
     * @return IssueImpl
     */
    public IssueImpl getComment()
    {
        String hql = "from IssueImpl i where "
                + "i.levelObjectTypeAsString = :type "
                + "&& i.levelObjectId = :oId";
        Map map = new HashMap();
        map.put("type", "S");
        map.put("oId", getId());

        IssueImpl issue = (IssueImpl) HibernateUtil.getFirst(hql, map);
        return issue;
    }
    
    public void addXliffAlt(XliffAlt alt)
    {
        if (alt == null)
            return;
        
        alt.setTuv(this);
        
        if (xliff_alt == null)
        {
            xliff_alt = new HashSet();
        }
        
        xliff_alt.add(alt);
    }
    
    public Set getXliffAlt() {
        return this.xliff_alt;
    }
    
    public void setXliffAlt(Set p_alt) {
        this.xliff_alt = p_alt;
    }
    
    public Set getIssueEditionRelation() {
        return this.issue_edition_relation;
    }
    
    public void setIssueEditionRelation(Set p_issue_edition) {
        this.issue_edition_relation = p_issue_edition;
    }
}
