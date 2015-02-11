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
package com.globalsight.everest.webapp.javabean;

/**
 * This bean class is used to pass a link URL info to a JSP.
 *
 * @version     1.0
 * @author      Ricardo Cisternas
 */

/*
 * MODIFIED     MM/DD/YYYY
 * Ricardo      10/2000     Initial version.
 * bwang        11/6/00     add pageURL property
 */

import com.globalsight.everest.webapp.webnavigation.LinkHelper;

public class NavigationBean
{
    private String linkName = null;
    private String pageName = null;
    private String pageURL=null;

    public NavigationBean()
    {
        linkName = "";
        pageName = "";
    }

    public NavigationBean(String linkName, String pageName)
    {
        this.linkName = linkName;
        this.pageName = pageName;
    }

    public void setLinkName(String theLinkName)
    {
        this.linkName = theLinkName;
    }

    public String getLinkName()
    {
        return linkName;
    }

    public void setPageName(String thePageName)
    {
        this.pageName = thePageName;
    }

    public String getPageName()
    {
        return pageName;
    }

    public String getPageURL()
    {
        return LinkHelper.getPageURL(this);
    }

}
