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

package com.globalsight.everest.webapp.pagehandler.administration.comment;

import com.globalsight.util.GlobalSightLocale;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Needed to display Segment Comment Summary table in the UI.
 */
public class LocaleCommentsSummary
{
    private GlobalSightLocale   targetLocale;
    private List                pageSummaries;

    public LocaleCommentsSummary(GlobalSightLocale targetLocale)
    {
        this.targetLocale = targetLocale;
    }

    public void setTargetLocale(GlobalSightLocale targetLocale)
    {
        this.targetLocale = targetLocale;
    }

    public Locale getTargetLocaleName()
    {
        return targetLocale.getLocale();
    }

    public void setPageCommentsSummary(List pageSummaries)
    {
        this.pageSummaries = pageSummaries;
    }

    public GlobalSightLocale getTargetLocale()
    {
        return targetLocale;
    }

    public List getPageCommentsSummary()
    {
        return pageSummaries;
    }
}
