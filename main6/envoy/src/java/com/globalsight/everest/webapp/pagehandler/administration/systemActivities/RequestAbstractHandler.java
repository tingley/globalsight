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
package com.globalsight.everest.webapp.pagehandler.administration.systemActivities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.util.comparator.StringComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.ActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.util.GeneralException;

/**
 * XmldtdFilePageHandler, A page handler to produce the entry page (index.jsp)
 * for XmldtdFile management.
 */
public abstract class RequestAbstractHandler extends PageActionHandler
{
    @ActionHandler(action = "remove", formClass = "")
    public void remove(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        String[] keys = request.getParameterValues("key");
        for (String key : keys)
        {
            cancelRequest(key);
        }
    }
    
    abstract protected void cancelRequest(String key);
    @SuppressWarnings("rawtypes")
    abstract protected List getAllVos();
    abstract protected StringComparator getComparator(Locale uiLocale);
    
    /**
     * Get list of all rules.
     */
    @SuppressWarnings("rawtypes")
    private void dataForTable(HttpServletRequest request)
            throws GeneralException
    {
        HttpSession session = request.getSession(false);
        int n = 10;
        String size = request.getParameter("numOfPageSize");
        if (size != null)
        {
            n = Integer.parseInt(size);
            session.setAttribute("systemActivityPageSize", size);
        }
        else
        {
            size = (String) session.getAttribute("systemActivityPageSize");
            if (size != null)
            {
                n = Integer.parseInt(size);
            }
        }
        
        List vos = getAllVos();
        Locale uiLocale = (Locale) session
                .getAttribute(WebAppConstants.UILOCALE);
        setTableNavigation(request, session,
                vos, getComparator(uiLocale), n, "requestDefine",
                "requestDefineKey");

        String tableNav = "";
        String tableNav2 = "";
        if (vos.size() == 0)
        {
            ResourceBundle bundle = PageHandler.getBundle(request.getSession());
            tableNav = bundle.getString("lb_displaying_zero");
            tableNav2 = getNav2(n);
        }
        
        request.setAttribute("tableNav", tableNav);
        request.setAttribute("tableNav2", tableNav2);
        request.setAttribute("pageSize", n);
        request.setAttribute("vos", vos);
    }

    private String getNav2(int pageSize)
    {
        StringBuffer sb = new StringBuffer();
        sb.append("Display #: ");
        sb.append("<select id='numOfPageSize' onchange='changePageSize(this.value);'>");
        List<Integer> pageScopes = new ArrayList<Integer>();
        pageScopes.add(10);
        pageScopes.add(20);
        pageScopes.add(50);
        for (Integer s : pageScopes)
        {
            if (pageSize == s)
                sb.append("<option value='" + s + "' selected>" + s + "</option>");
            else
                sb.append("<option value='" + s + "'>" + s + "</option>");
        }
        sb.append("</select>&nbsp;&nbsp;");
        return sb.toString();
    }    
    

    @Override
    public void afterAction(HttpServletRequest request,
            HttpServletResponse response)
    {
        dataForTable(request);
    }

    @Override
    public void beforeAction(HttpServletRequest request,
            HttpServletResponse response) throws EnvoyServletException, ServletException, IOException
    {

    }
}
