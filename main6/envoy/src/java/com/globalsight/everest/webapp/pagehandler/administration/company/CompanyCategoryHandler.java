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
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.ling.tm2.TmVersion;
import com.globalsight.util.GeneralException;

public class CompanyCategoryHandler extends PageHandler implements CompanyConstants
{
    private static Logger s_logger = Logger
            .getLogger(CompanyCategoryHandler.class.getName());
    
    /**
     * Invokes this PageHandler
     *
     * @param pageDescriptor the page desciptor
     * @param request the original request sent from the browser
     * @param response the original response object
     * @param context context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws ServletException, IOException,
            EnvoyServletException
    {
        HttpSession session = p_request.getSession(false);
        String action = p_request.getParameter("action");
        
        try
        {
            String tmp = "";
            ResourceBundle bundle = PageHandler.getBundle(session);
            String[] keyArray = new String[]
            { "lb_conflicts_glossary_guide", "lb_formatting_error",
                    "lb_mistranslated", "lb_omission_of_text",
                    "lb_spelling_grammar_punctuation_error" };
            List<String> tmpkeyList = Arrays.asList(keyArray);
            List<String> keyList = new ArrayList<String>(tmpkeyList);
            
            if (action.equals("next")) // add a new company
            {
                Company company = storeCompanyInfo(p_request);
                session.setAttribute("tmpcompanyInfo", company);
                p_request.setAttribute("action", "create");
                tmp = "lb_new";
                
                // init the "to" select table, add default value to it
                List<Select> toList = initSelectList(keyList, bundle);
                p_request.setAttribute("toList", toList);
            }
            else if (action.equals("edit")) // edit a company
            {
                SessionManager sessionMgr =
                    (SessionManager)session.getAttribute(SESSION_MANAGER);
                Company company = (Company) sessionMgr.getAttribute(CompanyConstants.COMPANY);
                modifyCompany(company, p_request);
                session.setAttribute("tmpcompanyInfo", company);
                tmp = "lb_edit";
                
                List<String> containedCategories = CompanyWrapper
                        .getCompanyCategoryList(String.valueOf(company.getId()));
                List<Select> toList = initSelectList(containedCategories, bundle);
                p_request.setAttribute("toList", toList);
                p_request.setAttribute("action", "edit");
                keyList.removeAll(containedCategories);
                // init the "from" select table, add default value to it
                List<Select> fromList = initSelectList(keyList, bundle);
                p_request.setAttribute("fromList", fromList);
            }
            
            // sentences on page
            initPage(bundle, p_request, tmp);
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
    
    private void initPage(ResourceBundle bundle, HttpServletRequest p_request, String tmp)
    {
        String lbcancel = bundle.getString("lb_cancel");
        String lbsave = bundle.getString("lb_save");
        String lbPrevious = bundle.getString("lb_previous");
        String lbAdd = bundle.getString("lb_add");
        String title = bundle.getString(tmp) + " "
                + bundle.getString("lb_company") + " - "
                + bundle.getString("lb_company_category");
        String helpFile = bundle.getString("help_companies_main_screen");
        String helpMsg = bundle.getString("helper_text_companies_category");
        String label_new_category = bundle.getString("lb_company_add_category");
        String labelForLeftTable = bundle.getString("lb_company_available_category");
        String labelForRightTable = bundle.getString("lb_company_added_category");
        String alert = bundle.getString("jsmsg_company_category");
        String alert_illegal = bundle.getString("jsmsg_company_illegal_category");
        String alert_same = bundle.getString("jsmsg_company_same_category");
        
        p_request.setAttribute("title", title);
        p_request.setAttribute("cancelButton", lbcancel);
        p_request.setAttribute("saveButton", lbsave);
        p_request.setAttribute("previousButton", lbPrevious);
        p_request.setAttribute("addButton", lbAdd);
        p_request.setAttribute("helpFile", helpFile);
        p_request.setAttribute("helpMsg", helpMsg);
        p_request.setAttribute("label", label_new_category);
        p_request.setAttribute("labelForLeftTable", labelForLeftTable);
        p_request.setAttribute("labelForRightTable", labelForRightTable);
        p_request.setAttribute("alert", alert);
        p_request.setAttribute("alert_illegal", alert_illegal);
        p_request.setAttribute("alert_same", alert_same);
    }
    
    private void modifyCompany(Company company, HttpServletRequest p_request)
    {
        company.setDescription(p_request.getParameter(CompanyConstants.DESC));
        company.setEmail(p_request.getParameter(CompanyConstants.EMAIL));
        company.setSessionTime(p_request.getParameter(CompanyConstants.SESSIONTIME));
        String enableIPFilter = p_request
                .getParameter(CompanyConstants.ENABLE_IP_FILTER);
        String enableTMAccessControl = p_request
                .getParameter(CompanyConstants.ENABLE_TM_ACCESS_CONTROL);
        String enableTBAccessControl = p_request
        .getParameter(CompanyConstants.ENABLE_TB_ACCESS_CONTROL);
        if (enableIPFilter != null && enableIPFilter.equalsIgnoreCase("on"))
        {
            company.setEnableIPFilter(true);
        }
        else
        {
            company.setEnableIPFilter(false);
        }
        if (enableTMAccessControl != null
                && enableTMAccessControl.equalsIgnoreCase("on"))
        {
            company.setEnableTMAccessControl(true);
        }
        else
        {
            company.setEnableTMAccessControl(false);
        }
        if (enableTBAccessControl != null
                && enableTBAccessControl.equalsIgnoreCase("on"))
        {
            company.setEnableTBAccessControl(true);
        }
        else
        {
            company.setEnableTBAccessControl(false);
        }
        String enableSso = p_request
                .getParameter(CompanyConstants.ENABLE_SSO_LOGON);
        company.setEnableSSOLogin(enableSso != null
                && enableSso.equalsIgnoreCase("on"));
        String ssoIdpUrl = p_request.getParameter(CompanyConstants.SSO_IDP_URL);
        company.setSsoIdpUrl(ssoIdpUrl);
        int TM3Version = 2;
        try
        {
            TM3Version = Integer.parseInt(p_request
                    .getParameter(CompanyConstants.TM3_VERSION));
        }
        catch (NumberFormatException nfe)
        {
            TM3Version = 2;
        }
        company.setTmVersion(TmVersion.fromValue(TM3Version));
    }
    
    /** 
     * This method should be in a transacton to make sure each step is successful.
     * Store a company info.
     */
    private Company storeCompanyInfo(HttpServletRequest p_request)
        throws RemoteException, NamingException, GeneralException
    {
        // create the company.
        Company company = new Company();
        company.setName(p_request.getParameter(CompanyConstants.NAME).trim());
        company.setDescription(p_request.getParameter(CompanyConstants.DESC));
        company.setEmail(p_request.getParameter(CompanyConstants.EMAIL));
        company.setSessionTime(p_request.getParameter(CompanyConstants.SESSIONTIME));
        String enableIPFilter = p_request
                .getParameter(CompanyConstants.ENABLE_IP_FILTER);
        String enableTMAccessControl = p_request
                .getParameter(CompanyConstants.ENABLE_TM_ACCESS_CONTROL);
        String enableTBAccessControl = p_request
        .getParameter(CompanyConstants.ENABLE_TB_ACCESS_CONTROL);
        if (enableIPFilter != null && enableIPFilter.equalsIgnoreCase("on"))
        {
            company.setEnableIPFilter(true);
        }
        else
        {
            company.setEnableIPFilter(false);
        }
        if (enableTMAccessControl != null
                && enableTMAccessControl.equalsIgnoreCase("on"))
        {
            company.setEnableTMAccessControl(true);
        }
        else
        {
            company.setEnableTMAccessControl(false);
        }
        if (enableTBAccessControl != null
                && enableTBAccessControl.equalsIgnoreCase("on"))
        {
            company.setEnableTBAccessControl(true);
        }
        else
        {
            company.setEnableTBAccessControl(false);
        }
        
        String enableSso = p_request
                .getParameter(CompanyConstants.ENABLE_SSO_LOGON);
        company.setEnableSSOLogin(enableSso != null
                && enableSso.equalsIgnoreCase("on"));
        String ssoIdpUrl = p_request.getParameter(CompanyConstants.SSO_IDP_URL);
        company.setSsoIdpUrl(ssoIdpUrl);
        int TM3Version = 2;
        try
        {
            TM3Version = Integer.parseInt(p_request
                    .getParameter(CompanyConstants.TM3_VERSION));
        }
        catch (NumberFormatException nfe)
        {
            TM3Version = 2;
        }
        company.setTmVersion(TmVersion.fromValue(TM3Version));
        return company;
    }
    
}
