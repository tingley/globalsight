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
package com.globalsight.everest.webapp.pagehandler.administration.mtprofile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.projecthandler.MachineTranslationExtentInfo;
import com.globalsight.everest.projecthandler.MachineTranslationProfile;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.MTProfileComparator;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.users.ExportUtil;
import com.globalsight.everest.webapp.tags.TableConstants;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.StringUtil;

/**
 * mtProfileHandler is the page handler responsible for displaying a list of tm
 * profiles and perform actions supported by the UI (JSP).
 */

public class MTProfileHandler extends PageHandler
{

    // non user related state
    private int num_per_page; // number of tm profiles per page
    private static final Logger CATEGORY = Logger
            .getLogger(MTProfileHandler.class);
    private static final String COMMA = ",";
    public static final String MTPS_LIST = "mtProfiles";
    public static final String MTP_KEY = "mtProfile";
    private final static String NEW_LINE = "\r\n";

    public MTProfileHandler()
    {
        try
        {
            num_per_page = SystemConfiguration.getInstance().getIntParameter(
                    SystemConfigParamNames.NUM_TMPROFILES_PER_PAGE);
        }
        catch (Exception e)
        {
            num_per_page = 10;
        }
    }

    /**
     * Invokes this PageHandler
     * 
     * @param p_pageDescriptor
     *            the page desciptor
     * @param p_request
     *            the original request sent from the browser
     * @param p_response
     *            the original response object
     * @param p_context
     *            context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws ServletException, IOException,
            EnvoyServletException
    {
        HttpSession sess = p_request.getSession(false);
        SessionManager sessionManager = (SessionManager) sess
                .getAttribute(SESSION_MANAGER);
        sessionManager.removeElement(MTProfileConstants.MT_PROFILE);
        String action = (String) p_request
                .getParameter(MTProfileConstants.ACTION);
        if (MTProfileConstants.REMOVE_ACTION.equals(action))
        {
            MachineTranslationProfile mtProfile = getMTProfile(p_request,
                    p_response);
            if (null == mtProfile)
                return;
            removeMT(p_request, mtProfile);
        }
        if (MTProfileConstants.MT_ACTIVE_ACTION.equals(action))
        {
            MachineTranslationProfile mtProfile = getMTProfile(p_request,
                    p_response);
            if (null == mtProfile)
                return;
            active(p_request, mtProfile);
        }
        if (MTProfileConstants.EXPORT_ACTION.equals(action))
        {
            exportMTP(p_request, p_response, sessionManager);
            return;
        }

        StringBuffer condition = new StringBuffer();
        String[][] array = new String[][]
        {
        { MTProfileConstants.FILTER_NAME, "mtp.MT_PROFILE_NAME" } };

        for (int i = 0; i < array.length; i++)
        {
            makeCondition(p_request, sessionManager, condition, array[i][0],
                    array[i][1]);
        }

        selectTMProfilesForDisplay(p_request, sess, condition.toString());

        // Call parent invokePageHandler() to set link beans and invoke JSP
        super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                p_context);
    }

    private void exportMTP(HttpServletRequest request,
            HttpServletResponse response, SessionManager sessionManager)
    {
        User user = (User) sessionManager.getAttribute(WebAppConstants.USER);
        String currentId = CompanyThreadLocal.getInstance().getValue();
        long companyId = Long.parseLong(currentId);
        // create property file
        File propertyFile = createPropertyFile(user.getUserName(), companyId);
        // get property file name
        String fileName = propertyFile.getName();

        String id = (String) request.getParameter("id");
        String[] idsArr = null;
        if (id != null && !id.equals(""))
        {
            MachineTranslationProfile mtp = null;
            idsArr = id.split(",");
            if (idsArr != null)
            {
                for (int n = 0; n < idsArr.length; n++)
                {
                    mtp = MTProfileHandlerHelper.getMTProfileById(idsArr[n]);
                    propertiesInputMTP(propertyFile, mtp);
                }
                ExportUtil.writeToResponse(response, propertyFile, fileName);
            }
        }
    }

    private void propertiesInputMTP(File propertyFile,
            MachineTranslationProfile mtp)
    {
        if (mtp == null)
            return;
        StringBuffer buffer = new StringBuffer();
        buffer.append("##MachineTranslationProfile.")
                .append(mtp.getCompanyid()).append(".").append(mtp.getId())
                .append(".begin").append(NEW_LINE);
        buffer.append("MachineTranslationProfile.").append(mtp.getId())
                .append(".ID=").append(mtp.getId()).append(NEW_LINE);
        buffer.append("MachineTranslationProfile.").append(mtp.getId())
                .append(".MT_PROFILE_NAME=").append(mtp.getMtProfileName())
                .append(NEW_LINE);
        buffer.append("MachineTranslationProfile.").append(mtp.getId())
                .append(".MT_ENGINE=").append(mtp.getMtEngine())
                .append(NEW_LINE);
        buffer.append("MachineTranslationProfile.").append(mtp.getId())
                .append(".DESCRIPTION=").append(mtp.getDescription())
                .append(NEW_LINE);
        buffer.append("MachineTranslationProfile.").append(mtp.getId())
                .append(".MT_CONFIDENCE_SCORE=")
                .append(mtp.getMtConfidenceScore()).append(NEW_LINE);
        buffer.append("MachineTranslationProfile.").append(mtp.getId())
                .append(".URL=").append(mtp.getUrl()).append(NEW_LINE);
        buffer.append("MachineTranslationProfile.").append(mtp.getId())
                .append(".PORT=").append(mtp.getPort()).append(NEW_LINE);
        buffer.append("MachineTranslationProfile.").append(mtp.getId())
                .append(".USERNAME=").append(mtp.getUsername())
                .append(NEW_LINE);
        if (mtp.getPassword() == null)
        {
            buffer.append("MachineTranslationProfile.").append(mtp.getId())
                    .append(".PASSWORD=").append("").append(NEW_LINE);
        }
        else
        {
            buffer.append("MachineTranslationProfile.").append(mtp.getId())
                    .append(".PASSWORD=").append(mtp.getPassword())
                    .append(NEW_LINE);
        }
        buffer.append("MachineTranslationProfile.").append(mtp.getId())
                .append(".CATEGORY=").append(mtp.getCategory())
                .append(NEW_LINE);
        buffer.append("MachineTranslationProfile.").append(mtp.getId())
                .append(".ACCOUNTINFO=").append(mtp.getAccountinfo())
                .append(NEW_LINE);
        buffer.append("MachineTranslationProfile.").append(mtp.getId())
                .append(".COMPANY_ID=").append(mtp.getCompanyid())
                .append(NEW_LINE);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        buffer.append("MachineTranslationProfile.").append(mtp.getId())
                .append(".TIMESTAMP=").append(df.format(mtp.getTimestamp()))
                .append(NEW_LINE);
        buffer.append("MachineTranslationProfile.").append(mtp.getId())
                .append(".SHOW_IN_EDITOR=").append(mtp.isShowInEditor())
                .append(NEW_LINE);
        buffer.append("MachineTranslationProfile.").append(mtp.getId())
                .append(".INCLUDE_MT_IDENTIFIERS=")
                .append(mtp.isIncludeMTIdentifiers()).append(NEW_LINE);
        buffer.append("MachineTranslationProfile.").append(mtp.getId())
                .append(".MT_IDENTIFIER_LEADING=")
                .append(mtp.getMtIdentifierLeading()).append(NEW_LINE);
        buffer.append("MachineTranslationProfile.").append(mtp.getId())
                .append(".MT_IDENTIFIER_TRAILING=")
                .append(mtp.getMtIdentifierTrailing()).append(NEW_LINE);
        buffer.append("MachineTranslationProfile.").append(mtp.getId())
                .append(".IS_ACTIVE=").append(mtp.isActive()).append(NEW_LINE);
        buffer.append("MachineTranslationProfile.").append(mtp.getId())
                .append(".EXTENT_JSON_INFO=").append(mtp.getJsonInfo())
                .append(NEW_LINE);
        buffer.append("##MachineTranslationProfile.")
                .append(mtp.getCompanyid()).append(".").append(mtp.getId())
                .append(".end").append(NEW_LINE).append(NEW_LINE);
        writeToFile(propertyFile, buffer.toString().getBytes());

        String companyId = String.valueOf(mtp.getCompanyid());
        Set<MachineTranslationExtentInfo> mteInfoSet = mtp.getExInfo();
        if (mteInfoSet != null && mteInfoSet.size() > 0)
        {
            for (MachineTranslationExtentInfo mteInfo : mteInfoSet)
            {
                propertiesInputMTEInfo(propertyFile, mteInfo, companyId);
            }
        }
    }

    private void propertiesInputMTEInfo(File propertyFile,
            MachineTranslationExtentInfo mteInfo, String companyId)
    {
        if (mteInfo == null)
            return;

        StringBuffer buffer = new StringBuffer();
        buffer.append("##MachineTranslationExtentInfo.").append(companyId)
                .append(".").append(mteInfo.getId()).append(".begin")
                .append(NEW_LINE);
        buffer.append("MachineTranslationExtentInfo.").append(mteInfo.getId())
                .append(".ID=").append(mteInfo.getId()).append(NEW_LINE);
        buffer.append("MachineTranslationExtentInfo.").append(mteInfo.getId())
                .append(".MT_PROFILE_ID=")
                .append(mteInfo.getMtProfile().getId()).append(NEW_LINE);
        buffer.append("MachineTranslationExtentInfo.").append(mteInfo.getId())
                .append(".LANGUAGE_PAIR_CODE=")
                .append(mteInfo.getLanguagePairCode()).append(NEW_LINE);
        buffer.append("MachineTranslationExtentInfo.").append(mteInfo.getId())
                .append(".LANGUAGE_PAIR_NAME=")
                .append(mteInfo.getLanguagePairName()).append(NEW_LINE);
        buffer.append("MachineTranslationExtentInfo.").append(mteInfo.getId())
                .append(".DOMAIN_CODE=").append(mteInfo.getDomainCode())
                .append(NEW_LINE);
        buffer.append("##MachineTranslationExtentInfo.").append(companyId)
                .append(".").append(mteInfo.getId()).append(".end")
                .append(NEW_LINE).append(NEW_LINE);
        writeToFile(propertyFile, buffer.toString().getBytes());
    }

    private void active(HttpServletRequest p_request,
            MachineTranslationProfile mtProfile)
    {
        MTProfileHandlerHelper.activeMTProfile(mtProfile);

    }

    private MachineTranslationProfile getMTProfile(
            HttpServletRequest p_request, HttpServletResponse p_response)
            throws IOException
    {
        String id = (String) p_request
                .getParameter(MTProfileConstants.MT_PROFILE_ID);
        if (id == null
                || p_request.getMethod().equalsIgnoreCase(
                        WebAppConstants.REQUEST_METHOD_GET))
        {
            p_response
                    .sendRedirect("/globalsight/ControlServlet?activityName=mtProfiles");
            return null;
        }

        if (id != null)
        {
            long mtProfileId = -1;
            try
            {
                mtProfileId = Long.parseLong(id);

            }
            catch (NumberFormatException nfe)
            {
                p_response
                        .sendRedirect("/globalsight/ControlServlet?activityName=mtProfiles");
                return null;
            }
            MachineTranslationProfile mtProfile = MTProfileHandlerHelper
                    .getMTProfileById(id);
            return mtProfile;

        }
        return null;

    }

    private void removeMT(HttpServletRequest p_request,
            MachineTranslationProfile mtProfile)
    {
        String promt = MTProfileHandlerHelper.isAble2Delete(mtProfile.getId());
        if (promt != null)
        {
            p_request
                    .setAttribute(
                            "exception",
                            "Mt profile is refered to by the following localization profiles. Please deselect this Mt profile from the localization profiles before removing it."
                                    + promt);
        }
        else
        {
            MTProfileHandlerHelper.removeMTProfile(mtProfile);
            clearSessionExceptTableInfo(p_request.getSession(false), MTP_KEY);
        }

    }

    private void makeCondition(HttpServletRequest p_request,
            SessionManager sessionMgr, StringBuffer condition, String par,
            String sqlparam)
    {
        String uNameFilter = (String) (p_request.getParameter(par) == null ? sessionMgr
                .getAttribute(par) : p_request.getParameter(par));
        if (StringUtils.isNotBlank(uNameFilter))
        {
            sessionMgr.setAttribute(par, uNameFilter);
            condition.append(" and  " + sqlparam + " LIKE '%"
                    + StringUtil.transactSQLInjection(uNameFilter.trim())
                    + "%'");
        }
        else
        {
            sessionMgr.setAttribute(par, "");
        }
    }

    private void selectTMProfilesForDisplay(HttpServletRequest p_request,
            HttpSession p_session, String condition) throws ServletException,
            IOException, EnvoyServletException
    {
        Locale uiLocale = (Locale) p_session
                .getAttribute(WebAppConstants.UILOCALE);
        List<MachineTranslationProfile> mtProfiles = null;
        try
        {
            mtProfiles = (List<MachineTranslationProfile>) MTProfileHandlerHelper
                    .getAllMTProfiles(condition);
            String par = MTProfileConstants.FILTER_COMPANY_NAME;
            String filterCompanyValue = (String) (p_request.getParameter(par) == null ? getSessionManager(
                    p_request).getAttribute(par)
                    : p_request.getParameter(par));
            if (StringUtil.isEmpty(filterCompanyValue))
            {
                filterCompanyValue = "";
            }
            else
            {
                for (Iterator iter = mtProfiles.iterator(); iter.hasNext();)
                {
                    MachineTranslationProfile mt = (MachineTranslationProfile) iter
                            .next();
                    if (!CompanyWrapper.getCompanyNameById(mt.getCompanyid())
                            .contains(filterCompanyValue))
                    {
                        iter.remove();
                    }
                }

            }
            // num_per_page
            determineNumPerPage(p_request);
            getSessionManager(p_request).setAttribute(par, filterCompanyValue);
            setTableNavigation(p_request, p_session, mtProfiles,
                    new MTProfileComparator(uiLocale), num_per_page, MTPS_LIST,
                    MTP_KEY);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }

    }

    private void determineNumPerPage(HttpServletRequest request)
    {
        SessionManager sessionMgr = getSessionManager(request);
        String mtProfileNumPerPage = request.getParameter("numOfPageSize");
        if (StringUtil.isEmpty(mtProfileNumPerPage))
        {
            mtProfileNumPerPage = (String) sessionMgr
                    .getAttribute("mtProfileNumPerPage");
        }

        if (mtProfileNumPerPage != null)
        {
            sessionMgr.setAttribute("mtProfileNumPerPage",
                    mtProfileNumPerPage.trim());
            if ("all".equalsIgnoreCase(mtProfileNumPerPage))
            {
                num_per_page = Integer.MAX_VALUE;
            }
            else
            {
                try
                {
                    num_per_page = Integer.parseInt(mtProfileNumPerPage);
                }
                catch (NumberFormatException ignore)
                {
                    num_per_page = 10;
                }
            }
        }
    }

    /**
     * Create property file
     * 
     * @param userName
     * @param companyId
     * @return File
     * */
    private static File createPropertyFile(String userName, Long companyId)
    {
        StringBuffer filePath = new StringBuffer();
        filePath.append(AmbFileStoragePathUtils.getFileStorageDirPath())
                .append(File.separator).append("GlobalSight")
                .append(File.separator).append("MachineTranslationProfiles")
                .append(File.separator).append("export");
        File file = new File(filePath.toString());
        file.mkdirs();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String fileName = "MachineTranslationProfiles_" + userName + "_"
                + sdf.format(new Date()) + ".properties";
        File propertiesFile = new File(file, fileName);

        return propertiesFile;
    }

