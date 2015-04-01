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

//
// globalsight imports
//
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.util.gxml.GxmlElement;

public final class TuImplVo extends PersistentObject implements Tu,
        Serializable
{
    private static final long serialVersionUID = -729123162750067474L;
    private static Logger logger = Logger.getLogger(TuImplVo.class);

    private long m_tmId;
    private String m_dataType = "unknown";
    private String m_tuType = TuType.UNSPECIFIED.getName();
    // Must be 'T' or 'L'. The 'U' stands for unknown.
    private char m_localizableType = 'U';
    // backpointer used for topLink
    private long m_leverageGroupId;
    private HashMap<Long, Tuv> m_tuvs = new HashMap<Long, Tuv>();

    private String m_sourceTmName = null;
    private String translate = null;

    /**
     * TU orders range from 1 - n. The default value is 0.
     */
    private long m_order = 0;

    /**
     * Paragraph ID of the current segment. After sentence segmentation,
     * multiple segments can belong to the same paragraph.
     */
    private long m_pid = 0;

    /**
     * Used by TopLink
     */
    public TuImplVo()
    {
    }

    //
    // Tu interface methods
    //

    /**
     * Get Tu unique identifier.
     */
    public long getTuId()
    {
        return getId();
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
     * Sets the paragraph ID (GXML's blockId).
     */
    public void setPid(long p_pid)
    {
        m_pid = p_pid;
    }

    /**
     * Retrieves the paragraph ID (GXML's blockId).
     */
    public long getPid()
    {
        return m_pid;
    }

    /**
     * Add a Tuv to a Tu. Also add this Tu to the Tuv as a back pointer.
     */
    public void addTuv(Tuv p_tuv)
    {
        m_tuvs.put(new Long(p_tuv.getLocaleId()), p_tuv);
        p_tuv.setTu(this);
    }

    /**
     * Get a tuv based on the specified locale id.
     */
    public Tuv getTuv(long p_localeId, long p_jobId)
    {
        return (Tuv) m_tuvs.get(new Long(p_localeId));
    }

    public Tuv getTuv(Long p_localeIdAsLong)
    {
        return (Tuv) m_tuvs.get(p_localeIdAsLong);
    }

    /**
     * Get a collection of all Tuvs. This is a combination of tuvs that belong
     * to a source page and target page(s).
     * 
     * @return A collection of Tuvs of this Tu.
     */
    public Collection getTuvs()
    {
        return m_tuvs.values();
    }

    public HashMap getTuvMap()
    {
        return m_tuvs;
    }

    /**
     * Get the Tu type.
     */
    public String getTuType()
    {
        return m_tuType;
    }

    /**
     * Get the data type.
     */
    public String getDataType()
    {
        return m_dataType;
    }

    /**
     * Get the Tm id.
     */
    public long getTmId()
    {
        return m_tmId;
    }

    /**
     * Get Leverage group id.
     */
    public long getLeverageGroupId()
    {
        return m_leverageGroupId;
    }

    /**
     * Set the leverage group that this tu belongs to. It sets only leverage
     * group id.
     */
    public void setLeverageGroup(LeverageGroup p_leverageGroup)
    {
        m_leverageGroupId = p_leverageGroup.getId();
    }

    public void setLeverageGroupId(long p_leverageGroupId)
    {
        m_leverageGroupId = p_leverageGroupId;
    }

    /**
     * Get the LocalizableType.
     */
    public char getLocalizableType()
    {
        return m_localizableType;
    }

    public boolean isLocalizable()
    {
        return m_localizableType == 'L' ? true : false;
    }

    /**
     * Get the LeverageGroup associated with this Tu. This method creates a new
     * LeverageGroup object, assuming that the object won't be used any more. By
     * doing this, we can eliminate a huge collection of Tu objects in
     * LeverageGroup object. We probably should eliminate LeverageGroup class
     * altogether.
     */
    public LeverageGroup getLeverageGroup()
    {
        LeverageGroupImpl levGrp = new LeverageGroupImpl();
        levGrp.setId(m_leverageGroupId);

        return levGrp;
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
        return super.toString() + " m_dataType=" + m_dataType + " m_tuType="
                + m_tuType + " m_pid=" + m_pid + " m_order=" + m_order;
    }

    //
    // package methods
    //

    /**
     * Set the TmId.
     */
    public void setTmId(long p_tmId)
    {
        m_tmId = p_tmId;
    }

    /**
     * Set the DataType.
     */
    public void setDataType(String p_dataType)
    {
        m_dataType = p_dataType;
    }

    /**
     * Set the LocalizableType. Must be 'L' or 'T'.
     */
    public void setLocalizableType(char p_localizableType)
    {
        m_localizableType = p_localizableType;
    }

    /**
     * Set the TuType. Don't expose this in Tu.
     * 
     * @param p_tuType
     *            the TuType.
     */
    public void setTuType(TuType p_tuType)
    {
        m_tuType = p_tuType.getName();
    }

    public void setTuTypeName(String p_tuType)
    {
        m_tuType = p_tuType;
    }

    public String getSourceTmName()
    {
        return m_sourceTmName;
    }

    public void setSourceTmName(String p_sourceTmName)
    {
        m_sourceTmName = p_sourceTmName;
    }

    public GxmlElement getXliffTargetGxml()
    {
        return null;
    }

    public String getXliffTarget()
    {
        return null;
    }

    public void setXliffTarget(String p_target)
    {
    }

    public String getXliffAlt()
    {
        return null;
    }

    public GxmlElement getXliffAltGxml()
    {
        return null;
    }

    public void setXliffAlt(String p_alt)
    {
    }

    public String getXliffTargetLanguage()
    {
        return null;
    }

    public void setXliffTargetLanguage(String p_lan)
    {
    }
    
    public String getXliffMrkId()
    {
        return null;
    }

    public void setXliffMrkId(String mid)
    {
    }
    
    public String getXliffMrkIndex()
    {
        return null;
    }

    public void setXliffMrkIndex(String mid)
    {
    }

    public String getGenerateFrom()
    {
        return null;
    }

    public void setGenerateFrom(String p_generator)
    {
    }

    public String getSourceContent()
    {
        return null;
    }

    public void setSourceContent(String p_sourceContent)
    {
    }

    public String getTranslate()
    {
        return translate;
    }

    public void setTranslate(String translate)
    {
        this.translate = translate;
    }
    
    public int getInddPageNum()
    {
        return -1;
    }

    public void setInddPageNum(int inddPageNum)
    {
        
    }
}
