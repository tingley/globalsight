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

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.TagSupport;

import java.io.IOException;

/**
 * This tag writes out the html for <input type="password"
 */
public class PasswordTag extends InputTag {

    // required attributes
    private int maxlength;
    private int size = 20;


    public void setMaxlength(int maxlength) {
        this.maxlength = maxlength;
    }

    public int getMaxlength() {
        return maxlength;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getSize() {
        return size;
    }

    /**
     * Writes the javascript to add a password field.
     */
    public int doStartTag() throws JspException
    {
        super.doStartTag();
        return EVAL_BODY_AGAIN;
    }

    public int doEndTag() throws JspException
    {
        super.doEndTag();
        return EVAL_PAGE;
    }


    public void printShared()
    throws JspException
    {
        try
        {
            JspWriter out = pageContext.getOut();
            out.print("<input type='password' name='" + name + "'");
            if (disabled)
                out.print(" disabled='" + disabled + "'");
            if (maxlength != 0)
                out.print(" maxlength='" + maxlength + "'");
            if (size != 0)
                out.print(" size='" + size + "'");
            out.print(" value='" + value + "'>\n");
        } catch (IOException ioe) {
             throw new JspException("Error: IOException while writing to client"
             + ioe.getMessage());
        }
    }

    public void release() {
        super.release();
        maxlength = 0;
        size = 20;
    }

}

