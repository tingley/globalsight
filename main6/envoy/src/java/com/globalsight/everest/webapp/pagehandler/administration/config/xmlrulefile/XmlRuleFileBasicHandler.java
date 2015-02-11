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

import com.globalsight.cxe.entity.xmlrulefile.XmlRuleFile;
import com.globalsight.cxe.entity.xmlrulefile.XmlRuleFileImpl;
import com.globalsight.cxe.persistence.xmlrulefile.XmlRuleFilePersistenceManager;

import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.XmlRuleFileComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.javabean.NavigationBean;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.ling.docproc.ExtractorException;
import com.globalsight.ling.docproc.extractor.xml.RuleFileHelper;
import com.globalsight.ling.docproc.extractor.xml.RuleFileValidator;
import com.globalsight.util.FormUtil;
import com.globalsight.util.GeneralException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;
import java.rmi.RemoteException;
import javax.naming.NamingException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class XmlRuleFileBasicHandler
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
    	if (p_request.getMethod().equalsIgnoreCase(REQUEST_METHOD_GET)) 
		{
			p_response
					.sendRedirect("/globalsight/ControlServlet?activityName=xmlrules");
			return;
		}
        HttpSession session = p_request.getSession(false);
        SessionManager sessionMgr = (SessionManager)session.getAttribute(
            WebAppConstants.SESSION_MANAGER);

        String action = p_request.getParameter("action");

        try
        {
            setExistingRuleNames(p_request);

            if (XmlRuleConstant.DUPLICATE.equals(action))
            {
                String id = (String) p_request.getParameter(RADIO_BUTTON);
                XmlRuleFile xmlRule = ServerProxy.getXmlRuleFilePersistenceManager().
                    readXmlRuleFile(Long.parseLong(id));
                xmlRule.setName("new_xml_rule");

                sessionMgr.setAttribute(XmlRuleConstant.XMLRULE_KEY, xmlRule);
                p_request.setAttribute("dup", "true");
            }
            else if (XmlRuleConstant.EDIT.equals(action))
            {
                // Fetch the xmlRuleFile to edit and store in session
            	String id = (String) p_request.getParameter(RADIO_BUTTON);
                XmlRuleFile xmlRule = ServerProxy.getXmlRuleFilePersistenceManager().
                    readXmlRuleFile(Long.parseLong(id));

                sessionMgr.setAttribute(XmlRuleConstant.XMLRULE_KEY, xmlRule);
                sessionMgr.setAttribute("edit", "true");
            }
            else if (XmlRuleConstant.VALIDATE.equals(action))
            {
                validate(p_request);
                FormUtil.addSubmitToken(p_request, FormUtil.Forms.NEW_XML_RULE);
                p_request.setAttribute("isValidate", true);
            }
            else if (XmlRuleConstant.TEST.equals(action))
            {
                testRule(p_request);
                FormUtil.addSubmitToken(p_request, FormUtil.Forms.NEW_XML_RULE);
            }
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

    /**
     * Validate the xml.  If invalid, set the error in the request.
     */
    private void validate(HttpServletRequest p_request)
        throws EnvoyServletException
    {
        try
        {
            RuleFileValidator validator = new RuleFileValidator();
            validator.setResourceBundle(getBundle(p_request.getSession(false)));

            String name = p_request.getParameter("saveRuleName");
            String desc = p_request.getParameter("descField");
            String rule = p_request.getParameter("textField");

            p_request.setAttribute("tmpRule", new XmlRuleFileImpl(name, desc, rule));

            if (!validator.validate(rule))
            {
                p_request.setAttribute("invalid", validator.getErrorMessage());
            }
            else
            {
                p_request.setAttribute("invalid", "");
            }
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Validate the xml.  If invalid, set the error in the request.
     */
    private void testRule(HttpServletRequest p_request)
        throws EnvoyServletException
    {
        try
        {
            String name = p_request.getParameter("saveRuleName");
            String desc = p_request.getParameter("descField");
            String rule = p_request.getParameter("textField");
			String testText = p_request.getParameter("testField");
			String testResult = "";
			try
			{
				testResult = RuleFileHelper.extractXmlFileWithRule(testText, rule);
                p_request.setAttribute("invalid", "");
			}
			catch (ExtractorException e)
			{
				testResult = getBundle(p_request.getSession(false)).getString("lb_xml_rule_test_with_error") + ":" + e.getMessage();
			    //p_request.setAttribute("invalid", e.getMessage());
			}
			p_request.setAttribute("tmpRule", new XmlRuleFileImpl(name, desc,
					rule));
			p_request.setAttribute("testText", testText);
			p_request.setAttribute("testResult", testResult);
		}
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Get list of all xmlRuleFile names.  Needed in jsp to determine
     * duplicate names.
     */
    private void setExistingRuleNames(HttpServletRequest p_request)
        throws RemoteException, NamingException, GeneralException
    {
        ArrayList list = (ArrayList)
            ServerProxy.getXmlRuleFilePersistenceManager().getAllXmlRuleFiles();

        ArrayList names = new ArrayList();

        if (list != null)
        {
            for (int i = 0, max = list.size(); i < max; i++)
            {
                XmlRuleFileImpl rule = (XmlRuleFileImpl)list.get(i);
                names.add(rule.getName());
            }
        }

        p_request.setAttribute(XmlRuleConstant.NAMES, names);
    }
}


