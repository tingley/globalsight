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

package com.globalsight.everest.edit.online;

import java.io.Serializable;

/**
 * <p>A data class for encapsulating page information for display in
 * the editor's Page Details dialog.</p>
 */
public class PageInfo
    implements Serializable
{
    private String str_pageName;
    private String str_pageFormat;
    private long i_wordCount;
    private int i_segmentCount;
    private String str_dataSourceType;
    private String str_externalBaseHref;

    /**
     * <P>Constructs an empty, uninitialized object.</P>
     */
    public PageInfo ()
    {
        str_pageName = "";
        str_pageFormat = "";
        str_dataSourceType = "";
        str_externalBaseHref = "";
        i_wordCount = 0;
        i_segmentCount = 0;
    }

    public PageInfo (String p_pageName, String p_pageFormat,
        String p_dataSourceType, String p_externalBaseHref,
        long p_wordCount, int p_segmentCount)
    {
        str_pageName = p_pageName;
        str_pageFormat = p_pageFormat;
        str_dataSourceType = p_dataSourceType;
        str_externalBaseHref = p_externalBaseHref;
        i_wordCount = p_wordCount;
        i_segmentCount = p_segmentCount;
    }

    public void setPageName(String p_pageName)
    {
        str_pageName = p_pageName;
    }

    public String getPageName()
    {
        return str_pageName;
    }

    public void setPageFormat(String p_pageFormat)
    {
        str_pageFormat = p_pageFormat;
    }

    public String getPageFormat()
    {
        return str_pageFormat;
    }

    public void setDataSourceType(String p_param)
    {
        str_dataSourceType = p_param;
    }

    public String getDataSourceType()
    {
        return str_dataSourceType;
    }

    public void setExternalBaseHref(String p_param)
    {
        str_externalBaseHref = p_param;
    }

    public String getExternalBaseHref()
    {
        return str_externalBaseHref;
    }

    public void setWordCount(long p_wordCount)
    {
        i_wordCount = p_wordCount;
    }

    public long getWordCount()
    {
        return i_wordCount;
    }

    public void setSegmentCount(int p_segmentCount)
    {
        i_segmentCount = p_segmentCount;
    }

    public int getSegmentCount()
    {
        return i_segmentCount;
    }

}
