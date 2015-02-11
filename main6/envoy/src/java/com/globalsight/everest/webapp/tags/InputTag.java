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
 * Abstract class for input fields.
 */
public abstract class InputTag extends BodyTagSupport {
    protected PageContext pageContext;
    protected String access = "shared";
    protected boolean disabled= false;
    protected String name;
    protected String onChange;  
    protected String onKeyPress;  
    protected String style;
    protected String styleClass;
    protected String value;

    public InputTag() {
        super();
        init();
    }

    public void setAccess(String access)
    {   
        this.access = access;
    }

    public void setDisabled(boolean disabled)
    {   
        this.disabled = disabled;
    }
    
    public void setName(String name)
    {   
        this.name = name;
    }
    
    public void setOnChange(String onChange)
    {
        this.onChange = onChange;
    }

    public void setOnKeyPress(String onKeyPress)
    {
        this.onKeyPress = onKeyPress;
    }

    public void setPageContext(PageContext pageContext) {
        this.pageContext = pageContext;
    }

    public void setStyle(String style)
    {   
        this.style = style;
    }
    
    public void setStyleClass(String styleClass)
    {   
        this.styleClass = styleClass;
    }
    
    public void setValue(String value)
    {   
        this.value = value;
    }
    
    public String getAccess()
    {   
        return access;
    }
    
    public boolean isDisabled()
    {   
        return disabled;
    }
    
    public String getName()
    {   
        return name;
    }
    
    public String getOnChange()
    {
        return onChange;
    }

    public String getOnKeyPress()
    {
        return onKeyPress;
    }

    public String getStyle()
    {   
        return style;
    }
    
    public String getStyleClass()
    {   
        return styleClass;
    }

    public String getValue()
    {   
        return value;
    }

    public int doStartTag() throws JspException {

        return EVAL_BODY_AGAIN;
    }

    public int doAfterBody() throws JspException {
        BodyContent body = getBodyContent();
        value = getDefaultFromBody(body);
        return SKIP_BODY;
    }

    public int doEndTag() throws JspException
    {
        if (access == null || access.equals(""))
        {
            printShared();
        }
        else if (access.equals("shared"))
        {
            printShared();
        }
        else if (access.equals("locked"))
        {
            printLocked();
        }
        else
        {
            printHidden();
        }
        return EVAL_BODY_AGAIN;
    }

    /**
     * All subclasses must implement this.  They should write the html
     * for the particular input field.
     */
    public abstract void printShared() throws JspException;

    /**
     * Will print the value of the input field.
     */
    public void printLocked()
    throws JspException
    {
        try
        {
            JspWriter out = pageContext.getOut();
            out.println(value);
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

    public void release()
    {
        super.release();
    }

    private void init()
    {
        access = "shared";
        disabled = false;
        name = null;
        onChange  = null;
        onKeyPress  = null;
        style = null;
        styleClass = null;
        value = null;
    }

    private String getDefaultFromBody(BodyContent body)
                                    throws JspTagException {
        if (value != null)
            return value;
        String bodyValue = "";
        if (body != null) {
            bodyValue = body.getString();
            if (bodyValue!= null) {
                bodyValue = bodyValue.trim();//trim the spaces
            }
            body.clearBody();
        }
        return bodyValue;
    }

}

