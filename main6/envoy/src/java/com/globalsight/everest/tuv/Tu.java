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

import com.globalsight.util.gxml.GxmlElement;

/**
 * The Tu interface represents a translation unit. A translation unit
 * encapsulates all the translation unit variants for a given segment of text.
 */
public interface Tu /** extends TuLing */
{

    /**
     * Get Tu unique identifier.
     * 
     * @return Tu unique identifier.
     */
    public long getTuId();

    /**
     * Get Tu unique identifier.
     * 
     * @return Tu unique identifier.
     */
    public long getId();

    /**
     * Return the persistent object's id as a Long object.
     * <p>
     * 
     * <p>
     * This is a convenience method that simply wraps the id as an object, so
     * that, for example, the idAsLong can be used as a Hashtable key.
     * 
     * @return the unique identifier as a Long object.
     */
    public Long getIdAsLong();

    /**
     * Set the order of this Tuv - with regards to the page.
     */
    public void setOrder(long p_order);

    public long getOrder();

    /**
     * Retrieves the paragraph ID.
     */
    public long getPid();

    /**
     * Add a Tuv to a Tu.
     * 
     * @param p_tuv
     *            the Tuv to add.
     */
    public void addTuv(Tuv p_tuv);

    /**
     * Get a tuv based on the specified locale id.
     * 
     * @param p_localeId
     *            - The locale id used for getting a tuv.
     * @return A Tuv based on a locale id.
     */
    public Tuv getTuv(long p_localeId, long jobId);

    /**
     * Get a collection of all Tuvs. This is a combination of tuvs that belong
     * to a source page and target page(s).
     * 
     * @return A collection of Tuvs of this Tu.
     */
    // public Collection getTuvs();

    /**
     * Get the Tu type.
     * 
     * @return The tu type.
     */
    public String getTuType();

    /**
     * Get the data type.
     * 
     * @return Tu's data type.
     */
    public String getDataType();

    /**
     * Get the Tm id.
     * 
     * @return The id of this Tu's tm.
     */
    public long getTmId();

    /**
     * Set the leverage group that this tu belongs to.
     * 
     * @param p_leverageGroup
     *            - The leverage group to be set.
     */
    public void setLeverageGroup(LeverageGroup p_leverageGroup);

    /**
     * Get the LeverageGroup associated with this Tu.
     * 
     * @returns the LeverageGroup associated with this Tu.
     */
    public LeverageGroup getLeverageGroup();

    public void setLeverageGroupId(long p_leverageGroupId);

    public long getLeverageGroupId();

    /**
     * Get the LocalizableType.
     * 
     * @return The LocalizableType.
     */
    public char getLocalizableType();

    public boolean isLocalizable();

    public String getSourceTmName();

    public void setSourceTmName(String p_sourceTmName);

    public GxmlElement getXliffTargetGxml();

    public String getXliffTarget();

    public void setXliffTarget(String p_target);

    public String getXliffTargetLanguage();

    public void setXliffTargetLanguage(String p_lan);

    public String getGenerateFrom();

    public void setGenerateFrom(String p_generator);

    public String getSourceContent();

    public void setSourceContent(String p_sourceContent);

    public String getTranslate();

    public void setTranslate(String translate);
    
    public void setXliffMrkId(String id);
    
    public String getXliffMrkId();
    
    public void setXliffMrkIndex(String index);
    
    public String getXliffMrkIndex();

    public int getInddPageNum();

    public void setInddPageNum(int inddPageNum);
}
