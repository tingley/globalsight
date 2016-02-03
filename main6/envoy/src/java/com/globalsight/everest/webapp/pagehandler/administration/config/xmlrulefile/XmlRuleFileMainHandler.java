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
package com.globalsight.everest.webapp.pagehandler.administration.config.xmlrulefile;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.cxe.entity.filterconfiguration.FilterHelper;
import com.globalsight.cxe.entity.filterconfiguration.XMLRuleFilter;
import com.globalsight.cxe.entity.xmlrulefile.XmlRuleFile;
import com.globalsight.cxe.entity.xmlrulefile.XmlRuleFileImpl;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.XmlRuleFileComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.FormUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.StringUtil;
import com.globalsight.util.edit.EditUtil;

/**
 * XmlRuleFilePageHandler, A page handler to produce the entry page (index.jsp)
 * for XmlRuleFile management.
 */
public class XmlRuleFileMainHandler extends PageHandler
{
    /**
     * Invokes this PageHandler
     *
     * @param p_pageDescriptor
     *            the page desciptor
     * @param p_request
     *            the original request sent from the browser
     * @param p_response
     *            the original response object
     * @param p_context
     *            context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor, HttpServletRequest p_request,
            HttpServletResponse p_response, ServletContext p_context) throws ServletException,
            IOException, EnvoyServletException
    {
        HttpSession session = p_request.getSession(false);
        String action = p_request.getParameter("action");
        SessionManager sessionManager = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        try
        {
            if (XmlRuleConstant.CANCEL.equals(action))
            {
                clearSessionExceptTableInfo(session, XmlRuleConstant.XMLRULE_KEY);
            }
            else if (XmlRuleConstant.NEW.equals(action))
            {
                if (FormUtil.isNotDuplicateSubmisson(p_request, FormUtil.Forms.NEW_XML_RULE))
                {
                    createRule(p_request, session);
                    clearSessionExceptTableInfo(session, XmlRuleConstant.XMLRULE_KEY);
                }
            }
            else if (XmlRuleConstant.EDIT.equals(action))
            {
                updateRule(p_request, session);
                clearSessionExceptTableInfo(session, XmlRuleConstant.XMLRULE_KEY);
            }
            else if (XmlRuleConstant.REMOVE.equals(action))
            {
                if (p_request.getMethod().equalsIgnoreCase(REQUEST_METHOD_GET))
                {
                    p_response.sendRedirect("/globalsight/ControlServlet?activityName=xmlrules");
                    return;
                }
                removeRule(p_request, session);
                clearSessionExceptTableInfo(session, XmlRuleConstant.XMLRULE_KEY);
            }
            handleFilters(p_request, sessionManager, action);
            dataForTable(p_request, session);
        }
        catch (NamingException ne)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, ne);
        }
        catch (RemoteException re)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, re);
        }
        catch (GeneralException ge)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, ge);
        }

        super.invokePageHandler(p_pageDescriptor, p_request, p_response, p_context);
    }

    private void removeRule(HttpServletRequest p_request, HttpSession session)
            throws RemoteException, NamingException, GeneralException
    {

        String ids = (String) p_request.getParameter(RADIO_BUTTON);
        String[] idarr = ids.split(" ");
        for (int j = 0; j < idarr.length; j++)
        {
            String id = idarr[j];
            // check whether some file profiles using it.
            String companyId = CompanyThreadLocal.getInstance().getValue();
            List xmlRuleFilters = FilterHelper.getXmlRuleFilters(id, companyId);
            if (xmlRuleFilters.size() > 0)
            {
                StringBuffer names = new StringBuffer();
                for (int i = 0; i < xmlRuleFilters.size(); i++)
                {
                    XMLRuleFilter filter = (XMLRuleFilter) xmlRuleFilters.get(i);
                    if (i > 0)
                    {
                        names.append(", ");
                    }
                    names.append(EditUtil.encodeXmlEntities(filter.getFilterName()));
                }
                p_request.setAttribute("invalid",
                        "This XML Rule is being used by the Xml Rule Filter: " + names);
            }
            else
            {
                XmlRuleFile xrf = ServerProxy.getXmlRuleFilePersistenceManager().readXmlRuleFile(
                        Long.parseLong(id));
                ServerProxy.getXmlRuleFilePersistenceManager().deleteXmlRuleFile(xrf);
            }
        }
    }

    /**
     * Create a new rule.
     */
    private void createRule(HttpServletRequest p_request, HttpSession p_session)
            throws RemoteException, NamingException, GeneralException
    {
        XmlRuleFileImpl ruleFile = new XmlRuleFileImpl();

        getParams(p_request, ruleFile);

        ServerProxy.getXmlRuleFilePersistenceManager().createXmlRuleFile(ruleFile);
    }

