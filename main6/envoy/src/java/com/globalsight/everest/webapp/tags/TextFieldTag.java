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
 * This tag writes out the html for <input type="text">
 */ 
public class TextFieldTag extends InputTag {
    private Tag parent;
    private int maxlength = 0;
    private int size = 0;

    public TextFieldTag() {
        super();
    }

    public void setMaxlength(int maxlength)
    {   
        this.maxlength = maxlength;
    }
    
    public void setSize(int size)
    {   
        this.size = size;
    }
    
    public int getMaxlength()
    {
        return maxlength;
    }
    
    public String getName()
    {   
        return name;
    }
    
    public int getSize()
    {   
        return size;
    }
    
    public int doStartTag() throws JspException
    {
        super.doStartTag();
        return EVAL_BODY_AGAIN;
    }

    public int doEndTag() throws JspException {
        super.doEndTag();
        return EVAL_PAGE;
    }

    public void release() {
        maxlength = 0;
        size = 0;
    }

    public void setParent(Tag parent) {
        this.parent = parent;
    }

    public Tag getParent() {
       return parent;
    }

    public void printShared()
    throws JspException
    {
        try
        {
            JspWriter out = pageContext.getOut();
            out.print("<input type='text' name='" + name + "'");
            if (disabled)
                out.print(" disabled='" + disabled + "'");
            if (maxlength != 0)
                out.print(" maxlength='" + maxlength + "'");
            if (size != 0)
                out.print(" size='" + size + "'");
            if (onKeyPress != null)
                out.print(" onKeyPress='" + onKeyPress + "'");
            if (style != null)
                out.print(" style='" + style + "'");
            if (styleClass != null)
                out.print(" class='" + styleClass + "'");
            out.print(" value='" + value + "'>\n");
        } catch (IOException ioe) {
             throw new JspException("Error: IOException while writing to client" 
             + ioe.getMessage());
        }
    }

}

