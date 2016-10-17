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

import com.globalsight.everest.category.CategoryType;
import com.globalsight.everest.category.CommonCategory;
import com.globalsight.everest.category.DefaultCategory;
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
        DefaultCategory dc = (DefaultCategory) session.getAttribute("defaultCategories");
        if (dc == null)
        {
            // init default categories
            dc = new DefaultCategory(bundle);
            dc.init();
            session.setAttribute("defaultCategories", dc);
        }

        p_request.setAttribute("defaultSegmentCommentCategories",
                dc.getDefaultCategoriesAsString(CategoryType.SegmentComment));
        p_request.setAttribute("defaultScorecardCategories",
                dc.getDefaultCategoriesAsString(CategoryType.ScoreCard));
        p_request.setAttribute("defaultMarketCategories",
                dc.getDefaultCategoriesAsString(CategoryType.Market));
        p_request.setAttribute("defaultQualityCategories",
                dc.getDefaultCategoriesAsString(CategoryType.Quality));
        p_request.setAttribute("defaultFluencyCategories",
                dc.getDefaultCategoriesAsString(CategoryType.Fluency));
        p_request.setAttribute("defaultAdequacyCategories",
                dc.getDefaultCategoriesAsString(CategoryType.Adequacy));
        p_request.setAttribute("defaultSeverityCategories",
                dc.getDefaultCategoriesAsString(CategoryType.Severity));

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
                
                p_request.setAttribute("segmentCommentCategories",
                        dc.getAvailableSegmentCommentCategories());
                p_request.setAttribute("scorecardCategories", dc.getAvailableScorecardCategories());
                p_request.setAttribute("qualityCategories", dc.getAvailableQualityCategories());
                p_request.setAttribute("marketCategories", dc.getAvailableMarketCategories());
                p_request.setAttribute("fluencyCategories", dc.getAvailableFluencyCategories());
                p_request.setAttribute("adequacyCategories", dc.getAvailableAdequacyCategories());
                p_request.setAttribute("severityCategories", dc.getAvailableSeverityCategories());
                
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
                    
                    p_request.setAttribute(
                            "allSegmentCommentCategories",
                            showCategories(bundle, CompanyWrapper.getCompanyCategories(companyID,
                                    CategoryType.SegmentComment, false, false)));
                    p_request.setAttribute(
                            "segmentCommentCategories",
                            showCategories(bundle, CompanyWrapper.getCompanyCategories(companyID,
                                    CategoryType.SegmentComment, true, false)));
                    p_request.setAttribute(
                            "allScorecardCategories",
                            showCategories(bundle, CompanyWrapper.getCompanyCategories(companyID,
                                    CategoryType.ScoreCard, false, false)));
                    p_request.setAttribute(
                            "scorecardCategories",
                            showCategories(bundle, CompanyWrapper.getCompanyCategories(companyID,
                                    CategoryType.ScoreCard, true, false)));
                    p_request.setAttribute(
                            "allQualityCategories",
                            showCategories(bundle, CompanyWrapper.getCompanyCategories(companyID,
                                    CategoryType.Quality, false, false)));
                    p_request.setAttribute(
                            "qualityCategories",
                            showCategories(bundle, CompanyWrapper.getCompanyCategories(companyID,
                                    CategoryType.Quality, true, false)));
                    p_request.setAttribute(
                            "allMarketCategories",
                            showCategories(bundle, CompanyWrapper.getCompanyCategories(companyID,
                                    CategoryType.Market, false, false)));
                    p_request.setAttribute(
                            "marketCategories",
                            showCategories(bundle, CompanyWrapper.getCompanyCategories(companyID,
                                    CategoryType.Market, true, false)));
                    p_request.setAttribute(
                            "allFluencyCategories",
                            showCategories(bundle, CompanyWrapper.getCompanyCategories(companyID,
                                    CategoryType.Fluency, false, false)));
                    p_request.setAttribute(
                            "fluencyCategories",
                            showCategories(bundle, CompanyWrapper.getCompanyCategories(companyID,
                                    CategoryType.Fluency, true, false)));
                    p_request.setAttribute(
                            "allAdequacyCategories",
                            showCategories(bundle, CompanyWrapper.getCompanyCategories(companyID,
                                    CategoryType.Adequacy, false, false)));
                    p_request.setAttribute(
                            "adequacyCategories",
                            showCategories(bundle, CompanyWrapper.getCompanyCategories(companyID,
                                    CategoryType.Adequacy, true, false)));
                    p_request.setAttribute(
                            "allSeverityCategories",
                            showCategories(bundle, CompanyWrapper.getCompanyCategories(companyID,
                                    CategoryType.Severity, false, false)));
                    p_request.setAttribute(
                            "severityCategories",
                            showCategories(bundle, CompanyWrapper.getCompanyCategories(companyID,
                                    CategoryType.Severity, true, false)));
                    
                    p_request.setAttribute("action", "edit");
                    
                    p_request.setAttribute("incontext_review_key_indd", getInCtxRvEnableIndd(companyID));
                    p_request.setAttribute("incontext_review_key_office", getInCtxRvEnableOffice(companyID));
                    p_request.setAttribute("incontext_review_key_xml", getInCtxRvEnableXML(companyID));
                    p_request.setAttribute("incontext_review_key_html", getInCtxRvEnableHTML(companyID));
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

    private List<CommonCategory> showCategories(ResourceBundle bundle, List<CommonCategory> data)
    {
        List<CommonCategory> categories = new ArrayList<CommonCategory>();
        CommonCategory category = null;
        String showName = "";
        for (CommonCategory commonCategory : data)
        {
            category = new CommonCategory();
            category.setName(commonCategory.getName());
            category.setId(commonCategory.getId());
            category.setCompanyId(commonCategory.getCompanyId());
            category.setIsActive(commonCategory.isActive());
            category.setIsAvailable(commonCategory.isAvailable());
            try
            {
                showName = bundle.getString(category.getName());
                category.setMemo(showName);
            }
            catch (Exception e)
            {
                category.setMemo(category.getName());
            }

            categories.add(category);
        }
        return categories;
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
    
    private String getInCtxRvEnableHTML(String companyId)
    {
        String temp = "false";
        try
        {
            temp = ServerProxy.getSystemParameterPersistenceManager()
                    .getSystemParameter(
                            SystemConfigParamNames.INCTXRV_ENABLE_HTML,
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
        p_request.setAttribute("fluencyHelpMsg",
                bundle.getString("helper_text_dqf_fluency_category"));
        p_request.setAttribute("adequacyHelpMsg",
                bundle.getString("helper_text_dqf_adequacy_category"));
        p_request.setAttribute("severityHelpMsg",
                bundle.getString("helper_text_dqf_severity_category"));
        p_request.setAttribute("defaultSettingHelpMsg",
                bundle.getString("helper_text_dqf_define_default"));
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
