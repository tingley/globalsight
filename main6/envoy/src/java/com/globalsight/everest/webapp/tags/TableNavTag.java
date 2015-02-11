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

package com.globalsight.everest.webapp.tags;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.javabean.NavigationBean;
import com.globalsight.everest.webapp.pagehandler.PageHandler;

public class TableNavTag extends TagSupport implements TableConstants
{

    private String bean; // Name of bean containing the list data. The bean
    // must be of type List and be in the request.
    private String key; // Unique key name used for hidden fields. Must be the
    // same name as the key used in the tableTag.
    private String pageUrl; // Same as given for tableTag
    private List data;
    private String otherUrl;

    /**
     * Set the name of the bean that contains the data. This is the name used in
     * useBean in the jsp.
     */
    public void setBean(String bean) throws JspException
    {
        this.bean = bean;

        // lookup the bean and set rows as the iterator
        data = (List) pageContext.getAttribute(bean, PageContext.REQUEST_SCOPE);
        if (data == null)
        {
            throw new AmbassadorTagException(pageContext,
                    "Cannot find list named: " + bean);
        }
    }

    /**
     * This is a unique name for the table to use for hidden fields. This must
     * be the same key that is used in the table tag.
     */
    public void setKey(String key)
    {
        this.key = key;
    }

    /**
     * This is the page url as defined in EnvoyConfig.xml. It must also be set
     * in a usebean in the jsp.
     */
    public void setPageUrl(String pageUrl)
    {
        NavigationBean nav = (NavigationBean) pageContext.getAttribute(pageUrl,
                PageContext.REQUEST_SCOPE);
        this.pageUrl = nav.getPageURL();
    }

    public String getPageUrl()
    {
        return pageUrl;
    }

    public String getBean()
    {
        return bean;
    }

    public String getKey()
    {
        return key;
    }

    public int doStartTag() throws JspException
    {
        int PAGES_EACH_SIDE = 3;

        JspWriter out = pageContext.getOut();
        SessionManager sessionMgr = (SessionManager) pageContext.getSession()
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        ResourceBundle bundle = PageHandler.getBundle(pageContext.getSession());
        HttpServletRequest request = (HttpServletRequest) pageContext
                .getRequest();
        if (data == null || data.size() == 0)
        {
            return SKIP_BODY;
        }

        try
        {
            // Get data needed for navigation
            int pageNum = ((Integer) request.getAttribute(key + PAGE_NUM))
                    .intValue();
            int numPages = ((Integer) request.getAttribute(key + NUM_PAGES))
                    .intValue();
            int total = ((Integer) request.getAttribute(key + LIST_SIZE))
                    .intValue();
            int perPage = ((Integer) request.getAttribute(key
                    + NUM_PER_PAGE_STR)).intValue();

            if (total <= perPage)
                return SKIP_BODY;

            int possibleTo = pageNum * perPage;
            int to = possibleTo > total ? total : possibleTo;
            int from = (to - data.size()) + 1;
            Integer sortChoice = (Integer) sessionMgr.getAttribute(key
                    + SORTING);

            Object[] args =
            { new Integer(from), new Integer(to), new Integer(total) };

            // Print "Displaying x to y of z"
            out.println(MessageFormat.format(bundle
                    .getString("lb_displaying_records"), args));

            out.println("<br>");
            out.println("&lt; ");

            // The "Previous" link
            if (pageNum == 1)
            {
                // Don't hyperlink "Previous" if it's the first page
                out.print(bundle.getString("lb_previous"));
            }
            else
            {
                int num = pageNum - 1;
                StringBuilder url = new StringBuilder();
                url.append("<a href=\"").append(pageUrl).append("&")
                        .append(key).append(PAGE_NUM).append("=").append(num)
                        .append("&").append(key).append(SORTING).append("=")
                        .append(sortChoice);
                if (otherUrl != null)
                {
                    url.append("&").append(otherUrl);
                }
                url.append("\">").append(bundle.getString("lb_previous"))
                        .append("</a>");
                out.println(url.toString());
            }
            out.print(" ");

            // Print out the paging numbers
            for (int i = 1; i <= numPages; i++)
            {
                if (((pageNum <= PAGES_EACH_SIDE) && (i <= PAGES_EACH_SIDE * 2))
                        || (((numPages - pageNum) <= PAGES_EACH_SIDE) && (i > (numPages - PAGES_EACH_SIDE * 2)))
                        || ((i <= (pageNum + PAGES_EACH_SIDE)) && (i >= (pageNum - PAGES_EACH_SIDE))))
                {
                    // Don't hyperlink the page you're on
                    if (i == pageNum)
                    {
                        out.print("<b>" + i + "</b>");
                    }
                    // Hyperlink the other pages
                    else
                    {
                        StringBuilder url = new StringBuilder();
                        url.append("<a class=standardHREF href=\"").append(pageUrl).append("&")
                                .append(key).append(PAGE_NUM).append("=").append(i)
                                .append("&").append(key).append(SORTING).append("=")
                                .append(sortChoice);
                        if (otherUrl != null)
                        {
                            url.append("&").append(otherUrl);
                        }
                        url.append("\">").append(i).append("</a>");
                        out.println(url.toString());
                    }
                    out.print(" ");
                }
            }
            // The "Next" link
            if (to >= total)
            {
                // Don't hyperlink "Next" if it's the last page
                out.print(bundle.getString("lb_next"));
            }
            else
            {
                int num = pageNum + 1;
                
                StringBuilder url = new StringBuilder();
                url.append("<a href=\"").append(pageUrl).append("&")
                        .append(key).append(PAGE_NUM).append("=").append(num)
                        .append("&").append(key).append(SORTING).append("=")
                        .append(sortChoice);
                if (otherUrl != null)
                {
                    url.append("&").append(otherUrl);
                }
                url.append("\">").append(bundle.getString("lb_next"))
                        .append("</a>");
                out.println(url.toString());
            }
            out.println(" &gt;");
        }
        catch (IOException e)
        {
            throw new AmbassadorTagException(pageContext, "I/O exception "
                    + e.getMessage());
        }
        return SKIP_BODY;
    }

    public void release()
    {
        bean = null;
        key = null;
        pageUrl = null;
    }

    public String getOtherUrl()
    {
        return otherUrl;
    }

    public void setOtherUrl(String otherUrl)
    {
        this.otherUrl = otherUrl;
    }
}
