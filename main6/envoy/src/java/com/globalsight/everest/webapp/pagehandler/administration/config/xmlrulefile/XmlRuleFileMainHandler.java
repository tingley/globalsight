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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
import com.globalsight.cxe.persistence.xmlrulefile.XmlRuleFileEntityException;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.XmlRuleFileComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.FormUtil;
import com.globalsight.util.GeneralException;

/**
 * XmlRuleFilePageHandler, A page handler to produce the entry page
 * (index.jsp) for XmlRuleFile management.
 */
public class XmlRuleFileMainHandler
    extends PageHandler
{
    /**
     * Invokes this PageHandler
     *
     * @param p_pageDescriptor the page desciptor
     * @param p_request the original request sent from the browser
     * @param p_response the original response object
     * @param p_context context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
        HttpServletRequest p_request, HttpServletResponse p_response,
        ServletContext p_context)
        throws ServletException, IOException, EnvoyServletException
    {
        HttpSession session = p_request.getSession(false);
        String action = p_request.getParameter("action");

        try
        {
            if (XmlRuleConstant.CANCEL.equals(action))
            {
                clearSessionExceptTableInfo(session, XmlRuleConstant.XMLRULE_KEY);
            }
            else if (XmlRuleConstant.NEW.equals(action))
            {
                if (FormUtil.isNotDuplicateSubmisson(p_request, FormUtil.Forms.NEW_XML_RULE)) {
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
        			p_response
        					.sendRedirect("/globalsight/ControlServlet?activityName=xmlrules");
        			return;
        		}
                removeRule(p_request, session);
                clearSessionExceptTableInfo(session, XmlRuleConstant.XMLRULE_KEY);
            }

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
			throws RemoteException, NamingException, GeneralException {

		String id = (String) p_request.getParameter(RADIO_BUTTON);

		// check whether some file profiles using it.		
		String fps = getFPNamesByXmlRuleId(id);
		String companyId = CompanyThreadLocal.getInstance().getValue();
		List xmlRuleFilters = FilterHelper.getXmlRuleFilters(id, companyId);
		if(xmlRuleFilters.size() > 0)
		{
            p_request.setAttribute("invalid",
                    "This XML Rule is being used by the Xml Rule Filter: "
                            + ((XMLRuleFilter)xmlRuleFilters.get(0)).getFilterName());
		}
		else if (fps != null && fps.length() > 0) {
			p_request.setAttribute("invalid",
					"This XML Rule is being used by these File Profiles: "
							+ fps.substring(0, fps.length() - 2));
		} else {
			XmlRuleFile xrf = ServerProxy.getXmlRuleFilePersistenceManager()
					.readXmlRuleFile(Long.parseLong(id));
			ServerProxy.getXmlRuleFilePersistenceManager().deleteXmlRuleFile(
					xrf);
		}

	}

	private String getFPNamesByXmlRuleId(String x_ruleid)
			throws XmlRuleFileEntityException, RemoteException {
		StringBuilder fps = new StringBuilder();

		String sql = "SELECT DISTINCT NAME " + "FROM FILE_PROFILE "
				+ "WHERE XML_RULE_ID = :ruleid";

		try {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("ruleid", x_ruleid);
			List<?> list = HibernateUtil.searchWithSql(sql, params);

			for (Object obj : list) {
				fps.append(obj.toString()).append(", ");
			}
		} catch (Exception e) {
			throw new XmlRuleFileEntityException(e);
		}

		return fps.toString();
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
        SessionManager sessionMgr = (SessionManager)p_session.getAttribute(
            WebAppConstants.SESSION_MANAGER);
        XmlRuleFileImpl ruleFile = (XmlRuleFileImpl)sessionMgr.getAttribute(
            XmlRuleConstant.XMLRULE_KEY);

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
        Collection xmlrulefiles = ServerProxy.getXmlRuleFilePersistenceManager().
            getAllXmlRuleFiles();
        Locale uiLocale = (Locale)p_session.getAttribute(WebAppConstants.UILOCALE);

        setTableNavigation(p_request, p_session, new ArrayList(xmlrulefiles),
            new XmlRuleFileComparator(uiLocale),
            10,
            XmlRuleConstant.XMLRULE_LIST, XmlRuleConstant.XMLRULE_KEY);
    }
}


