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

/**
 * Preview URL Class Definition
 */
public interface PreviewUrl
{
    /**
     * Return the id of preview URL.
     *
     * <p>Note: The id is set by TopLink persistence.
     *
     * @return id as a long
     */
    public long getId();

    /**
     * Return the name of the preview URL
     *
     * @return URL name
     */
    public String getName();

    /**
     * Return the description of the preview URL
     *
     * @return preview URL description
     */
    public String getDescription();

    /**
     * Return the content of the preview URL
     *
     * @return preview URL content
     */
    public String getContent();

    /**
      * Sets the name of the preview URL
      *
      * @param p_name Preview URL name
      */
    public void setName(String p_name);

    /**
     * Sets the description of the preview URL
     *
     * @param p_description Preview URL description
     */
    public void setDescription(String p_description);

    /**
     * Sets preview URL content
     *
     * @param p_urlXml Preview URL XML
     */
    public void setContent(String p_urlXml);
}