    /**
     * Modify an existing rule.
     */
    private void updateRule(HttpServletRequest p_request, HttpSession p_session)
            throws RemoteException, NamingException, GeneralException
    {
        SessionManager sessionMgr = (SessionManager) p_session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        XmlRuleFileImpl ruleFile = (XmlRuleFileImpl) sessionMgr
                .getAttribute(XmlRuleConstant.XMLRULE_KEY);

        getParams(p_request, ruleFile);

        ServerProxy.getXmlRuleFilePersistenceManager().updateXmlRuleFile(ruleFile);
    }

    /**
     * Get request params and update rule.
     */
    private void getParams(HttpServletRequest p_request, XmlRuleFileImpl p_ruleFile)
    {
        String name = p_request.getParameter("saveRuleName");
        String desc = p_request.getParameter("descField");
        String rule = p_request.getParameter("textField");

        p_ruleFile.setName(name);
        p_ruleFile.setDescription(desc);
        p_ruleFile.setRuleText(rule);
    }

    /**
     * Get list of all rules.
     */
    private void dataForTable(HttpServletRequest p_request, HttpSession p_session)
            throws RemoteException, NamingException, GeneralException
    {
        Collection xmlrulefiles = ServerProxy.getXmlRuleFilePersistenceManager()
                .getAllXmlRuleFiles();
        Locale uiLocale = (Locale) p_session.getAttribute(WebAppConstants.UILOCALE);

        SessionManager sessionManager = (SessionManager) p_session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        String xmlruleName = (String) sessionManager.getAttribute("xmlruleName");
        String xmlruleCompName = (String) sessionManager.getAttribute("xmlruleCompName");

        Object[] extensions = xmlrulefiles.toArray();
        ArrayList fes = new ArrayList();

        if (xmlruleName != "" || xmlruleCompName != "")
        {
            if (xmlruleName != null && xmlruleName != "")
            {
                for (int i = 0; i < extensions.length; i++)
                {
                    if (extensions[i].toString().toLowerCase().indexOf(xmlruleName.toLowerCase()) >= 0)
                    {
                        fes.add(extensions[i]);
                    }
                }
            }

            else if (xmlruleCompName != null && xmlruleCompName != "")
            {
                for (int i = 0; i < extensions.length; i++)
                {
                    String compName = CompanyWrapper.getCompanyNameById(
                            ((XmlRuleFileImpl) extensions[i]).getCompanyId()).toLowerCase();
                    if (compName.toLowerCase().indexOf(xmlruleCompName.toLowerCase()) >= 0)
                    {
                        fes.add(extensions[i]);
                    }
                }
            }
            else
            {

                for (int i = 0; i < extensions.length; i++)
                {
                    fes.add(extensions[i]);
                }

            }
        }
        else
        {
            for (int i = 0; i < extensions.length; i++)
            {
                fes.add(extensions[i]);
            }
        }

        int numPerPage = getNumPerPage(p_request, p_session);
        setTableNavigation(p_request, p_session, fes, new XmlRuleFileComparator(uiLocale),
                numPerPage, XmlRuleConstant.XMLRULE_LIST, XmlRuleConstant.XMLRULE_KEY);
    }

    private int getNumPerPage(HttpServletRequest p_request, HttpSession p_session)
    {
        int result = 10;

        SessionManager sessionManager = (SessionManager) p_session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        String xmlruleNumPerPage = p_request.getParameter("numOfPageSize");
        if (StringUtil.isEmpty(xmlruleNumPerPage))
        {
            xmlruleNumPerPage = (String) sessionManager.getAttribute("xmlruleNumPerPage");
        }

        if (xmlruleNumPerPage != null)
        {
            sessionManager.setAttribute("xmlruleNumPerPage", xmlruleNumPerPage.trim());
            if ("all".equalsIgnoreCase(xmlruleNumPerPage))
            {
                result = Integer.MAX_VALUE;
            }
            else
            {
                try
                {
                    result = Integer.parseInt(xmlruleNumPerPage);
                }
                catch (NumberFormatException ignore)
                {
                    result = 10;
                }
            }
        }

        return result;
    }

    private void handleFilters(HttpServletRequest p_request, SessionManager sessionMgr,
            String action)
    {
        String xmlruleName = (String) p_request.getParameter("xmlruleName");
        String xmlruleCompName = (String) p_request.getParameter("xmlruleCompName");
        if (p_request.getMethod().equalsIgnoreCase(WebAppConstants.REQUEST_METHOD_GET))
        {
            xmlruleName = (String) sessionMgr.getAttribute("xmlruleName");
            xmlruleCompName = (String) sessionMgr.getAttribute("xmlruleCompName");
        }
        sessionMgr.setAttribute("xmlruleName", xmlruleName);
        sessionMgr.setAttribute("xmlruleCompName", xmlruleCompName);
    }

}