    private static void writeToFile(File writeInFile, byte[] bytes)
    {
        writeInFile.getParentFile().mkdirs();

        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream(writeInFile, true);
            fos.write(bytes);
        }
        catch (Exception e)
        {
        }
        finally
        {
            try
            {
                fos.close();
            }
            catch (IOException e)
            {

            }
        }
    }

    public void clearSessionExceptTableInfo(HttpSession p_session, String p_key)
    {
        SessionManager sessionMgr = (SessionManager) p_session
                .getAttribute(SESSION_MANAGER);

        Integer sortType = (Integer) sessionMgr.getAttribute(p_key
                + TableConstants.SORTING);
        Boolean reverseSort = (Boolean) sessionMgr.getAttribute(p_key
                + TableConstants.REVERSE_SORT);
        Integer lastPage = (Integer) sessionMgr.getAttribute(p_key
                + TableConstants.LAST_PAGE_NUM);
        sessionMgr.clear();
        sessionMgr.setAttribute(p_key + TableConstants.SORTING, sortType);
        sessionMgr.setAttribute(p_key + TableConstants.REVERSE_SORT,
                reverseSort);
        sessionMgr.setAttribute(p_key + TableConstants.LAST_PAGE_NUM, lastPage);
    }

    private SessionManager getSessionManager(HttpServletRequest request)
    {
        HttpSession session = request.getSession(false);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);

        return sessionMgr;
    }
}
