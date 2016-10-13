/*
 * Copyright (c) 2000 GlobalSight Corporation. All rights reserved.
 *
 * THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF
 * GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
 * IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
 * OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
 * AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.
 *
 * THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
 * SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
 * UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
 * BY LAW.
 */

package com.globalsight.migration.system3;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

/**
 * This class is responsible for retrieving data from LANGS table.
 */
public class System3Segment
{
    private String m_dataType = null;
    private String m_itemType = null;
    private boolean m_isTranslatable;
    private Map m_texts = new HashMap();
    

    public String getDataType()
    {
        return m_dataType;
    }
    
    public String getItemType()
    {
        return m_itemType;
    }
    
    public boolean isTranslatable()
    {
        return m_isTranslatable;
    }
    
    public void setDataType(String p_dataType)
    {
        m_dataType = p_dataType;
    }
    
    public void setItemType(String p_itemType)
    {
        m_itemType = p_itemType;
    }
    
    public void setTranslatable(boolean p_translatable)
    {
        m_isTranslatable = p_translatable;
    }
    
        
    public void addText(String p_locale, String p_text)
    {
        m_texts.put(p_locale, p_text);
    }
    

    /**
     * returns Iterator that iterates Map.Entry that contains a locale
     * name as a key and a text as a value.
     * @return Iterator
     */
    public Iterator iterator()
    {
        Set entrySet = m_texts.entrySet();
        return entrySet.iterator();
    }
}
