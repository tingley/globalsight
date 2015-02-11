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

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;
import javax.servlet.http.HttpSession;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.TagSupport;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.Vector;

import com.globalsight.everest.webapp.javabean.SkinBean;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.javabean.NavigationBean;

/**
 * This writes a table tag.  It uses styles to conform to an
 * "ambassodor" table look and feel.
 *
 * This iterates over a table's body contents.  The first time
 * it writes the table header.  Then it iterates writing a
 * row in the table.
 */
public class TableTag extends BodyTagSupport
{

    protected String        bean;           // bean name
    protected String        id;             // for scripting variable
    protected String        key;            // unique key on page for hidden fields
    protected String        emptyTableMsg;  // message if empty table
    protected String        pageUrl;        // url to call for sort, navigating, etc
    protected String        filterMethod;   // used for filtering out a row
    protected Object        filterData;     // used for filtering out a row
    protected String        filterSelection;  // used so selected filter can
                                              // be passed on the url (because of sorting)
    protected List          data;           // contains data for the table
    protected Class         elementClass;
    protected int           index;
    protected boolean       showHeader;     // true if writing table header
    protected boolean       skip;           // true if skipping a row (because of filter)
    private String 	    	width=null;
    private boolean         evenRow;
    private String          otherUrl = null;

    private String          taskListStart;
    private boolean         hasFilter;

    public String getTaskListStart()
    {
        return taskListStart;
    }

    public void setTaskListStart(String taskListStart)
    {
        this.taskListStart = taskListStart;
    }

    public TableTag()
    {
        init();
    }

    /**
     * Set the name of the bean that contains the data.  This must be defined
     * in a useBean in the jsp.
     *
     * @exception JspException if the bean cannot be found
     */
    public void setBean(String bean) throws JspException
    {
        this.bean = bean;

        // lookup the bean and set rows as the iterator
        data = (List)
             pageContext.getAttribute(bean, PageContext.REQUEST_SCOPE);
        if (data == null)
        {
             throw new AmbassadorTagException(pageContext, "Cannot find list named: " + bean);
        }
        showHeader = true;
    }

    public String getBean()
    {
        return bean;
    }

    /**
     * This is the class name of the list data type.
     */
    public void setDataClass(String className) throws ClassNotFoundException
    {
        setElementClass(Class.forName(className));
    }


    /**
     * This is the id of the scripting variable to be used to access data in the list.
     */
    public void setId(String id)
    {
        this.id = id;
    }

    public String getId()
    {
        return id;
    }

    /**
     * This is a unique key needed for hidden html elements.
     * This must be the same as the key set for the TableNavTag, if used.
     */
    public void setKey(String key)
    {
        this.key = key;
    }

    public String getKey()
    {
        return key;
    }

    /**
     * If you want to hide rows that don't match some value in the data, you can
     * do that by specifying a method and it's value.  For instance, say
     * you don't want rows printed which don't have a target local of French.
     * Here you specify the method name in the data which gets the target locale.
     * Then you also need to set filterData in order to know which data is valid.
     *
     * @param method name of method in bean
     */
    public void setFilterMethod(String filterMethod)
    {
        this.filterMethod = filterMethod;
    }
    public String getFilterMethod()
    {
        return filterMethod;
    }
    public void setFilterData(Object filterData)
    {
        this.filterData = filterData;
    }
    public Object getFilterData()
    {
        return filterData;
    }
    public void setFilterSelection(String filterSelection)
    {
        this.filterSelection = filterSelection;
    }
    public String getFilterSelection()
    {
        return filterSelection;
    }

    /**
     * This is a message to be displayed if the table is empty.  It must
     * defined in the properties file.
     */
    public void setEmptyTableMsg(String emptyTableMsg)
    {
        this.emptyTableMsg = emptyTableMsg;
    }

    public String getEmptyTableMsg()
    {
        return emptyTableMsg;
    }

    /**
     * This is the page url as defined in EnvoyConfig.xml.  It must also
     * be set in a usebean in the jsp.
     */
    public void setPageUrl(String pageUrl)
    {
        NavigationBean nav = (NavigationBean)
             pageContext.getAttribute(pageUrl, PageContext.REQUEST_SCOPE);
        this.pageUrl = nav.getPageURL();
    }

    public String getPageUrl()
    {
        if (this.otherUrl != null)
        {
            return pageUrl + "&" + otherUrl;
        }

        return pageUrl;
    }

    /**
     * Set the width of the table.  Default is 100%.
     */
    public void setWidth(String width)
    {
        this.width = width;
    }

    public String getWidth()
    {
        return width;
    }

    public List getData()
    {
        return data;
    }

    public boolean showHeader()
    {
        return showHeader;
    }

    public boolean skip()
    {
        return skip;
    }

