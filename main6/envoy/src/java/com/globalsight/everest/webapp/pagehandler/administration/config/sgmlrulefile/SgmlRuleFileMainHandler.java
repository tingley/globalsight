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

package com.globalsight.everest.webapp.pagehandler.administration.config.sgmlrulefile;

import org.apache.log4j.Logger;

import com.globalsight.everest.webapp.pagehandler.administration.config.sgmlrulefile.FileUploadHelper;
import com.globalsight.ling.sgml.sgmlrules.SgmlRule;
import com.globalsight.ling.sgml.sgmlrules.SgmlRulesManager;

import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.javabean.NavigationBean;
import com.globalsight.everest.webapp.pagehandler.ControlFlowHelper;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.tasks.TaskHelper;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;

import com.globalsight.util.edit.EditUtil;

import com.globalsight.ling.sgml.dtd.DTDParser;
import com.globalsight.ling.sgml.dtd.DTD;

import java.io.*;
import java.util.*;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class SgmlRuleFileMainHandler
    extends PageHandler
    implements SgmlRuleConstants
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            SgmlRuleFileMainHandler.class);

    //
    // Interface Methods: PageHandler
    //

    /**
     *
     * @param p_pageDescriptor the page desciptor
     * @param p_request the original request sent from the browser
     * @param p_response the original response object
     * @param p_context context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
        HttpServletRequest p_request, HttpServletResponse p_response,
        ServletContext p_context)
        throws ServletException,
               IOException,
               EnvoyServletException
    {
        HttpSession session = p_request.getSession();
        SessionManager sessionMgr = (SessionManager)session.getAttribute(
            WebAppConstants.SESSION_MANAGER);

        Locale uiLocale = (Locale)session.getAttribute(UILOCALE);

        // Get user object for the person who has logged in.
        //User user = TaskHelper.getUser(session);

        String action = p_request.getParameter("action");

        try
        {
            if (action == null)
            {
                String dtds = SgmlRulesManager.getDtdsAsXml();
                sessionMgr.setAttribute("dtds", dtds);
            }
            else if (action.equals("upload"))
            {
                FileUploadHelper o_upload = new FileUploadHelper();
                o_upload.doUpload(p_request);

                String filename = o_upload.getSavedFilepath();
                String publicid = o_upload.getFieldValue("publicid");
                publicid = publicid.trim();

                filename = getFilename(filename);

                try
                {
                    // Create the DTD with the filename as systemid.
                    SgmlRulesManager.addDTD(publicid, filename);

                    if (CATEGORY.isDebugEnabled())
                    {
                        CATEGORY.debug("DTD uploaded: PUBLIC ID `" +
                            publicid + "'" + " file `" + filename + "'");
                    }
                }
                catch (Exception ex)
                {
                    sessionMgr.setAttribute("error", ex.getMessage());
                }

                // Update the catalog.
                String dtds = SgmlRulesManager.getDtdsAsXml();
                sessionMgr.setAttribute("dtds", dtds);
            }
            else if (action.equals("create"))
            {
                String publicid = p_request.getParameter("publicid");
                publicid = publicid.trim();

                try
                {
                    SgmlRulesManager.addDTD(publicid, "");
                }
                catch (Exception ex)
                {
                    sessionMgr.setAttribute("error", ex.getMessage());
                }

                // Update the catalog.
                String dtds = SgmlRulesManager.getDtdsAsXml();
                sessionMgr.setAttribute("dtds", dtds);
                setupEdit(p_request, sessionMgr, session);
            }
            else if (action.equals("delete"))
            {
                String publicid = p_request.getParameter("publicid");

                SgmlRulesManager.removeDTD(publicid);

                if (CATEGORY.isDebugEnabled())
                {
                    CATEGORY.debug("DTD removed: PUBLIC ID `" + publicid + "'");
                }

                // Update the catalog.
                String dtds = SgmlRulesManager.getDtdsAsXml();
                sessionMgr.setAttribute("dtds", dtds);
            }
            else if (action.equals("self"))
            {
                // do save first
                //doSave(p_request, sessionMgr, false);

                setupEdit(p_request, sessionMgr, session);
            }
            else if (action.equals("edit"))
            {
                setupEdit(p_request, sessionMgr, session);
            }
            else if (action.equals("changeElement"))
            {
                doChangeElement(p_request, sessionMgr);
                setupEdit(p_request, sessionMgr, session);
            }
            else if (action.equals("addElement"))
            {
                SgmlRule rule = (SgmlRule)sessionMgr.getAttribute("rule");
                doAddElement(p_request, rule);
                setupEdit(p_request, sessionMgr, session);
            }
            else if (action.equals("removeElement"))
            {
                SgmlRule rule = (SgmlRule)sessionMgr.getAttribute("rule");
                doRemoveElement(p_request, rule);
                setupEdit(p_request, sessionMgr, session);
            }
            else if (action.equals("changeAttribute"))
            {
                SgmlRule rule = (SgmlRule)sessionMgr.getAttribute("rule");
                doChangeAttr(p_request, session, rule);
                SgmlRulesManager.saveSgmlRule(rule);
                setupEdit(p_request, sessionMgr, session);
            }
            else if (action.equals("addAttribute"))
            {
                SgmlRule rule = (SgmlRule)sessionMgr.getAttribute("rule");
                doAddAttr(p_request, session, rule);
                setupEdit(p_request, sessionMgr, session);
            }
            else if (action.equals("removeAttribute"))
            {
                SgmlRule rule = (SgmlRule)sessionMgr.getAttribute("rule");
                doRemoveAttr(p_request, session, rule);
                setupEdit(p_request, sessionMgr, session);
            }
            else if (action.equals("cancel") || action.equals("done"))
            {
                sessionMgr.clear();
                String dtds = SgmlRulesManager.getDtdsAsXml();
                sessionMgr.setAttribute("dtds", dtds);
                sessionMgr.removeElement("rule");
            }
        }
        catch (Throwable ex)
        {
            CATEGORY.error("sgml rule file error", ex);

            // JSP needs to clear this.
            sessionMgr.setAttribute("error", ex.getMessage());
        }

        super.invokePageHandler(p_pageDescriptor, p_request,
            p_response, p_context);
    }

    private void setupEdit(HttpServletRequest p_request,
        SessionManager p_sessionMgr, HttpSession p_session)
        throws EnvoyServletException
    {
        String publicid = p_request.getParameter("publicid");

        SgmlRule rule = null;
        // if publicid is null, then must be sorting or next, etc
        if (publicid != null)
        {
            rule = SgmlRulesManager.loadSgmlRule(publicid);
            p_sessionMgr.setAttribute("rule", rule);
        }
        else
        {
            rule = (SgmlRule)p_sessionMgr.getAttribute("rule");
        }

        String criteria = null;
        String searchField = (String)p_request.getParameter("searchField");
        if (p_request.getParameter("search") != null || searchField != null)
        {
            criteria = (String) p_request.getParameter("nameOptions");
            searchField = (String) p_request.getParameter("searchField");
            p_sessionMgr.setAttribute("criteria", criteria);
            p_sessionMgr.setAttribute("searchField", searchField);
        }
        else
        {
            p_sessionMgr.removeElement("criteria");
            p_sessionMgr.removeElement("searchField");
        }

        initElementsTable(p_request, p_session, p_sessionMgr,
            rule.getData(criteria, searchField));
    }

    private void doChangeElement(HttpServletRequest p_request,
        SessionManager p_sessionMgr)
        throws EnvoyServletException
    {
        SgmlRule rule = (SgmlRule)p_sessionMgr.getAttribute("rule");

        String orig = p_request.getParameter("origelemName");
        String name = p_request.getParameter("elemName");
        String type = p_request.getParameter("elemType");
        boolean paired = false;
        if (p_request.getParameter("elemPaired") != null)
        {
            paired = true;
        }

        rule.updateElement(orig, name, new Boolean(type).booleanValue(), paired);

        SgmlRulesManager.saveSgmlRule(rule);
    }

    private void doAddElement(HttpServletRequest p_request, SgmlRule p_rule)
    {
        String name = (String)p_request.getParameter("elemName");
        String type = (String)p_request.getParameter("elemType");
        boolean paired = false;
        if (p_request.getParameter("elemPaired") != null)
        {
            paired = true;
        }

        p_rule.addElement(name, new Boolean(type).booleanValue(), paired);
        p_request.setAttribute("selectElem", name);

        SgmlRulesManager.saveSgmlRule(p_rule);
    }

    private void doRemoveElement(HttpServletRequest p_request, SgmlRule p_rule)
    {
        String name = (String)p_request.getParameter("elemName");

        p_rule.removeElement(name);

        SgmlRulesManager.saveSgmlRule(p_rule);
    }

    private void doChangeAttr(HttpServletRequest p_request,
        HttpSession p_session, SgmlRule p_rule)
    {
        String elemName = (String)p_request.getParameter("elemName");
        String orig = p_request.getParameter("origattrName");
        String name = (String)p_request.getParameter("attrName");
        String extractAs = (String)p_request.getParameter("extractAs");
        String type = (String)p_request.getParameter("attrType");

        p_rule.updateAttribute(elemName, orig, name, extractAs, type);

        p_request.setAttribute("selectElem", elemName);
        p_request.setAttribute("selectAttr", name);

        SgmlRulesManager.saveSgmlRule(p_rule);
    }

    private void doAddAttr(HttpServletRequest p_request,
        HttpSession p_session, SgmlRule p_rule)
    {
        String elemName = (String)p_request.getParameter("elemName");
        String name = (String)p_request.getParameter("attrName");
        String extractAs = (String)p_request.getParameter("extractAs");
        String type = (String)p_request.getParameter("attrType");

        p_rule.addAttribute(elemName, name, extractAs, type);

        p_request.setAttribute("selectElem", elemName);
        p_request.setAttribute("selectAttr", name);

        SgmlRulesManager.saveSgmlRule(p_rule);
    }

    private void doRemoveAttr(HttpServletRequest p_request,
        HttpSession p_session, SgmlRule p_rule)
    {
        String elemName = (String)p_request.getParameter("elemName");
        String name = (String)p_request.getParameter("attrName");

        p_rule.removeAttribute(elemName, name);

        p_request.setAttribute("selectElem", elemName);
        p_request.setAttribute("selectAttr", name);

        SgmlRulesManager.saveSgmlRule(p_rule);
    }

    private String getFilename(String p_file)
    {
        int index = p_file.lastIndexOf("/");
        if (index < 0)
        {
            index = p_file.lastIndexOf("\\");
        }

        if (index >= 0)
        {
            return p_file.substring(index + 1);
        }

        return p_file;
    }

    private void initElementsTable(HttpServletRequest request,
        HttpSession session, SessionManager sessionMgr, List elements)
        throws EnvoyServletException
    {
        Locale uiLocale = (Locale)session.getAttribute(UILOCALE);

        setTableNavigation(request, session, elements,
            new ElementComparator(uiLocale),
            10,
            NUM_PER_PAGE_STR,
            NUM_PAGES, ELEM_LIST,
            SORTING,
            REVERSE_SORT,
            PAGE_NUM,
            LAST_PAGE_NUM,
            LIST_SIZE);
    }
}
