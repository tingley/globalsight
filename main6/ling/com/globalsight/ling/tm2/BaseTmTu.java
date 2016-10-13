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

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.globalsight.util.GlobalSightLocale;

/**
 * BaseTmTu is an interface that defines interfaces of various Translation Unit
 * representations. Translation Unit usually has one source Translation Unit
 * Variant and one or more target Translation Unit Variants. Translation Unit
 * Variant has text in a given locale. The term Translation Unit and Translation
 * Unit Variant are taken from TMX (http://www.lisa.org/tmx) specification.
 */

public interface BaseTmTu
{
    public long getId();

    public void setId(long p_id);

    public long getTmId();

    public void setTmId(long p_tmId);

    /**
     * returns its format
     * 
     * @return name of the format
     */
    public String getFormat();

    /**
     * returns its type
     * 
     * @return name of the type
     */
    public String getType();

    /**
     * test if it's translatable
     * 
     * @return true if translatable, false if localizable
     */
    public boolean isTranslatable();

    /**
     * sets its format
     * 
     * @param p_format
     *            name of the format
     */
    public void setFormat(String p_format);

    /**
     * sets its type
     * 
     * @param p_type
     *            name of the type
     */
    public void setType(String p_type);

    /**
     * sets the TU as translatable
     */
    public void setTranslatable();

    /**
     * sets the TU as localizable
     */
    public void setLocalizable();

    /**
     * adds a Tuv to this Tu
     * 
     * @param p_tuv
     *            Tuv to add
     */
    public void addTuv(BaseTmTuv p_tuv);

    /**
     * removes a Tuv from this Tu
     * 
     * @param p_tuv
     *            Tuv to be removed
     */
    public void removeTuv(BaseTmTuv p_tuv);
    
    public void removeTuvsForLocale(GlobalSightLocale locale);

    /**
     * Get a first Tuv in a Tuv array in a given locale.
     * 
     * @param p_locale
     *            GlobalSightLocale
     * @return BaseTmTuv. If a Tuv in a given locale cannot be found, null is
     *         returned.
     */
    public BaseTmTuv getFirstTuv(GlobalSightLocale p_locale);

    /**
     * Get a Collection of Tuv in a given locale.
     * 
     * @param p_locale
     *            GlobalSightLocale
     * @return Collection object that contains one or more BaseTmTuv objects. If
     *         a Collection in a given locale cannot be found, null is returned.
     */
    public Collection<BaseTmTuv> getTuvList(GlobalSightLocale p_locale);

    /**
     * Get all locales of Tuvs that are added to this Tu.
     * 
     * @return a Set of GlobalSightLocale.
     */
    public Set<GlobalSightLocale> getAllTuvLocales();

    /**
     * Get a number of Tuvs this Tu has.
     * 
     * @return Number of Tuvs
     */
    public int getTuvSize();

    public String getSourceTmName();

    public void setSourceTmName(String p_sourceTmName);

    public Object clone();

    public String toDebugString(boolean p_includeTuvs);

    public boolean isFromWorldServer();
    
    public void setFromWorldServer(boolean fromWorldServer);
    
    public List<BaseTmTuv> getTuvs();
    
    public String getSourceContent();

    public void setSourceContent(String sourceContent);
}