    public void setElementClass(Class elementClass)
    {
        this.elementClass = elementClass;
    }

    public Class getElementClass()
    {
        return elementClass;
    }

    /**
     * Write out javascript needed for sorting columns.  Write the opening
     * html table tag.  Write the first html row tag.
     *
     * @exception JspTagException is any writes fail.
     */
    public int doStartTag() throws JspException
    {
        JspWriter out = pageContext.getOut();

        try
        {
            //get the start at parameter and initialize the data set
            HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();

            if (data.size() == 0) {
                if (!showHeader) {
                    return SKIP_BODY;
                }
            }
            index = 0;
            if (!showHeader) pageContext.setAttribute(id, data.get(index));

            // Write html table tag.
            if (width==null)
            {
            	out.println("<table cellspacing=\"0\" cellpadding=\"1\" border=\"0\" class=\"listborder\" width=\"100%\">");
			}
            else
            {
				out.println("<table cellspacing=\"0\" cellpadding=\"1\" border=\"0\" class=\"listborder\" width=\"" +getWidth() + ">");
			}

            out.println("  <tr class=\"tableHeadingBasicTM\">");

            return EVAL_BODY_AGAIN;
        }
        catch (IOException e)
        {
            throw new AmbassadorTagException(pageContext, "I/O exception " + e.getMessage());
        }
    }

    /**
     * This method is called for each row of data.
     *
     * @exception JspException if there is an IO error.
     */
    public int doAfterBody() throws JspException
    {
        JspWriter    out = pageContext.getOut();
        BodyContent body = getBodyContent();

        try
        {
            if (!showHeader)
            {
                if (data.size() == 0)
                {
                    return SKIP_BODY;
                }
                index++;
            }
            if (showHeader && isHasFilter()) {
                out.print("</tr><tr class='tableHeadingFilter'>");
                String filterContent = (String) pageContext.getAttribute("filterContent");
                out.print(filterContent);
            }
            showHeader = false;
            if (skip == false)
                out.println("  </tr>");
            body.writeOut(getPreviousOut());
        }
        catch (IOException e)
        {
            throw new JspTagException("Table Tag: " + e.getMessage());
        }

        // clear up so the next time the body content is empty
        body.clearBody();


        try
        {
            skip = false;
            if (index < data.size())
            {
                // Check if filtering out rows
                if (filterMethod != null)
                {
                    try {
                        Method method = elementClass.getMethod(filterMethod, null);
                        Object obj = method.invoke(data.get(index), null);
                        if (!obj.equals(filterData))
                        {
                            //index++;
                            skip = true;
                            pageContext.setAttribute(id, data.get(index));
                            return EVAL_BODY_AGAIN;
                        }
                    }
                    catch (NoSuchMethodException e)
                    {
                    }
                    catch (IllegalAccessException e1)
                    {
                    }
                    catch (InvocationTargetException e2)
                    {
                    }
                }

                if (evenRow)
                {
                    out.println("  <tr class=tableRowEvenTM vAlign=top>");
                    evenRow = false;
                }
                else
                {
                    out.println("  <tr class=tableRowOddTM vAlign=top>");
                    evenRow = true;
                }
                pageContext.setAttribute(id, data.get(index));
                return EVAL_BODY_AGAIN;
            }
            else
            {
                // Finally done.  Do not iterate over the body again.
                return SKIP_BODY;
            }
        }
        catch (IOException e)
        {
            throw new AmbassadorTagException(pageContext,
                             "I/O exception " + e.getMessage());
        }
    }

    /**
     * Write ending html table tag.  Write hidden fields needed for column
     * sorting and table size, etc.
     */
    public int doEndTag() throws JspException
    {
        JspWriter    out = pageContext.getOut();
        try
        {
            if (data.size() == 0)
            {
                ResourceBundle bundle = PageHandler.getBundle(pageContext.getSession());
                out.println("<tr><td colspan=4 class=\"standardText\">" + bundle.getString(emptyTableMsg) + "</td></tr>");
            }
            out.println("</table>");

        }
        catch (IOException e)
        {
            throw new AmbassadorTagException(pageContext, "I/O exception " + e.getMessage());
        }
        evenRow = false;
        return EVAL_PAGE;
    }

    public void release()
    {
        init();
    }

    protected void init()
    {
        bean = null;
        data = null;
        pageUrl = null;
        width = null;
        filterMethod = null;
        filterData = null;
        filterSelection = null;
        skip = false;
        evenRow = true;
        taskListStart = null;
    }

    public String getOtherUrl()
    {
        return otherUrl;
    }

    public void setOtherUrl(String otherUrl)
    {
        this.otherUrl = otherUrl;
    }

    public boolean isHasFilter()
    {
        return hasFilter;
    }

    public void setHasFilter(boolean hasFilter)
    {
        this.hasFilter = hasFilter;
    }
}
