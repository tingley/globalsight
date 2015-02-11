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
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.pagehandler.PageHandler;

/**
 * Checks if the user has permission.  If not, it doesn't show the body.
 */
public class PermissionTag extends TagSupport
{
    private String  name;  // The name of the permission to check

    public PermissionTag()
    {
        init();
    }

    /**
     * Set the name of the permission.
     */
    public void setName(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public void release()
    {
        init();
    }

    public void init()
    {
        name = null;
    }

    public int doStartTag() throws JspException
    {
        JspWriter out = pageContext.getOut();
        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        HttpSession session = request.getSession(false);

        PermissionSet userPerms = (PermissionSet) session.getAttribute(
                    WebAppConstants.PERMISSIONS);
        boolean hasPerm = userPerms.getPermissionFor(name);

        if (hasPerm)
            return EVAL_PAGE;
        else
            return SKIP_BODY;
    }
}
