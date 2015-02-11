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
package com.globalsight.everest.webapp.pagehandler.administration.vendors;

import org.apache.log4j.Logger;

import com.globalsight.everest.localemgr.LocaleManager;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.UserComparator;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.customform.CustomForm;
import com.globalsight.everest.vendormanagement.Vendor;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.GeneralException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Document;

public class CustomPageDesignerHandler extends PageHandler
{
    /**
     * Invokes this PageHandler
     *
     * @param p_pageDescriptor the page desciptor
     * @param p_request the original request sent from the browser
     * @param p_response the original response object
     * @param p_context context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor pageDescriptor,
                                  HttpServletRequest request,
                                  HttpServletResponse response,
                                  ServletContext context)
    throws ServletException, IOException, EnvoyServletException
    {
        HttpSession session = request.getSession(false);
        SessionManager sessionMgr =
            (SessionManager)session.getAttribute(SESSION_MANAGER);
        String action = (String)request.getParameter("action");

        if ("save".equals(action))
        {
            // Save the custom page designer info to the database  
            CustomPageHelper.saveCustomForm(request);
        }
        else if ("remove".equals(action))
        {
            // Remove the custom page designer info to the database  
            CustomPageHelper.removeForm();
            clearSessionExceptTableInfo(session, VendorConstants.VENDOR_KEY);
        }

        Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);

        // get existing form (if there is one)
        CustomForm customForm = CustomPageHelper.getCustomForm();
        if (customForm != null && !customForm.getLocale().getLocale().equals(uiLocale))
        {
            sessionMgr.setAttribute("designerLocale", customForm.getLocale().getLocale().toString());
            request.setAttribute("wrongLocale", "true");
        }
        else if (customForm == null)
        {
            // custom form does not exist
            String html = getEmptySection(session);
            sessionMgr.setAttribute("sections", html);
        }
        else
        {
            sessionMgr.setAttribute("pageTitle", customForm.getPageName());
            sessionMgr.setAttribute("designerLocale", customForm.getLocale().getLocale().toString());
            Document doc = CustomPageHelper.getDocument(customForm);
            String html = CustomPageHelper.getSectionsForDesigner(doc, getBundle(session));
            sessionMgr.setAttribute("sections", html);
            html = CustomPageHelper.getFieldsForDesigner(doc, getBundle(session));
            sessionMgr.setAttribute("fields", html);

        }
        
        // Call parent invokePageHandler() to set link beans and invoke JSP
        super.invokePageHandler(pageDescriptor, request, response, context);
    }

    /**
     * When no custom page exists, start with an empty table.  Return the html
     * for the empty table.
     */
    private String getEmptySection(HttpSession session)
    {
        ResourceBundle bundle = getBundle(session);

        StringBuffer section = new StringBuffer();
        section.append("<table id='sectionTable' border='0' cellspacing='0' cellpadding='5' class='list' width ='60%'>");
        section.append("<tbody id='sectionTableBody'>");
        section.append("<tr class='tableHeadingBasic'>");
        section.append("<td width='2%'>&nbsp;</td>");
        section.append("<td>" + bundle.getString("lb_name") +  "</td>");
        section.append("</tr><tr>");
        section.append("<td colspan='2' style='standardText'>");
        section.append("<div id='emptySectionTable' class='standardText'>");
        section.append("&nbsp;" + bundle.getString("jsmsg_custom_page_no_sections") + "</div>");
        section.append("</td></tr></tbody></table>");
        return section.toString();
    }
}
