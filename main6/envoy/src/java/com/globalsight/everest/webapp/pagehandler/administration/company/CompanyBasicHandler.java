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
package com.globalsight.everest.webapp.pagehandler.administration.company;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.everest.company.Company;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.GeneralException;

public class CompanyBasicHandler extends PageHandler implements
        CompanyConstants
{
    private static Logger s_logger = Logger.getLogger(CompanyBasicHandler.class
            .getName());

    /**
     * Invokes this PageHandler
     * 
     * @param pageDescriptor
     *            the page desciptor
     * @param request
     *            the original request sent from the browser
     * @param response
     *            the original response object
     * @param context
     *            context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws ServletException, IOException,
            EnvoyServletException
    {
        HttpSession session = p_request.getSession(false);
        String action = p_request.getParameter("action");
        p_request.setAttribute(SystemConfigParamNames.ENABLE_SSO,
                getSSOEnable());
        p_request.setAttribute(SystemConfigParamNames.SYSTEM_NOTIFICATION_ENABLED, 
                ServerProxy.getMailer().isSystemNotificationEnabled());

        PermissionSet userPerms = (PermissionSet) session
                .getAttribute(WebAppConstants.PERMISSIONS);
        ResourceBundle bundle = PageHandler.getBundle(session);
        String[] keyArray = new String[]
        { "lb_conflicts_glossary_guide", "lb_formatting_error",
                "lb_mistranslated", "lb_omission_of_text",
                "lb_spelling_grammar_punctuation_error" };
        String[] scorecardKeyArray = new String[]
        {"lb_spelling_grammar", "lb_consistency",
                "lb_style", "lb_terminology",};
        String[] qualityKeyArray = new String[]{"lb_good","lb_acceptable","lb_poor"};
        String[] marketKeyArray = new String[]{"lb_suitable_fluent", "lb_literal_at_times","lb_unsuitable"};
        List<String> tmpkeyList = Arrays.asList(keyArray);
        List<String> tempScorecardKeyList = Arrays.asList(scorecardKeyArray);
        List<String> tmpQualityKeyList = Arrays.asList(qualityKeyArray);
        List<String> tmpMarketKeyList = Arrays.asList(marketKeyArray);
        List<String> keyList = new ArrayList<String>(tmpkeyList);
        List<String> sorcecardKeyList  = new ArrayList<String>(tempScorecardKeyList);
        List<String> qualityKeyList = new ArrayList<String>(tmpQualityKeyList);
        List<String> marketKeyList  = new ArrayList<String>(tmpMarketKeyList);
        try
        {
            if (action.equals(CompanyConstants.CREATE))
            {
                // gbs-1389: restrict direct access to create company without
                // create company permission.
                if (!userPerms.getPermissionFor(Permission.COMPANY_NEW))
                {
                    if (userPerms.getPermissionFor(Permission.COMPANY_VIEW))
                    {
                        p_response.sendRedirect("/globalsight/ControlServlet?activityName=companies");
                    }
                    else
                    {
                        p_response.sendRedirect(p_request.getContextPath());
                    }
                    return;
                }
                setDateForNewCompany(p_request);
                
                // init the "to" select table, add default value to it
                List<Select> toList = initSelectList(keyList, bundle);
                List<Select> scorecardToList = initSelectList(sorcecardKeyList, bundle);
                List<Select> qualityToList = initSelectList(qualityKeyList, bundle);
                List<Select> marketToList = initSelectList(marketKeyList, bundle);
                p_request.setAttribute("toList", toList);
                p_request.setAttribute("scorecardToList", scorecardToList);
                p_request.setAttribute("qualityToList", qualityToList);
                p_request.setAttribute("marketToList", marketToList);
                
                p_request.setAttribute("incontext_review_key_indd", "false");
                p_request.setAttribute("incontext_review_key_office", "false");
                p_request.setAttribute("incontext_review_key_xml", "false");
            }
            else if (action.equals(CompanyConstants.EDIT))
            {
                // gbs-1389: restrict direct access to edit company without
                // edit company permission.
                if (!userPerms.getPermissionFor(Permission.COMPANY_EDIT))
                {
                    if (userPerms.getPermissionFor(Permission.COMPANY_VIEW))
                    {
                        p_response.sendRedirect("/globalsight/ControlServlet?activityName=companies");
                    }
                    else
                    {
                        p_response.sendRedirect(p_request.getContextPath());
                    }
                    return;
                }
                String companyID = (String) p_request.getParameter("id");
                if (companyID != null)
                {
                    SessionManager sessionMgr = (SessionManager) session
                            .getAttribute(SESSION_MANAGER);
                    Company company = (Company) ServerProxy.getJobHandler()
                            .getCompanyById(Long.parseLong(companyID));
                    sessionMgr.setAttribute(CompanyConstants.COMPANY, company);
                    
                    List<String> containedCategories = CompanyWrapper
                            .getCompanyCategoryList(companyID);
                    List<String> containedScorecardCategories = CompanyWrapper
                    		.getCompanyScorecardCategoryList(companyID);
                    List<String> containedQualityCategories = CompanyWrapper.getCompanyQualityCategoryList(companyID);
                    List<String> containedMarketCategories = CompanyWrapper.getCompanyMarketCategoryList(companyID);
                    List<Select> toList = initSelectList(containedCategories, bundle);
                    List<Select> scorecardToList = initSelectList(containedScorecardCategories, bundle);
                    List<Select> qualityToList = initSelectList(containedQualityCategories, bundle);
                    List<Select> marketToList = initSelectList(containedMarketCategories, bundle);
                    
                    p_request.setAttribute("toList", toList);
                    p_request.setAttribute("scorecardToList", scorecardToList);
                    p_request.setAttribute("qualityToList", qualityToList);
                    p_request.setAttribute("marketToList", marketToList);
                    
                    p_request.setAttribute("action", "edit");
                    
                    List<String> availableCategories = CompanyWrapper
                            .getCompanyCategoryAvailList(companyID);
                    List<String> availableScorecardCategories = CompanyWrapper
                            .getCompanyScorecardCategoryAvailList(companyID);
                    List<String> availableQualityCategories = CompanyWrapper
                            .getCompanyQualityCategoryAvailList(companyID);
                    List<String> availableMarketCategories = CompanyWrapper
                            .getCompanyMarketCategoryAvailList(companyID);
                    // init the "from" select table, add default value to it
                    List<Select> fromList = initSelectList(availableCategories, bundle);
                    List<Select> scorecardFromList = initSelectList(availableScorecardCategories, bundle);
                    List<Select> qualityFromList = initSelectList(availableQualityCategories, bundle);
                    List<Select> marketFromList = initSelectList(availableMarketCategories, bundle);
                    p_request.setAttribute("fromList", fromList);
                    p_request.setAttribute("scorecardFromList", scorecardFromList);
                    p_request.setAttribute("qualityFromList", qualityFromList);
                    p_request.setAttribute("marketFromList", marketFromList);
                    
                    p_request.setAttribute("incontext_review_key_indd", getInCtxRvEnableIndd(companyID));
                    p_request.setAttribute("incontext_review_key_office", getInCtxRvEnableOffice(companyID));
                    p_request.setAttribute("incontext_review_key_xml", getInCtxRvEnableXML(companyID));
                }
                p_request.setAttribute("edit", "true");
            }
            
            // sentences on page
            initPage(bundle, p_request);
        }
        catch (NamingException ne)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL,
                    ne);
        }
        catch (RemoteException re)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL,
                    re);
        }
        catch (GeneralException ge)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL,
                    ge);
        }
        super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                p_context);
    }

    /**
     * Get list of all company names. Needed in jsp to determine duplicate
     * names.
     */
    private void setDateForNewCompany(HttpServletRequest p_request)
            throws RemoteException, NamingException, GeneralException
    {
        ArrayList list = (ArrayList) ServerProxy.getJobHandler()
                .getAllCompanies();
        ArrayList names = new ArrayList();
        if (list != null)
        {
            for (int i = 0; i < list.size(); i++)
            {
                Company company = (Company) list.get(i);
                names.add(company.getName());
            }
        }
        p_request.setAttribute(CompanyConstants.NAMES, names);

        String defEmail = ServerProxy.getSystemParameterPersistenceManager()
                .getAdminSystemParameter(SystemConfigParamNames.ADMIN_EMAIL)
                .getValue();
        p_request.setAttribute(CompanyConstants.EMAIL, defEmail);
    }

    private String getSSOEnable()
    {
        String temp = "false";
        try
        {
            temp = ServerProxy.getSystemParameterPersistenceManager()
                    .getAdminSystemParameter(SystemConfigParamNames.ENABLE_SSO)
                    .getValue();
        }
        catch (Exception e)
        {
            s_logger.error("There is an error when getting ENABLE_SSO from System Parameter");
        }
        return temp;
    }
    
    private String getInCtxRvEnableIndd(String companyId)
    {
        String temp = "false";
        try
        {
            temp = ServerProxy.getSystemParameterPersistenceManager()
                    .getSystemParameter(SystemConfigParamNames.INCTXRV_ENABLE_INDD,
                            companyId)
                    .getValue();
        }
        catch (Exception e)
        {
            // ignore
        }
        return temp;
    }
    
    private String getInCtxRvEnableOffice(String companyId)
    {
        String temp = "false";
        try
        {
            temp = ServerProxy.getSystemParameterPersistenceManager()
                    .getSystemParameter(SystemConfigParamNames.INCTXRV_ENABLE_OFFICE,
                            companyId)
                    .getValue();
        }
        catch (Exception e)
        {
            // ignore
        }
        return temp;
    }
    
    private String getInCtxRvEnableXML(String companyId)
    {
        String temp = "false";
        try
        {
            temp = ServerProxy.getSystemParameterPersistenceManager()
                    .getSystemParameter(
                            SystemConfigParamNames.INCTXRV_ENABLE_XML,
                            companyId)
                    .getValue();
        }
        catch (Exception e)
        {
            // ignore
        }
        return temp;
    }
    
    private void initPage(ResourceBundle bundle, HttpServletRequest p_request)
    {
        String lbcancel = bundle.getString("lb_cancel");
        String lbsave = bundle.getString("lb_save");
        String lbPrevious = bundle.getString("lb_previous");
        String lbAdd = bundle.getString("lb_add");
        String helpFile = bundle.getString("help_companies_main_screen");
        String helpMsg = bundle.getString("helper_text_companies_category");
        String scorecardHelpMsg = bundle.getString("helper_text_reviewer_scorecard_category");
        String label_new_category = bundle.getString("lb_company_add_category");
        String labelForLeftTable = bundle.getString("lb_company_available_category");
        String labelForRightTable = bundle.getString("lb_company_added_category");
        String alert = bundle.getString("jsmsg_company_category");
        String alert_illegal = bundle.getString("jsmsg_company_illegal_category");
        String alert_same = bundle.getString("jsmsg_company_same_category");
        String qualityHelpMsg = bundle.getString("helper_text_reviewer_quality_category");
        String marketHelpMsg = bundle.getString("helper_text_reviewer_market_category");
        
        p_request.setAttribute("cancelButton", lbcancel);
        p_request.setAttribute("saveButton", lbsave);
        p_request.setAttribute("previousButton", lbPrevious);
        p_request.setAttribute("addButton", lbAdd);
        p_request.setAttribute("helpFile", helpFile);
        p_request.setAttribute("helpMsg", helpMsg);
        p_request.setAttribute("scorecardHelpMsg", scorecardHelpMsg);
        p_request.setAttribute("qualityHelpMsg", qualityHelpMsg);
        p_request.setAttribute("marketHelpMsg", marketHelpMsg);
        p_request.setAttribute("label", label_new_category);
        p_request.setAttribute("labelForLeftTable", labelForLeftTable);
        p_request.setAttribute("labelForRightTable", labelForRightTable);
        p_request.setAttribute("alert", alert);
        p_request.setAttribute("alert_illegal", alert_illegal);
        p_request.setAttribute("alert_same", alert_same);
    }
    
    /**
     * Get categoryList according to bundle list
     * @param keyList
     * @param bundle
     * @return
     */
    private List<Select> initSelectList(List<String> keyList, ResourceBundle bundle)
    {
        List<Select> list = new ArrayList<Select>();
        for (String key : keyList)
        {
            String valueOfSelect = "";
            try
            {
                valueOfSelect = bundle.getString(key);
            }
            catch (MissingResourceException e)
            {
                valueOfSelect = key;
            }
            // we should put value both at key and value places
            Select option = new Select(key, valueOfSelect);
            list.add(option);
        }
        return list;
    }
}
