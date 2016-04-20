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

import java.io.IOException;
import java.util.ResourceBundle;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.log4j.Logger;

import com.globalsight.everest.webapp.pagehandler.PageHandler;

public class Lb extends TagSupport
{
    static private final Logger logger = Logger.getLogger(Lb.class);

    private static final long serialVersionUID = 4320560255272161662L;
    private String key;

    public Lb()
    {
        super();
    }

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    @Override
    public int doEndTag() throws JspException
    {
        JspWriter out = pageContext.getOut();
        ResourceBundle bundle = PageHandler.getBundle(pageContext.getSession());
        try
        {
            out.write(bundle.getString(key));
            out.flush();
        }
        catch (IOException e)
        {
            logger.error(e);
        }
        
        return TagSupport.EVAL_PAGE;
    }
}
