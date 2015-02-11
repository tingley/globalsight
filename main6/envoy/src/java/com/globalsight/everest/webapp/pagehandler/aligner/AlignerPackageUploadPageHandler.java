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

package com.globalsight.everest.webapp.pagehandler.aligner;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.everest.aligner.AlignerManager;
import com.globalsight.everest.aligner.AlignerPackageUploadOptions;
import com.globalsight.everest.company.Company;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.projecthandler.ProjectTM;
import com.globalsight.everest.projecthandler.ProjectTMTBUsers;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.StringComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.GeneralException;
import com.globalsight.util.SortUtil;
import com.globalsight.util.edit.EditUtil;

/**
 * <p>
 * This page handler is responsible for creating aligner packages, downloading
 * and uploading them..
 * </p>
 */
public class AlignerPackageUploadPageHandler extends PageHandler implements
        WebAppConstants
{
    static private final Logger CATEGORY = Logger
            .getLogger(AlignerPackageUploadPageHandler.class);

    static public String UPLOADDIR = "/_AlignerUploads_";

    //
    // Static Members
    //
    static private AlignerManager s_manager = null;

    //
    // Constructor
    //
    public AlignerPackageUploadPageHandler()
    {
        super();

        if (s_manager == null)
        {
            try
            {
                s_manager = ServerProxy.getAlignerManager();
            }
            catch (GeneralException ex)
            {
                // ignore.
            }
        }
    }

    //
    // Interface Methods: PageHandler
    //

    /**
     * Invoke this PageHandler.
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
        HttpSession session = p_request.getSession();
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);

        Locale uiLocale = (Locale) session
                .getAttribute(WebAppConstants.UILOCALE);

        String userId = getUser(session).getUserId();
        String action = (String) p_request.getParameter(GAP_ACTION);
        String options = (String) p_request.getParameter(GAP_OPTIONS);

        if (options != null)
        {
            // options are posted as UTF-8 string
            options = EditUtil.utf8ToUnicode(options);
        }

        try
        {
            AlignerPackageUploadOptions gapOptions = (AlignerPackageUploadOptions) sessionMgr
                    .getAttribute(GAP_OPTIONS);

            if (gapOptions != null)
            {
                if (options != null)
                {
                    gapOptions.init(options);
                }
            }
            else
            {
                gapOptions = new AlignerPackageUploadOptions();

                sessionMgr.setAttribute(GAP_OPTIONS, gapOptions);
            }

            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("package upload options = "
                        + gapOptions.getXml());
            }

            if (action == null)
            {
                if (sessionMgr.getAttribute(GAP_TMS) == null)
                {
                    ArrayList tms = getTMs(userId);

                    // Sort the TM list. Use StringComparator for
                    // plain names, or TmComparator for Tm objects (Tm
                    // is an interface implemented by ProjectTm).
                    SortUtil.sort(tms, new StringComparator(uiLocale));

                    sessionMgr.setAttribute(GAP_TMS, tms);
                }
            }
            else if (action.equals(GAP_ACTION_UPLOADPACKAGE))
            {
                // Read file and options from upload request,
                // then pass to importer.
                FileUploadHelper o_upload = new FileUploadHelper();
                o_upload.doUpload(p_request);

                gapOptions.init(o_upload.getFieldValue(GAP_OPTIONS));

                if (CATEGORY.isDebugEnabled())
                {
                    CATEGORY.debug("Uploading alignment package "
                            + o_upload.getSavedFilepath() + " using options\n"
                            + gapOptions.getXml());
                }

                s_manager.uploadPackage(gapOptions,
                        o_upload.getSavedFilepath(), getUser(session));
            }

            // clean up performed in AlignerPageHandler.
        }
        catch (Exception ex)
        {
            CATEGORY.error("aligner package upload error", ex);

            // JSP needs to clear this.
            sessionMgr.setAttribute(GAP_ERROR, ex.toString());
        }

        super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                p_context);
    }

    /**
     * Get TMs this user can access
     * 
     * @param userId
     *            User's id
     * @return
     * @throws Exception
     * 
     * @author Leon Song
     * @since 8.0
     */
    private ArrayList getTMs(String userId) throws Exception
    {
        String currentCompanyId = CompanyThreadLocal.getInstance().getValue();
        Company currentCompany = CompanyWrapper
                .getCompanyById(currentCompanyId);
        boolean enableTMAccessControl = currentCompany
                .getEnableTMAccessControl();
        ArrayList<String> tmListOfUser = new ArrayList<String>();
        if (enableTMAccessControl)
        {
            boolean isAdmin = UserUtil.isInPermissionGroup(userId,
                    "Administrator");
            boolean isSuperAdmin = UserUtil.isSuperAdmin(userId);
            boolean isSuperPM = UserUtil.isSuperPM(userId);
            if (!isAdmin && !isSuperAdmin)
            {
                ProjectTMTBUsers projectTMTBUsers = new ProjectTMTBUsers();

                if (isSuperPM)
                {
                    List tmIdList = projectTMTBUsers.getTList(userId, "TM");
                    Iterator it = tmIdList.iterator();
                    while (it.hasNext())
                    {
                        ProjectTM tm = null;
                        try
                        {
                            tm = ServerProxy
                                    .getProjectHandler()
                                    .getProjectTMById(
                                            ((BigInteger) it.next())
                                                    .longValue(),
                                            false);
                        }
                        catch (Exception e)
                        {
                            throw new EnvoyServletException(e);
                        }
                        if (String.valueOf(tm.getCompanyId()).equals(
                                currentCompanyId))
                        {
                            tmListOfUser.add(tm.getName());
                        }
                    }
                }
                else
                {
                    List tmIdList = projectTMTBUsers.getTList(userId, "TM");
                    Iterator it = tmIdList.iterator();
                    while (it.hasNext())
                    {
                        ProjectTM tm = null;
                        try
                        {
                            tm = ServerProxy
                                    .getProjectHandler()
                                    .getProjectTMById(
                                            ((BigInteger) it.next())
                                                    .longValue(),
                                            false);
                        }
                        catch (Exception e)
                        {
                            throw new EnvoyServletException(e);
                        }
                        tmListOfUser.add(tm.getName());
                    }
                }
            }
            else
            {
                // result is an unordered collection of TM objects.
                Collection tmp = ServerProxy.getProjectHandler()
                        .getAllProjectTMs();

                for (Iterator it = tmp.iterator(); it.hasNext();)
                {
                    ProjectTM tm = (ProjectTM) it.next();
                    if (String.valueOf(tm.getCompanyId()).equals(
                            currentCompanyId))
                    {
                        tmListOfUser.add(tm.getName());
                    }
                }
            }
        }
        else
        {
            // result is an unordered collection of TM objects.
            Collection tmp = ServerProxy.getProjectHandler().getAllProjectTMs();

            for (Iterator it = tmp.iterator(); it.hasNext();)
            {
                ProjectTM tm = (ProjectTM) it.next();
                if (String.valueOf(tm.getCompanyId()).equals(currentCompanyId))
                {
                    tmListOfUser.add(tm.getName());
                }
            }

        }
        return tmListOfUser;
    }
}
