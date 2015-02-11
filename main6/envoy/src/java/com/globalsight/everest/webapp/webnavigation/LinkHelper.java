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

package com.globalsight.everest.webapp.webnavigation;

import org.apache.log4j.Logger;

import com.globalsight.everest.webapp.javabean.NavigationBean;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.util.system.ConfigException;
import com.globalsight.util.GeneralException;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;

/**
 * Helps create URLs for JSPs.
 */
public class LinkHelper
{

    // Constants
    static public final String ACTIVITY_NAME = "activityName";
    static public final String LINK_NAME = "linkName";
    static public final String MAIN_URL = "/globalsight/ControlServlet?";
    static public final String PAGE_NAME = "pageName";
    static public final String HTTP_REQUEST_VALUE_EQUAL = "=";
    static public final String HTTP_REQUEST_VALUE_DELIMITER = "&";

    static private boolean m_SSL = false;
    static private String m_port = null;
    static private String m_protocol = null;

    // Category for log4j logging.
    private static final Logger CATEGORY =
        Logger.getLogger(
            LinkHelper.class.getName());


    /**
     * Constructor
     *
     */
    private LinkHelper()
    {
    }

    /**
     * Creates a URL for a link that leads to a page.
     * @param navigationBean The navigation bean that that this class
     * will be building URLs for.
     * @return A String containing the URL.
     */
    public static String getPageURL(NavigationBean p_navigationBean)
    {
        String linkName = p_navigationBean.getLinkName();
        String pageName = p_navigationBean.getPageName();
        StringBuffer buff = new StringBuffer(linkName.length()
            + pageName.length() + MAIN_URL.length()
            + LINK_NAME.length() + PAGE_NAME.length()
            + (2 * HTTP_REQUEST_VALUE_EQUAL.length())
            + HTTP_REQUEST_VALUE_EQUAL.length());
        // Concatenate the String elements together.
        buff.append(MAIN_URL);
        buff.append(LINK_NAME);
        buff.append(HTTP_REQUEST_VALUE_EQUAL);
        buff.append(linkName);
        buff.append(HTTP_REQUEST_VALUE_DELIMITER);
        buff.append(PAGE_NAME);
        buff.append(HTTP_REQUEST_VALUE_EQUAL);
        buff.append(pageName);
        return buff.toString();
    }

    public static String getWebActivityURL(HttpServletRequest p_request,
        String p_activityName)
    {
        return MAIN_URL + ACTIVITY_NAME + "=" + p_activityName;
    }

    public static String getSystemHomeURL(HttpServletRequest p_request)
    {
        return MAIN_URL;
    }
}
