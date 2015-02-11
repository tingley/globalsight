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
package com.globalsight.everest.customform;

//java
import java.util.Date;

// globalsight
import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.util.GlobalSightLocale;


/**
 * This class represents a custom design form which contains the
 * structure in XML format.
 */           
public class CustomForm extends PersistentObject
{
    private static final long serialVersionUID = -175637186495309227L;
    private String m_modifierUserId = null;
    private Date m_modifiedDate = null;
    private String m_xmlDesign = null;
    private GlobalSightLocale m_locale = null;


    //////////////////////////////////////////////////////////////////////
    //  Begin: Constructor
    //////////////////////////////////////////////////////////////////////
    /**    
    */
    public CustomForm()
    {        
        super();
    }
    //////////////////////////////////////////////////////////////////////
    //  End: Constructor
    //////////////////////////////////////////////////////////////////////

    
    //////////////////////////////////////////////////////////////////////
    //  Begin: Helper Methods
    //////////////////////////////////////////////////////////////////////
    /**
     * Get the name of the custom form.
     * @return The custom form name.
     */
    public String getPageName()
    {
        return getName();
    }

    /**
     * Get the user id of the last individual who has modified
     * this custom form.
     * @return The modifier user id.
     */
    public String getModifierUserId()
    {
        return m_modifierUserId;
    }

    /**
     * Get the modification date of the form.
     * @return The date which the form was last updated.
     */
    public Date getModifiedDate()
    {
        return m_modifiedDate;
    }

    /**
     * Get the designed custom form in XML format.
     * @return The XML representation of the form.
     */
    public String getXmlDesign()
    {
        return m_xmlDesign;
    }

    /**
     * Get the locale in which the labels of this form where saved.
     * @return The locale for the saved strings.
     */
    public GlobalSightLocale getLocale()
    {
        return m_locale;
    }

    public void setLocale(GlobalSightLocale p_locale)
    {
        m_locale = p_locale;
    }

    /**
     * Update the initial values of the custom form.
     * @param p_pageName - The name of the form.
     * @param p_modifierUserId - The user id of the form creator/modifier.
     * @param p_xmlDesign - The form design in XML format.
     */
    public void update(String p_pageName, 
                       GlobalSightLocale p_locale,
                       String p_modifierUserId, 
                       String p_xmlDesign)
    {
        setName(p_pageName);
        m_locale = p_locale;
        m_modifierUserId = p_modifierUserId;
        m_xmlDesign = p_xmlDesign;
        m_modifiedDate = new Date();
    }
    //////////////////////////////////////////////////////////////////////
    //  End: Helper Methods
    //////////////////////////////////////////////////////////////////////

    public void setModifierUserId(String userId)
    {
        m_modifierUserId = userId;
    }

    public void setModifiedDate(Date date)
    {
        m_modifiedDate = date;
    }

    public void setXmlDesign(String design)
    {
        m_xmlDesign = design;
    }
 }
