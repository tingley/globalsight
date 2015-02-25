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
package com.globalsight.connector.mindtouch;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.connector.mindtouch.form.MindTouchConnectorFilter;
import com.globalsight.cxe.entity.mindtouch.MindTouchConnector;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.util.comparator.MindTouchConnectorComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.ActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageActionHandler;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.StringUtil;

public class MindTouchMainHandler extends PageActionHandler
{
    static private final Logger logger = Logger
            .getLogger(MindTouchMainHandler.class);

    private List<?> allConns = null;

    @ActionHandler(action = "save", formClass = "com.globalsight.cxe.entity.mindtouch.MindTouchConnector", loadFromDb = true)
    public void save(HttpServletRequest request, HttpServletResponse response,
            Object form) throws Exception
    {
        MindTouchConnector connector = (MindTouchConnector) form;
        HibernateUtil.saveOrUpdate(connector);
    }

    @ActionHandler(action = "remove", formClass = "")
    public void remove(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        String[] ids = request.getParameterValues("mtConnectorIds");
        for (String id : ids)
        {
            long cId = Long.parseLong(id);
            MindTouchConnector c = HibernateUtil.get(MindTouchConnector.class, cId);
            c.setIsActive(false);
            HibernateUtil.update(c);
        }
    }

    @SuppressWarnings("unchecked")
    @ActionHandler(action = "filter", formClass = "com.globalsight.connector.mindtouch.form.MindTouchConnectorFilter")
    public void filter(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        allConns = MindTouchManager.getAllConnectors();
        MindTouchConnectorFilter filter = (MindTouchConnectorFilter) form;
        allConns = filter.filter((List<MindTouchConnector>) allConns);

        request.setAttribute("nameFilter", filter.getNameFilter());
        request.setAttribute("urlFilter", filter.getUrlFilter());
        request.setAttribute("usernameFilter", filter.getUsernameFilter());
        request.setAttribute("companyNameFilter", filter.getCompanyNameFilter());
    }

    private void dataForTable(HttpServletRequest request)
            throws GeneralException
    {
        HttpSession session = request.getSession(false);
        Locale uiLocale = (Locale) session
                .getAttribute(WebAppConstants.UILOCALE);

        if (allConns == null)
        {
            allConns = MindTouchManager.getAllConnectors();
        }

        Integer orgSize = (Integer) session.getAttribute("mindtouchConnectorPageSize");
        int size = orgSize == null ? 10 : orgSize;
        String numOfPerPage = request.getParameter("numOfPageSize");
        if (StringUtil.isNotEmpty(numOfPerPage))
        {
            try
            {
                size = Integer.parseInt(numOfPerPage);
            }
            catch (Exception e)
            {
                size = Integer.MAX_VALUE;
            }
            
            session.setAttribute("mindtouchConnectorPageSize", size);
        }

        setTableNavigation(request, session, allConns,
                new MindTouchConnectorComparator(uiLocale), size,
                "mindtouchConnectorList", "mindtouchConnectorKey");
    }

    @Override
    public void beforeAction(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException,
            EnvoyServletException
    {
        allConns = null;
        clearSessionExceptTableInfo(request.getSession(false),
                "mindtouchConnectorKey");

        response.setCharacterEncoding("utf-8");
    }

    @Override
    public void afterAction(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException,
            EnvoyServletException
    {
        dataForTable(request);
    }
}
