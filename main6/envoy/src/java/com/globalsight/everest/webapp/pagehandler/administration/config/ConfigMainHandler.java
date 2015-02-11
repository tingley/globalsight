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

package com.globalsight.everest.webapp.pagehandler.administration.config;

// Envoy packages
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.config.SystemParameter;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.login.LoginMainHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.GeneralException;
import com.globalsight.util.ServerUtil;
import com.globalsight.util.modules.Modules;

public class ConfigMainHandler extends PageHandler {
    private static Logger s_logger = Logger
            .getLogger(ConfigMainHandler.class);

    public static final String SLASH = "/";

    private String TWENTYFOUR = "24";
    
    //which will add a suffix(companyName) to super params
    private static String[] companyParams0 = new String[] {
            SystemConfigParamNames.CXE_DOCS_DIR,
            SystemConfigParamNames.FILE_STORAGE_DIR,
            SystemConfigParamNames.CXE_NTCS_DIR,
    };
    //inherit form super params
    private static String[] companyParams = new String[] {
            SystemConfigParamNames.REPORTS_ACTIVITY,
//            SystemConfigParamNames.MSOFFICE_CONV_DIR,
            SystemConfigParamNames.PDF_CONV_DIR,
            SystemConfigParamNames.ADD_LANG_META_TAG,
            SystemConfigParamNames.EXPORT_DIR_NAME_STYLE,
            SystemConfigParamNames.WEB_SERVER_ADMIN_EMAIL_ADDRESS,
            SystemConfigParamNames.AUTO_REPLACE_TERMS,
            SystemConfigParamNames.ANONYMOUS_TERMBASES,
            //SystemConfigParamNames.COSTING_ENABLED,
            //SystemConfigParamNames.REVENUE_ENABLED,
            SystemConfigParamNames.COMMENTS_SORTING,
            SystemConfigParamNames.EMAIL_AUTHENTICATION_ENABLED,
            SystemConfigParamNames.ACCOUNT_USERNAME,
            SystemConfigParamNames.ACCOUNT_PASSWORD,
            SystemConfigParamNames.RECORDS_PER_PAGE_JOBS,
            SystemConfigParamNames.RECORDS_PER_PAGE_TASKS,
            SystemConfigParamNames.MT_SHOW_IN_EDITOR,
            SystemConfigParamNames.HANDLE_IMPORT_FAILURE,
            SystemConfigParamNames.REIMPORT_OPTION,
            SystemConfigParamNames.REIMPORT_DELAY_MILLISECONDS,
            //For this parameter is system level parameter, not company level
            //SystemConfigParamNames.CUSTOMER_INSTALL_KEY,
            SystemConfigParamNames.IS_DELL,
            //SystemConfigParamNames.MY_JOBS_DAYS_RETRIEVED,
            // Add parameters for master layer translate switch
            // move ADOBE_XMP_TRANSLATE, INDESIGN_MASTER_TRANSLATE to filter
            //SystemConfigParamNames.ADOBE_XMP_TRANSLATE,
            //SystemConfigParamNames.INDESIGN_MASTER_TRANSLATE,
            SystemConfigParamNames.PPT_MASTER_TRANSLATE,
            //For this two parameters will change to company level from system
            //level SystemConfigParamNames.OVERDUE_PM_DAY
            //      SystemConfigParamNames.OVERDUE_USER_DAY
            SystemConfigParamNames.OVERDUE_PM_DAY,
            SystemConfigParamNames.OVERDUE_USER_DAY,

            SystemConfigParamNames.REPORTS_CHECK_ACCESS,
            SystemConfigParamNames.EXPORT_REMOVE_INFO_ENABLED,
            SystemConfigParamNames.DUPLICATION_OF_OBJECTS_ALLOWED,
            SystemConfigParamNames.JOB_SEARCH_REPLACE_ALLOWED,
            SystemConfigParamNames.IMPORT_SUGGEST_JOB_NAME,
            SystemConfigParamNames.COSTING_LOCKDOWN,
            SystemConfigParamNames.PER_FILE_CHARGE01_KEY,
            SystemConfigParamNames.PER_FILE_CHARGE02_KEY,
            SystemConfigParamNames.PER_JOB_CHARGE_KEY,
            SystemConfigParamNames.DOWNLOAD_JOB_DELAY_TIME,
            SystemConfigParamNames.TASK_COMPLETE_DELAY_TIME,
            SystemConfigParamNames.ENABLE_SSO,
            SystemConfigParamNames.SERVER_INSTANCE_ID
            };  
    
