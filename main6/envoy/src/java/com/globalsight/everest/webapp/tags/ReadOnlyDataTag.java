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

import java.io.*;
import java.util.ResourceBundle;
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;
import javax.servlet.http.HttpSession;

import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.pagehandler.PageHandler;

/**
 * This tag writes out a value or "CONFIDENTIAL"
 */ 
public class ReadOnlyDataTag implements Tag {
    private String access = "shared";
    private String value;
    private Tag parent;
    private PageContext pageContext;

    public ReadOnlyDataTag() {
        super();
    }

    public void setAccess(String access)
    {
        this.access = access;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    public void setParent(Tag parent)
    {
       this.parent = parent;
    }

    public void setPageContext(PageContext pageContext)
    {
       this.pageContext = pageContext;
    }

    public Tag getParent()
    {
       return parent;
    }

    public String getAccess()
    {
        return access;
    }

    public String getValue()
    {
        return value;
    }
    
    public int doStartTag() throws JspException
    {
        if ("hidden".equals(access))
        {
            printHidden();
        }
        else
        {
            printShared();
        }
        return SKIP_BODY;
    }

    public int doEndTag() throws JspException {
        return EVAL_PAGE;
    }

    public void release() {
        access = "shared";
        value = null;
    }

    public void printShared()
    throws JspException
    {
        try
        {
            JspWriter out = pageContext.getOut();
            out.println("<span class='standardText'>" + value + "</span>");
        } catch (IOException ioe) {
             throw new JspException("Error: IOException while writing to client" 
             + ioe.getMessage());
        }
    }

    /**
     * Will print [CONFIDENTIAL]
     */
    public void printHidden()
    throws JspException
    {
        try
        {
            JspWriter out = pageContext.getOut();
            ResourceBundle bundle = PageHandler.getBundle(pageContext.getSession());
            out.println("<span class=confidential>[" +
                  bundle.getString("lb_confidential") + "]</span>");
        } catch (IOException ioe) {
             throw new JspException("Error: IOException while writing to client"
             + ioe.getMessage());
        }
    }

}

