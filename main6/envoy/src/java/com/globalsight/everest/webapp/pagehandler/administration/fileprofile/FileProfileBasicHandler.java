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
package com.globalsight.everest.webapp.pagehandler.administration.fileprofile;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.cxe.entity.filterconfiguration.FilterHelper;
import com.globalsight.cxe.entity.filterconfiguration.QAFilterManager;
import com.globalsight.cxe.entity.knownformattype.KnownFormatType;
import com.globalsight.cxe.entity.knownformattype.KnownFormatTypeImpl;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.FileExtensionComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.config.xmldtd.XmlDtdManager;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.FormUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.SortUtil;
import com.globalsight.util.collections.HashtableValueOrderWalkerFactory;

/**
 * Pagehandler for the new File Profile page
 */
public class FileProfileBasicHandler extends PageHandler
{
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
        String action = p_request.getParameter("action");

        try
        {
            // Verify whether the scripts exist in the server side.
            if (FileProfileConstants.VERIFY_ACTION.equals(action))
            {
                String message = "";
                String filePath = (String) p_request.getParameter("path");
                File scriptFile = new File(filePath);

                if (scriptFile.isDirectory())
                {
                    message = String.valueOf(FileProfileConstants.IS_DIRECTORY);
                }
                else if (!scriptFile.exists())
                {
                    message = String
                            .valueOf(FileProfileConstants.FILE_NOT_EXIST);
                }
                else
                {
                    message = String.valueOf(FileProfileConstants.FILE_OK);
                }
                PrintWriter out = p_response.getWriter();
                p_response.setContentType("text/html");
                out.write(message);
                return;
            }

            FileProfile fp = null;
            String formatType = null;
            List extensionList = null;
            if (action.equals(FileProfileConstants.EDIT))
            {
                String idString = (String) p_request.getParameter(RADIO_BUTTON);
                if (idString == null
                        || p_request.getMethod().equalsIgnoreCase(
                                REQUEST_METHOD_GET))
                {
                    p_response
                            .sendRedirect("/globalsight/ControlServlet?activityName=fileprofiles");
                    return;
                }
                if (idString != null)
                {
                    String id = idString.split(",")[0];
                    fp = ServerProxy.getFileProfilePersistenceManager()
                            .getFileProfileById(Long.parseLong(id), true);
                    formatType = ServerProxy
                            .getFileProfilePersistenceManager()
                            .getKnownFormatTypeById(fp.getKnownFormatTypeId(),
                                    false).getFormatType();
                    extensionList = (List) ServerProxy
                            .getFileProfilePersistenceManager()
                            .getFileExtensionsByFileProfile(fp);
                    SessionManager sessionMgr = (SessionManager) p_request
                            .getSession().getAttribute(
                                    WebAppConstants.SESSION_MANAGER);
                    sessionMgr.setAttribute("fileprofile", fp);
                    sessionMgr.setAttribute("formatType", formatType);
                    p_request.setAttribute("edit", "true");
                }
            }

            setDataForComboBoxes(p_request, action, formatType, extensionList);

            FormUtil.addSubmitToken(p_request, FormUtil.Forms.NEW_FILE_PROFILE);
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

    private void setDataForComboBoxes(HttpServletRequest p_request,
            String p_action, String p_format, List p_extension)
            throws GeneralException, NamingException, RemoteException
    {
        // create hashtable of L10nProfile names and ids
        Hashtable ht = ServerProxy.getProjectHandler().getAllL10nProfileNames();
        p_request.setAttribute("locProfiles", HashtableValueOrderWalkerFactory
                .createHashtableValueOrderWalker(ht));

        // file profiles for checking dup names
        p_request.setAttribute("names", ServerProxy
                .getFileProfilePersistenceManager().getAllFileProfiles());

        // format types
        List<KnownFormatTypeImpl> allFormatTypes = new ArrayList(ServerProxy
                .getFileProfilePersistenceManager().getAllKnownFormatTypes());

        for (KnownFormatTypeImpl type : allFormatTypes)
        {
            if (type.getName() != null
                    && type.getName().indexOf("New Office 2010") > 0)
            {
                allFormatTypes.remove(type);
                allFormatTypes.add(type);
                break;
            }
        }

        // filters
        String companyName = UserUtil.getCurrentCompanyName(p_request);
        long companyId = ServerProxy.getJobHandler().getCompany(companyName)
                .getIdAsLong();
        p_request
                .setAttribute("filters", FilterHelper.getKnownFormatFilterMap(
                        allFormatTypes, companyId));

        p_request.setAttribute("qaFilters",
                QAFilterManager.getAllQAFilters(companyId));

        // Data encodings
        p_request.setAttribute("encodings", ServerProxy.getLocaleManager()
                .getAllCodeSets());

        // XML Rules
        p_request.setAttribute("xmlRules", ServerProxy
                .getXmlRuleFilePersistenceManager().getAllXmlRuleFiles());

        // XML DTD
        p_request.setAttribute("xmlDtds", XmlDtdManager.getAllXmlDtd());

        // File Extensions
        List extensions = new ArrayList(ServerProxy
                .getFileProfilePersistenceManager().getAllFileExtensions());
        HttpSession session = p_request.getSession();
        if (session != null
                && session.getAttribute(WebAppConstants.UILOCALE) != null)
        {
            Locale locale = (Locale) session
                    .getAttribute(WebAppConstants.UILOCALE);
            SortUtil.sort(extensions, new FileExtensionComparator(locale));
        }

        hideSomeFormat(allFormatTypes, p_action, p_format);

        p_request.setAttribute("formatTypes", allFormatTypes);
        p_request.setAttribute("extensions", extensions);

        /**
         * List xlfExts = new ArrayList(); xlfExts.add("xlf");
         * xlfExts.add("xliff"); ArrayList xlfFps = new ArrayList(ServerProxy
         * .getFileProfilePersistenceManager().getFileProfilesByExtension(
         * xlfExts, companyId)); p_request.setAttribute("xlfFps", xlfFps);
         */
    }

    // Hidden some useless file format
    public void hideSomeFormat(List<?> p_allFormatTypes, String p_action,
            String p_useFormat)
    {
        ArrayList<String> ulFormatlist = new ArrayList<String>(
                FileProfileConstants.useLessFormatList);

        if (p_action.equals(FileProfileConstants.EDIT))
        {
            ulFormatlist.remove(p_useFormat);
            removeIgnoreCase(ulFormatlist, p_useFormat);
        }

        removeFormatWithNameList(p_allFormatTypes, ulFormatlist);
    }

    @SuppressWarnings("unchecked")
    public void removeIgnoreCase(ArrayList list, String name)
    {
        for (int i = 0; i < list.size(); i++)
        {
            if (name.equalsIgnoreCase(list.get(i).toString()))
            {
                list.remove(i);
                break;
            }
        }
    }

    public void removeFormatWithNameList(List<?> list, List<?> nameList)
    {
        for (int i = 0; i < list.size(); i++)
        {
            KnownFormatType elem = (KnownFormatType) list.get(i);
            if (nameList.contains(elem.getName()))
            {
                list.remove(elem);
                i--;
            }
        }
    }
}
