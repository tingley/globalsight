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

import com.globalsight.everest.foundation.User;
import com.globalsight.everest.vendormanagement.Vendor;
import com.globalsight.everest.securitymgr.VendorSecureFields;
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


public class VendorSecurityHandler extends PageHandler
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
        Vendor vendor = (Vendor)sessionMgr.getAttribute("vendor");
        String action = (String)request.getParameter("action");

        // FieldSecurity will be in the session if the user did a next
        // then previous, so check that
        FieldSecurity fs = (FieldSecurity)
            sessionMgr.getAttribute(VendorConstants.FIELD_SECURITY_NOCHECK);
        if (fs == null)
        {
            fs = VendorHelper.getSecurity(vendor, user, false);
            sessionMgr.setAttribute(VendorConstants.FIELD_SECURITY_NOCHECK, fs);
        }
        
        if ("nextCustom".equals(action))
        {
            // Save the data from the custom fields page
            VendorHelper.saveCustom(vendor, request, sessionMgr);
        }
        else if ("next".equals(action))
        {
            // Save the data from the projects page
            VendorHelper.saveProjects(vendor, request);
        }
        else if ("prev".equals(action))
        {
            // Save the data from the permissions page
            VendorHelper.savePermissions(vendor, request, sessionMgr);
        }
        else if ("security".equals(action))
        {
            // Save the data from the basic info page
            VendorHelper.saveBasicInfo(vendor, request);

        }

        setFieldList(session, request, vendor, user, fs);

        // Call parent invokePageHandler() to set link beans and invoke JSP
        super.invokePageHandler(pageDescriptor, request,
                                response, context);
    }

    /**
     * Overide getControlFlowHelper so we can do processing
     * and redirect the user correctly.
     *
     * @return the name of the link to follow
     */
    public ControlFlowHelper getControlFlowHelper(
        HttpServletRequest p_request, HttpServletResponse p_response)
    {

        return new CustomFormControlFlowHelper(p_request, p_response);
    }

    /**
     * Create a list of all fields in a vendor and set it in the request.
     * Create a list of all labels to go with the fields.
     */ 
    private void setFieldList(HttpSession session, HttpServletRequest request,
                              Vendor vendor, User user, FieldSecurity fs)
    throws EnvoyServletException
    {
        // ORDER MATTERS - ALL 3 ARRAYS MUST BE IN THE SAME ORDER

        // create array of field names
        ArrayList fieldList = new ArrayList();
        fieldList.add(VendorSecureFields.ADDRESS);
        fieldList.add(VendorSecureFields.AMBASSADOR_ACCESS);
        fieldList.add(VendorSecureFields.CELL_PHONE);
        fieldList.add(VendorSecureFields.COMPANY);
        fieldList.add(VendorSecureFields.CITIZENSHIP);
        fieldList.add(VendorSecureFields.COUNTRY);
        fieldList.add(VendorSecureFields.CUSTOM_FIELDS);
        fieldList.add(VendorSecureFields.RESUME);
        fieldList.add(VendorSecureFields.DOB);
        fieldList.add(VendorSecureFields.EMAIL);
        fieldList.add(VendorSecureFields.EMAIL_LANGUAGE);
        fieldList.add(VendorSecureFields.FAX);
        fieldList.add(VendorSecureFields.FIRST_NAME);
        fieldList.add(VendorSecureFields.HOME_PHONE);
        fieldList.add(VendorSecureFields.LAST_NAME);
        fieldList.add(VendorSecureFields.NOTES);
        fieldList.add(VendorSecureFields.PASSWORD);
        fieldList.add(VendorSecureFields.PROJECTS);
        fieldList.add(VendorSecureFields.ROLES);
        fieldList.add(VendorSecureFields.STATUS);
        fieldList.add(VendorSecureFields.TITLE);
        fieldList.add(VendorSecureFields.IS_INTERNAL);
        fieldList.add(VendorSecureFields.USERNAME);
        fieldList.add(VendorSecureFields.CUSTOM_ID);
        fieldList.add(VendorSecureFields.WORK_PHONE);
        request.setAttribute("fieldList", fieldList);

        // create array of field security values
        ArrayList fieldValueList = new ArrayList();
        fieldValueList.add(fs.get(VendorSecureFields.ADDRESS));
        fieldValueList.add(fs.get(VendorSecureFields.AMBASSADOR_ACCESS));
        fieldValueList.add(fs.get(VendorSecureFields.CELL_PHONE));
        fieldValueList.add(fs.get(VendorSecureFields.COMPANY));
        fieldValueList.add(fs.get(VendorSecureFields.CITIZENSHIP));
        fieldValueList.add(fs.get(VendorSecureFields.COUNTRY));
        fieldValueList.add(fs.get(VendorSecureFields.CUSTOM_FIELDS));
        fieldValueList.add(fs.get(VendorSecureFields.RESUME));
        fieldValueList.add(fs.get(VendorSecureFields.DOB));
        fieldValueList.add(fs.get(VendorSecureFields.EMAIL));
        fieldValueList.add(fs.get(VendorSecureFields.EMAIL_LANGUAGE));
        fieldValueList.add(fs.get(VendorSecureFields.FAX));
        fieldValueList.add(fs.get(VendorSecureFields.FIRST_NAME));
        fieldValueList.add(fs.get(VendorSecureFields.HOME_PHONE));
        fieldValueList.add(fs.get(VendorSecureFields.LAST_NAME));
        fieldValueList.add(fs.get(VendorSecureFields.NOTES));
        fieldValueList.add(fs.get(VendorSecureFields.PASSWORD));
        fieldValueList.add(fs.get(VendorSecureFields.PROJECTS));
        fieldValueList.add(fs.get(VendorSecureFields.ROLES));
        fieldValueList.add(fs.get(VendorSecureFields.STATUS));
        fieldValueList.add(fs.get(VendorSecureFields.TITLE));
        fieldValueList.add(fs.get(VendorSecureFields.IS_INTERNAL));
        fieldValueList.add(fs.get(VendorSecureFields.USERNAME));
        fieldValueList.add(fs.get(VendorSecureFields.CUSTOM_ID));
        fieldValueList.add(fs.get(VendorSecureFields.WORK_PHONE));
        request.setAttribute("fieldValueList", fieldValueList);

        // create array of labels
        ResourceBundle bundle = getBundle(session);
        ArrayList labelList = new ArrayList();
        labelList.add(bundle.getString("lb_address"));
        labelList.add(bundle.getString("lb_ambassador_access"));
        labelList.add(bundle.getString("lb_cell_phone"));
        labelList.add(bundle.getString("lb_company_name"));
        labelList.add(bundle.getString("lb_countries"));
        labelList.add(bundle.getString("lb_country"));
        labelList.add(bundle.getString("lb_custom_fields"));
        labelList.add(bundle.getString("lb_cv_resume"));
        labelList.add(bundle.getString("lb_date_of_birth"));
        labelList.add(bundle.getString("lb_email"));
        labelList.add(bundle.getString("lb_email_language"));
        labelList.add(bundle.getString("lb_fax"));
        labelList.add(bundle.getString("lb_first_name"));
        labelList.add(bundle.getString("lb_home_phone"));
        labelList.add(bundle.getString("lb_last_name"));
        labelList.add(bundle.getString("lb_notes"));
        labelList.add(bundle.getString("lb_password"));
        labelList.add(bundle.getString("lb_projects"));
        labelList.add(bundle.getString("lb_roles"));
        labelList.add(bundle.getString("lb_status"));
        labelList.add(bundle.getString("lb_title"));
        labelList.add(bundle.getString("lb_type"));
        labelList.add(bundle.getString("lb_user_name"));
        labelList.add(bundle.getString("lb_vendor_id"));
        labelList.add(bundle.getString("lb_work_phone"));
        request.setAttribute("labelList", labelList);
    }
}