    //registry key sensitive params
    private static String[] companyParams1 = new String[] {
            SystemConfigParamNames.CORPUS_STORE_NATIVE_FORMAT,
            SystemConfigParamNames.CORPUS_SHOW_ALL_TMS_TO_LPS,
            SystemConfigParamNames.PIVOT_CURRENCY,
            SystemConfigParamNames.REVENUE_ENABLED };
    
    private static List<String> ignoredSysParams = null;

    //used when creat a company,get all params needing to creat
    public static String[] getParams() {
        int len = companyParams.length + companyParams1.length;
        String[] params = new String[len];
        System.arraycopy(companyParams, 0, params, 0, companyParams.length);
        System.arraycopy(companyParams1, 0, params, companyParams.length,
                companyParams1.length);
        return params;
    }
    public static String[] getCompanySuffixedParams() {
        return companyParams0;
    }
    public static String[] getCompanyParams(){
        return null;
    }
    
    // Gets the ignored system parameter list, which only exist in super company.
    public static List<String> getIgnoredSysParams()
    {
        if (ignoredSysParams == null)
        {
            ignoredSysParams = new ArrayList<String>();
            ignoredSysParams.add(SystemConfigParamNames.SERVER_INSTANCE_ID);
        }

        return ignoredSysParams;
    }
    
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws ServletException, IOException,
            EnvoyServletException {
        
        // turn off cache.  do both.  "pragma" for the older browsers.
        p_response.setHeader("Pragma", "no-cache"); //HTTP 1.0
        p_response.setHeader("Cache-Control", "no-cache"); //HTTP 1.1
        p_response.addHeader("Cache-Control", "no-store"); // tell proxy not to cache
        p_response.addHeader("Cache-Control", "max-age=0"); // stale right away
    	
		HttpSession session = p_request.getSession(false);
		// gbs-1389: restrict direct access to system parameter page without the
		// permission.
		PermissionSet userPerms = (PermissionSet) session
				.getAttribute(WebAppConstants.PERMISSIONS);
		if (!userPerms.getPermissionFor(Permission.SYSTEM_PARAMS)) 
		{
			p_response.sendRedirect(p_request.getContextPath());
			return;
		}
		
		// Reset Server Instance ID value if match the condition.
		ServerUtil.getServerInstanceID();
		
        //save system parameters first into db
        if (p_request.getParameter(companyParams[0]) != null) {
            setSystemParameters(p_request);
            LoginMainHandler.m_enableSSO = null;
        }
        //then get system parameters from db forwarding to web page
        getSystemParameters(p_request);
        super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                p_context);
    }

    private void getSystemParameters(HttpServletRequest p_request)
            throws EnvoyServletException {
        for (int i = 0; i < companyParams.length; i++) {
            p_request.setAttribute(companyParams[i], getSystemParameter(
                    companyParams[i]).getValue());

        }
        getSystemParameters1(p_request);
    }

    private void setSystemParameters(HttpServletRequest p_request)
            throws EnvoyServletException {
        for (int i = 0; i < companyParams.length; i++) {
            String paramStr = companyParams[i];
            SystemParameter sysParam = getSystemParameter(paramStr);
            if (p_request.getParameter(paramStr) != null)
            {
            	 sysParam.setValue(p_request.getParameter(paramStr));
            	 //update the system parameter to 'system_parameter' table
                 updateSystemParameter(sysParam);
            }           
        }
        setSystemParameters1(p_request);
        
    }

    private void getSystemParameters1(HttpServletRequest p_request) {
        if (Modules.isCorpusInstalled()) {
            p_request.setAttribute(
                    SystemConfigParamNames.CORPUS_STORE_NATIVE_FORMAT,
                    getSystemParameter(
                            SystemConfigParamNames.CORPUS_STORE_NATIVE_FORMAT)
                            .getValue());
            p_request.setAttribute(
                    SystemConfigParamNames.CORPUS_SHOW_ALL_TMS_TO_LPS,
                    getSystemParameter(
                            SystemConfigParamNames.CORPUS_SHOW_ALL_TMS_TO_LPS)
                            .getValue());
        }
//
//        String analyzeInterval = getSystemParameter(
//                SystemConfigParamNames.ANALYZE_SCRIPT_INTERVAL).getValue();
//        analyzeInterval = getNumerator(analyzeInterval);
//        p_request.setAttribute(SystemConfigParamNames.ANALYZE_SCRIPT_INTERVAL,
//                analyzeInterval);

        boolean isDescendingComments = Boolean.valueOf(
                getSystemParameter(SystemConfigParamNames.COMMENTS_SORTING)
                        .getValue()).booleanValue();
        String sortComments = (String) (getSystemParameter(SystemConfigParamNames.COMMENTS_SORTING)
                .getValue());

        if ("asc".equals(sortComments)) {
            p_request.setAttribute(SystemConfigParamNames.COMMENTS_SORTING,
                    "asc");
        } else if ("desc".equals(sortComments)) {
            p_request.setAttribute(SystemConfigParamNames.COMMENTS_SORTING,
                    "desc");
        } else {
            p_request.setAttribute(SystemConfigParamNames.COMMENTS_SORTING,
                    "default");
        }
        getCosting(p_request);
    }

    private void setSystemParameters1(HttpServletRequest p_request) {
        //      Only needed if costing is enabled
        SystemParameter currencySP = null;
        boolean isCostingEnabled = Boolean.valueOf(
                getSystemParameter(SystemConfigParamNames.COSTING_ENABLED)
                        .getValue()).booleanValue();
        SystemParameter jreSP = getSystemParameter(SystemConfigParamNames.REVENUE_ENABLED);
        SystemParameter corpusSP = getSystemParameter(SystemConfigParamNames.CORPUS_STORE_NATIVE_FORMAT);
        SystemParameter corpusTMP = getSystemParameter(SystemConfigParamNames.CORPUS_SHOW_ALL_TMS_TO_LPS);

        if (isCostingEnabled) {
            currencySP = getSystemParameter(SystemConfigParamNames.PIVOT_CURRENCY);
            currencySP.setValue(p_request
                    .getParameter(SystemConfigParamNames.PIVOT_CURRENCY));
            jreSP.setValue(p_request
                    .getParameter(SystemConfigParamNames.REVENUE_ENABLED));
        }

        if (Modules.isCorpusInstalled()) {
            corpusSP
                    .setValue(p_request
                            .getParameter(SystemConfigParamNames.CORPUS_STORE_NATIVE_FORMAT));
            corpusTMP
                    .setValue(p_request
                            .getParameter(SystemConfigParamNames.CORPUS_SHOW_ALL_TMS_TO_LPS));
        }

        if (Modules.isCorpusInstalled()) {
            updateSystemParameter(corpusSP);
            updateSystemParameter(corpusTMP);
        }

        if (isCostingEnabled) {
            updateSystemParameter(currencySP);
            updateSystemParameter(jreSP);
        }
    }

    private SystemParameter getSystemParameter(String p_name)
            throws EnvoyServletException {
        try {
            return ServerProxy.getSystemParameterPersistenceManager()
                    .getSystemParameter(p_name);
        } catch (RemoteException re) {
            throw new EnvoyServletException(GeneralException.EX_REMOTE, re);
        }
    }

    private SystemParameter updateSystemParameter(
            SystemParameter p_systemParameter) throws EnvoyServletException {
        try {
            return ServerProxy.getSystemParameterPersistenceManager()
                    .updateSystemParameter(p_systemParameter);
        } catch (RemoteException re) {
            throw new EnvoyServletException(GeneralException.EX_REMOTE, re);
        }
    }

    /**
     * Get costing information from the Request.
     */
    private void getCosting(HttpServletRequest p_request)
            throws EnvoyServletException {
        // if costing is enabled
        boolean isCostingEnabled = Boolean.valueOf(
                getSystemParameter(SystemConfigParamNames.COSTING_ENABLED)
                        .getValue()).booleanValue();
        boolean isRevenueEnabled = Boolean.valueOf(
                getSystemParameter(SystemConfigParamNames.REVENUE_ENABLED)
                        .getValue()).booleanValue();
        if (isCostingEnabled) {
            p_request.setAttribute(SystemConfigParamNames.COSTING_ENABLED,
                    "true");
            if (isRevenueEnabled) {
                p_request.setAttribute(SystemConfigParamNames.REVENUE_ENABLED,
                        "true");
            }
            // get the default currency
            String defaultCurrencySymbol = getSystemParameter(
                    SystemConfigParamNames.PIVOT_CURRENCY).getValue();

            p_request.setAttribute(SystemConfigParamNames.PIVOT_CURRENCY,
                    defaultCurrencySymbol);
        } else // costing isn't enabled
        {
            p_request.setAttribute(SystemConfigParamNames.COSTING_ENABLED,
                    "false");
            p_request.setAttribute(SystemConfigParamNames.REVENUE_ENABLED,
                    "false");
        }
    }

    private String convertAmPmToTwentyFourFormat(String p_analyzeInterval) {
        String analyzeInterval = null;

        int val = 0;
        try {
            val = Integer.parseInt(p_analyzeInterval);
        } catch (NumberFormatException e) {
        }

        StringBuffer sb = new StringBuffer(new Integer(val).toString());
        analyzeInterval = sb.append(SLASH).append(TWENTYFOUR).toString();

        return analyzeInterval;
    }
}