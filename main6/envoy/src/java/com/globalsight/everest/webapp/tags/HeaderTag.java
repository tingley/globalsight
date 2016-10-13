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

/**
 * This tag writes out the main heading and optional helper text.
 */ 
public class HeaderTag extends TagSupport {
    private String title = null;
    private String helperText = null;
    private PageContext pageContext;

    public HeaderTag() {
        super();
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public void setHelperText(String helperText)
    {
        this.helperText = helperText;
    }

    public String getTitle()
    {
        return title;
    }

    public String getHelperText()
    {
        return helperText;
    }

    public void setPageContext(PageContext pageContext)
    {
       this.pageContext = pageContext;
    }

    public int doStartTag() throws JspException
    {
        try
        {
            JspWriter out = pageContext.getOut();
            out.println("<span class='mainHeading'>" + title + "</span>");
            out.println("<p>");
            if (helperText != null)
            {
                out.println("<table cellspacing=0 cellpadding=0 border=0 class=standardText>");
                out.println("<tr><td width=100%>" +  helperText + "</td></tr></table><br>");
            }
        }
        catch (IOException ioe)
        {
             throw new JspException("Error: IOException while writing to client"
             + ioe.getMessage());
        }
        return SKIP_BODY;
    }

    public int doEndTag() throws JspException {
        return EVAL_PAGE;
    }

    public void release() {
        title = null;
        helperText = null;
    }

}

