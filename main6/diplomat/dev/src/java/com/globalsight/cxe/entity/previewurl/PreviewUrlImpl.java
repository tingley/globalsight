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
package com.globalsight.cxe.entity.previewurl;
/*
 * Copyright (c) 2001 GlobalSight Corporation. All rights reserved.
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

import com.globalsight.everest.persistence.PersistentObject;

/**
 * Implemenation of PreviewUrl interface
 */
public class PreviewUrlImpl extends PersistentObject
    implements PreviewUrl
{
    private static final long serialVersionUID = -7455363888812491716L;
    
    //  Private Members
    private String  m_name;
    private String  m_description;
    private String  m_urlXml;

    /**
     *  Default constructor used by TopLink Only
     */
    public PreviewUrlImpl ()
    {
        super();

        m_name = null;
        m_description = null;
        m_urlXml = null;
    }

    /**
     * Constructor that supplies all attributes for PreviewUrl object.
     *
     * @param p_name Name of preview URL
     * @param p_description Description of preview URL
     * @param p_urlXml Content of preview URL
     */
    public PreviewUrlImpl (String p_name, String p_description, String p_urlXml)
    {
        super();

        m_name = p_name;
        m_description = p_description;
        m_urlXml = p_urlXml;
    }

    /**
     * Constructs an PreviewUrlImpl object from a PreviewUrl object
     *
     * @param p_previewUrl Another PreviewUrl object
     **/
    public PreviewUrlImpl(PreviewUrl p_previewUrl)
    {
        super();

        m_name = p_previewUrl.getName();
        m_description = p_previewUrl.getDescription();
        m_urlXml = p_previewUrl.getContent();
    }

    /**
     * Return the description of the preview URL
     *
     * @return preview URL description
     */
    public String getDescription ()
    {
        return m_description;
    }

    /**
     * Sets the description of the preview URL
     *
     * @param p_description Preview URL description
     */
    public void setDescription (String p_description)
    {
        m_description = p_description;
    }

    /**
     * Return the content of the preview URL
     *
     * @return preview URL content
     */
    public String getContent ()
    {
        return m_urlXml;
    }

    /**
     * Sets preview URL content
     *
     * @param p_urlXml Preview URL XML
     */
    public void setContent (String p_urlXml)
    {
        m_urlXml = p_urlXml;
    }

    /**
     * Return the name of the preview URL
     *
     * @return URL name
     */
    public String getName ()
    {
        return m_name;
    }

    /**
     * Sets the name of the preview URL
     *
     * @param p_name Preview URL name
     */
    public void setName (String p_name)
    {
        m_name = p_name;
    }

    /**
     * Return string representation of object
     *
     * @return string representation of object
     */
    public String toString()
    {
        return m_name;
    }

    public String getUrlXml()
    {
        return m_urlXml;
    }

    public void setUrlXml(String xml)
    {
        m_urlXml = xml;
    }
}

