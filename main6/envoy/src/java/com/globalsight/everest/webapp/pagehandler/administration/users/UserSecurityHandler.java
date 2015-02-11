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
package com.globalsight.everest.webapp.pagehandler.administration.users;

import com.globalsight.everest.foundation.User;
import com.globalsight.everest.securitymgr.UserSecureFields;
import com.globalsight.everest.securitymgr.FieldSecurity;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.ControlFlowHelper;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.GeneralException;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


public class UserSecurityHandler extends PageHandler
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
    throws ServletException, IOException,
        EnvoyServletException
    {
        HttpSession session = request.getSession(false);
        SessionManager sessionMgr =
            (SessionManager)session.getAttribute(SESSION_MANAGER);
        User user = (User)sessionMgr.getAttribute(WebAppConstants.USER);
        String action = (String)request.getParameter("action");


        if ("security".equals(action))
        {
            // We're here from MOD1, and we need to get the base data from
            // the user.
            sessionMgr.setAttribute("edit", "true");
            ModifyUserWrapper wrapper = (ModifyUserWrapper)
                sessionMgr.getAttribute(UserConstants.MODIFY_USER_WRAPPER);
            UserUtil.extractUserData(request, wrapper, false);
            FieldSecurity fs = (FieldSecurity)sessionMgr.getAttribute("fieldSecurity");
            if (fs == null)
                fs = UserHandlerHelper.getSecurity(wrapper.getUser(), user, false);
            setFieldList(session, request, (CreateUserWrapper)wrapper, user, fs);
            sessionMgr.setAttribute("fieldSecurity",
                UserHandlerHelper.getSecurity(wrapper.getUser(), user, false));
        }
        else if ("prev".equals(action))
        {
            // Save data from permissions page
            CreateUserWrapper wrapper = (CreateUserWrapper)
                sessionMgr.getAttribute(UserConstants.CREATE_USER_WRAPPER);
            UserUtil.extractPermissionData(request);

            FieldSecurity fs = (FieldSecurity)sessionMgr.getAttribute("fieldSecurity");
            setFieldList(session, request, wrapper, user, fs);
        }
        else if ("next".equals(action))
        {
            // Get the data from the last page (adding projects page)
            CreateUserWrapper wrapper = (CreateUserWrapper)
                sessionMgr.getAttribute(UserConstants.CREATE_USER_WRAPPER);
            UserUtil.extractProjectData(request, wrapper);

            FieldSecurity fs = (FieldSecurity)sessionMgr.getAttribute("fieldSecurity");
            if (fs == null)
            {
                fs = UserHandlerHelper.getSecurity(wrapper.getUser(), user, false);
                sessionMgr.setAttribute("fieldSecurity", fs);
            }
            setFieldList(session, request, wrapper, user, fs);
        }


        // Call parent invokePageHandler() to set link beans and invoke JSP
        super.invokePageHandler(pageDescriptor, request,
                                response, context);
    }

    /**
     * Create a list of all fields in a vendor and set it in the request.
     * Create a list of all labels to go with the fields.
     */ 
    private void setFieldList(HttpSession session, HttpServletRequest request,
                              CreateUserWrapper wrapper, User user,
                              FieldSecurity fs)
    throws EnvoyServletException
    {
        // ORDER MATTERS - ALL 3 ARRAYS MUST BE IN THE SAME ORDER

        // create array of field names
        ArrayList fieldList = new ArrayList();
        fieldList.add(UserSecureFields.ACCESS_GROUPS);
        fieldList.add(UserSecureFields.ADDRESS);
        fieldList.add(UserSecureFields.CELL_PHONE);
        fieldList.add(UserSecureFields.COMPANY);
        fieldList.add(UserSecureFields.COUNTRY);
        fieldList.add(UserSecureFields.EMAIL_ADDRESS);
        fieldList.add(UserSecureFields.CC_EMAIL_ADDRESS);
        fieldList.add(UserSecureFields.BCC_EMAIL_ADDRESS);
        fieldList.add(UserSecureFields.EMAIL_LANGUAGE);
        fieldList.add(UserSecureFields.FAX);
        fieldList.add(UserSecureFields.FIRST_NAME);
        fieldList.add(UserSecureFields.HOME_PHONE);
        fieldList.add(UserSecureFields.LAST_NAME);
        fieldList.add(UserSecureFields.PASSWORD);
        fieldList.add(UserSecureFields.PROJECTS);
        fieldList.add(UserSecureFields.ROLES);
        fieldList.add(UserSecureFields.STATUS);
        fieldList.add(UserSecureFields.TITLE);
        fieldList.add(UserSecureFields.WORK_PHONE);
        request.setAttribute("fieldList", fieldList);

        // create array of field security values
        ArrayList fieldValueList = new ArrayList();
        SessionManager sessionMgr =
            (SessionManager)session.getAttribute(SESSION_MANAGER);
        fieldValueList.add(fs.get(UserSecureFields.ACCESS_GROUPS));
        fieldValueList.add(fs.get(UserSecureFields.ADDRESS));
        fieldValueList.add(fs.get(UserSecureFields.CELL_PHONE));
        fieldValueList.add(fs.get(UserSecureFields.COMPANY));
        fieldValueList.add(fs.get(UserSecureFields.COUNTRY));
        fieldValueList.add(fs.get(UserSecureFields.EMAIL_ADDRESS));
        fieldValueList.add(fs.get(UserSecureFields.CC_EMAIL_ADDRESS));
        fieldValueList.add(fs.get(UserSecureFields.BCC_EMAIL_ADDRESS));
        fieldValueList.add(fs.get(UserSecureFields.EMAIL_LANGUAGE));
        fieldValueList.add(fs.get(UserSecureFields.FAX));
        fieldValueList.add(fs.get(UserSecureFields.FIRST_NAME));
        fieldValueList.add(fs.get(UserSecureFields.HOME_PHONE));
        fieldValueList.add(fs.get(UserSecureFields.LAST_NAME));
        fieldValueList.add(fs.get(UserSecureFields.PASSWORD));
        fieldValueList.add(fs.get(UserSecureFields.PROJECTS));
        fieldValueList.add(fs.get(UserSecureFields.ROLES));
        fieldValueList.add(fs.get(UserSecureFields.STATUS));
        fieldValueList.add(fs.get(UserSecureFields.TITLE));
        fieldValueList.add(fs.get(UserSecureFields.WORK_PHONE));
        request.setAttribute("fieldValueList", fieldValueList);

        // create array of labels
        ResourceBundle bundle = getBundle(session);
        ArrayList labelList = new ArrayList();
        labelList.add(bundle.getString("lb_access_level"));
        labelList.add(bundle.getString("lb_address"));
        labelList.add(bundle.getString("lb_cell_phone"));
        labelList.add(bundle.getString("lb_company_name"));
        labelList.add(bundle.getString("lb_country"));
        labelList.add(bundle.getString("lb_email"));
        labelList.add(bundle.getString("lb_cc_email"));
        labelList.add(bundle.getString("lb_bcc_email"));
        labelList.add(bundle.getString("lb_email_language"));
        labelList.add(bundle.getString("lb_fax"));
        labelList.add(bundle.getString("lb_first_name"));
        labelList.add(bundle.getString("lb_home_phone"));
        labelList.add(bundle.getString("lb_last_name"));
        labelList.add(bundle.getString("lb_password"));
        labelList.add(bundle.getString("lb_projects"));
        labelList.add(bundle.getString("lb_roles"));
        labelList.add(bundle.getString("lb_status"));
        labelList.add(bundle.getString("lb_title"));
        labelList.add(bundle.getString("lb_work_phone"));
        request.setAttribute("labelList", labelList);
    }
}
