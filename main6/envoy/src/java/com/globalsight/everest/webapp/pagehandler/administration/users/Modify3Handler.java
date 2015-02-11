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

import java.io.IOException;
import java.util.Hashtable;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.GlobalSightLocale;

public class Modify3Handler extends PageHandler
{
    /**
     * Invokes this PageHandler.
     */
    public void invokePageHandler(WebPageDescriptor p_thePageDescriptor,
        HttpServletRequest p_theRequest, HttpServletResponse p_theResponse,
	ServletContext p_context)
        throws ServletException, IOException, EnvoyServletException
    {
        // Get the session manager.
        HttpSession session = p_theRequest.getSession(false);
        SessionManager sessionMgr =
            (SessionManager)session.getAttribute(SESSION_MANAGER);
        ModifyUserWrapper wrapper =
            (ModifyUserWrapper) sessionMgr.getAttribute(UserConstants.MODIFY_USER_WRAPPER);

        Hashtable sourceTargetMap = wrapper.getTmpSourceTargetMap();
        boolean isCostingEnabled = false;
        boolean isRevenueEnabled = false;
        try
        {
           SystemConfiguration sc = SystemConfiguration.getInstance();
           isCostingEnabled = sc.getBooleanParameter(
               SystemConfigParamNames.COSTING_ENABLED);
           isRevenueEnabled = sc.getBooleanParameter(
               SystemConfigParamNames.REVENUE_ENABLED);
        }
        catch (Exception e )
        {
            // Problem getting system parameter.
        }

        ResourceBundle bundle = PageHandler.getBundle(session);
        // Get the new/modification switches, and remove them from the session manager.
        Boolean isModify = (Boolean) sessionMgr.getAttribute(Modify2Handler.MOD_ROLES);

        StringBuffer sourceLocaleBuf = new StringBuffer();
        StringBuffer jsBuf = new StringBuffer();
        StringBuffer paddingBuf = new StringBuffer();
        StringBuffer activityBuf = new StringBuffer();
        StringBuffer addAnother = new StringBuffer(); 
        

         // get the UI locale
        Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);

        String sourceLocale = null, targetLocale = null;
        String action = p_theRequest.getParameter(USER_ACTION);
        String warningInfo = (p_theRequest.getParameter("warningInfo") == null? "" :
                             (String)p_theRequest.getParameter("warningInfo"));

        p_theRequest.setAttribute("warningInfo", warningInfo);
        if (action == null)
        {
                // Save data from previous page
            UserUtil.extractUserData(p_theRequest, wrapper, false); 
        }

        // We're in 'new' state; populate as normal.
        boolean modifyOrSetRate = false;
        String selectedCompanyId = UserUtil.getSelectedCompanyId(p_theRequest, wrapper); 
        wrapper.setCurCompanyId(selectedCompanyId);
        
        /*String companyId = null;
        try {
            companyId = CompanyWrapper.getCompanyIdByName(wrapper.getCompanyName());
        } catch (PersistenceException e) {
            throw new EnvoyServletException(e);
        }*/
        //TODO refactor for the similar code
        
        if(WebAppConstants.USER_ACTION_SET_SOURCE.equals(action))
        {
            UserUtil.createActivitySelectionHTML(selectedCompanyId, activityBuf, uiLocale, sourceLocale, targetLocale);
            UserUtil.createLocaleHTML(selectedCompanyId, sourceLocaleBuf, jsBuf, paddingBuf, bundle, 
                                      sourceTargetMap, uiLocale, addAnother, sourceLocale, targetLocale, true);   
        }
        if (action != null && 
             action.intern() == USER_ACTION_NEW_LOCALES)
        {
            //when new, the source/target locale(select list) is "choose..."
            sourceLocale = null;
            targetLocale = null;
            
            UserUtil.createActivitySelectionHTML(selectedCompanyId, activityBuf, uiLocale, sourceLocale, targetLocale);
            UserUtil.createLocaleHTML(selectedCompanyId, sourceLocaleBuf, jsBuf, paddingBuf, bundle, 
                                      sourceTargetMap, uiLocale, addAnother, sourceLocale, targetLocale, true);
            isModify = Boolean.valueOf(false);
        }
        else if (action != null && action.intern() == USER_ACTION_MODIFY_LOCALES) 
        {
            // We're modifying existing activities for a given source/target locale
            // pair; get them off the session manager.
            sourceLocale = (String)p_theRequest.getParameter(Modify2Handler.SOURCE_LOCALE);
            targetLocale = (String)p_theRequest.getParameter(Modify2Handler.TARGET_LOCALE);
            selectedCompanyId = p_theRequest.getParameter(Modify2Handler.COMPANY_ID);
            p_theRequest.setAttribute(Modify2Handler.COMPANY_ID, selectedCompanyId);
            modifyOrSetRate = true;
            isModify = Boolean.valueOf(true);
        }
        else if (action != null && action.intern() == USER_ACTION_SET_RATE)
        {
            sourceLocale = UserUtil.extractSourceLocale(p_theRequest);
            targetLocale = UserUtil.extractTargetLocale(p_theRequest);
            modifyOrSetRate = true;
        }
        if(modifyOrSetRate)
        {
            GlobalSightLocale sourceWrapper = UserHandlerHelper.getLocaleByString(sourceLocale);
            GlobalSightLocale targetWrapper = UserHandlerHelper.getLocaleByString(targetLocale);
            p_theRequest.setAttribute("isModify", isModify);
            p_theRequest.setAttribute("sourceLocale",
	        UserUtil.genLocaleOptionString(sourceWrapper, uiLocale));
            p_theRequest.setAttribute("targetLocale",
            UserUtil.genLocaleOptionString(targetWrapper, uiLocale));
            p_theRequest.setAttribute(Modify2Handler.COMPANY_ID, selectedCompanyId);
            Vector vRoles = wrapper.getRoles(sourceLocale, targetLocale);
            UserUtil.createActivityHTMLForRoles(selectedCompanyId, activityBuf, vRoles, uiLocale, sourceLocale, targetLocale);
            UserUtil.createLocaleHTML(selectedCompanyId, sourceLocaleBuf, jsBuf, paddingBuf, bundle, 
                                      sourceTargetMap, uiLocale, addAnother, sourceLocale, targetLocale, false);
        }
        String allRoleCompanyNames = UserUtil.createCompanyHTML(sessionMgr, wrapper,
                selectedCompanyId).toString();
        //it should be a Label but not SelectList while is modify.
        if (isModify != null && isModify.booleanValue()){
            allRoleCompanyNames = UserUtil.createCompanyHTMLText(CompanyWrapper.getCompanyNameById(selectedCompanyId));
        }

        sessionMgr.setAttribute(UserConstants.ADD_ANOTHER, addAnother);
        
        p_theRequest.setAttribute("allRoleCompanyNames", allRoleCompanyNames);
        p_theRequest.setAttribute("allSourceLocales", sourceLocaleBuf.toString());
        p_theRequest.setAttribute("jsArrays", jsBuf.toString());
        p_theRequest.setAttribute("optionPadding", paddingBuf.toString());
        p_theRequest.setAttribute("activities", activityBuf.toString());
        p_theRequest.setAttribute(SystemConfigParamNames.COSTING_ENABLED,
            new Boolean(isCostingEnabled));
        p_theRequest.setAttribute(SystemConfigParamNames.REVENUE_ENABLED,
            new Boolean(isRevenueEnabled));

        //Call parent invokePageHandler() to set link beans and invoke JSP
        super.invokePageHandler(p_thePageDescriptor, p_theRequest,
            p_theResponse,p_context);

    }
}

