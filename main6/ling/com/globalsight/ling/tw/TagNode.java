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
package com.globalsight.ling.tw;

import java.util.Hashtable;

/**
 * Primarily a data class that contains all the attributes of a single tmx tag.
 * This data is used by the error checker for both source and target items.
 */
public class TagNode
{
    private boolean m_bMapped;
    private int m_nSourceListIndex;
    private String m_strTmxName;
    private String m_strPTagName;
    private boolean m_bIsPaired;
    private Hashtable m_hAttributes;
    // GBS-3722
    private boolean m_isMTIdentifier = false;
    public static final String INTERNAL = "internal";

    /**
     * constructor.
     */
    public TagNode(String p_strTmxTagName, String p_strPTagName,
            Hashtable p_hAttributes) throws TagNodeException
    {
        super();

        if (p_strTmxTagName == null || p_strTmxTagName.length() == 0
                || p_strPTagName == null || p_hAttributes == null)
        {
            throw new TagNodeException("Invalid Tagnode");
        }

        if (!INTERNAL.equals(p_strTmxTagName) && p_strPTagName.length() == 0)
        {
            throw new TagNodeException("Invalid Tagnode");
        }

        m_bMapped = false;
        m_nSourceListIndex = -1;
        m_strTmxName = p_strTmxTagName;
        m_strPTagName = p_strPTagName;
        m_hAttributes = p_hAttributes;

        if (p_strTmxTagName.equals("bpt") || p_strTmxTagName.equals("ept"))
        {
            m_bIsPaired = true;
        }
        else
        {
            m_bIsPaired = false;
        }
    }

    /**
     * Sets the mapped indicator.
     * 
     * @param p_State
     *            - true to set as mapped. False to set as unmapped.
     */
    public void setMapped(boolean p_State)
    {
        m_bMapped = p_State;
    }

    /**
     * Records the index of this node in the source list. Source nodes are also
     * referenced by the target list. If you are in the target list you can use
     * this index to know the nodes position in the source list.
     * 
     * @param p_Idx
     *            - the source list index where this node can be found.
     */
    public void setSourceListIndex(int p_Idx)
    {
        m_nSourceListIndex = p_Idx;
    }

    /**
     * Sets the literal TMX name for this node.
     * 
     * @param p_Name
     *            - the TMX type name.
     */
    public void setTmxType(String p_Name)
    {
        m_strTmxName = p_Name;
    }

    /**
     * Set the PTag name for this node.
     * 
     * @param p_Name
     *            - the PTag name as a string.
     */
    public void setPTagName(String p_Name)
    {
        m_strPTagName = p_Name;
    }

    /**
     * Sets the tags attributes for this node.
     * 
     * @param p_hAttr
     *            - the attrbute hash.
     */
    public void setAttributes(Hashtable p_hAttr)
    {
        m_hAttributes = p_hAttr;
    }

    /**
     * Sets whether the node is a paired type.
     * 
     * @param p_State
     *            - true to indicate paired. Otherwise false.
     */
    public void setPaired(boolean p_State)
    {
        m_bIsPaired = p_State;
    }

    /**
     * Indicates whether this node has been mapped.
     * 
     * @return true if node is mapped. Otherwise false.
     */
    public boolean isMapped()
    {
        return m_bMapped;
    }

    public boolean isMTIdentifier()
    {
        return m_isMTIdentifier;
    }

    public void setMTIdentifier(boolean p_isMTIdentifier)
    {
        m_isMTIdentifier = p_isMTIdentifier;
    }

    /**
     * Gets the original index of this node in regards to the source list.
     * Source nodes are also referenced by the target list. If you are in the
     * target list you can use this index to know the nodes position in the
     * source list.
     * 
     * @return the index as an integer.
     */
    public int getSourceListIndex()
    {
        return m_nSourceListIndex;
    }

    /**
     * Gets the literal TMX name for this node.
     * 
     * @return the TMX type name as a string.
     */
    public String getTmxType()
    {
        return m_strTmxName;
    }

    /**
     * Gets the PTag name for this node.
     * 
     * @return the PTag name as a string.
     */
    public String getPTagName()
    {
        return m_strPTagName;
    }

    /**
     * Indicates whether the node is a paired.
     * 
     * @return true if node is paired. Otherwise false.
     */
    public boolean isPaired()
    {
        return m_bIsPaired;
    }

    /**
     * Gets the tags attributes for this node.
     * 
     * @return the attribute hash.
     */
    public Hashtable getAttributes()
    {
        return m_hAttributes;
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer();

        sb.append("m_bMapped=");
        sb.append(m_bMapped);
        sb.append(", m_nSourceListIndex=");
        sb.append(m_nSourceListIndex);
        sb.append(", m_strTmxName=");
        sb.append(m_strTmxName);
        sb.append(", m_strPTagName=");
        sb.append(m_strPTagName);
        sb.append(", m_bIsPaired=");
        sb.append(m_bIsPaired);
        sb.append(", m_hAttributes=");
        sb.append(m_hAttributes);
        return sb.toString();
    }
}
