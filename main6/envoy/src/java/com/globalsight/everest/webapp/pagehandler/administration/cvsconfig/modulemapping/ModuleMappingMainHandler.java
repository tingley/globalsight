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
package com.globalsight.everest.webapp.pagehandler.administration.cvsconfig.modulemapping;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;

import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.cvsconfig.CVSServer;
import com.globalsight.everest.cvsconfig.CVSServerManagerLocal;
import com.globalsight.everest.cvsconfig.CVSUtil;
import com.globalsight.everest.cvsconfig.modulemapping.ModuleMapping;
import com.globalsight.everest.cvsconfig.modulemapping.ModuleMappingComparator;
import com.globalsight.everest.cvsconfig.modulemapping.ModuleMappingException;
import com.globalsight.everest.cvsconfig.modulemapping.ModuleMappingManagerLocal;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.SortUtil;

public class ModuleMappingMainHandler extends PageHandler
{
    private ModuleMappingManagerLocal manager = new ModuleMappingManagerLocal();
    private static final Logger logger = Logger
            .getLogger(ModuleMappingMainHandler.class.getName());

    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws ServletException, IOException,
            EnvoyServletException
    {
        try
        {
            HttpSession session = p_request.getSession(false);
            String action = p_request.getParameter("action");

            HashMap<String, String> params = null;
            if (ModuleMappingConstants.CREATE.equals(action))
            {
                // Add new CVS server configuration
                createModuleMapping(p_request);
            }
            else if (ModuleMappingConstants.UPDATE.equals(action))
            {
                updateModuleMapping(p_request);
            }
            else if (ModuleMappingConstants.REMOVE.equals(action))
            {
                removeModuleMapping(p_request);
            }
            else if (ModuleMappingConstants.SEARCH.equals(action))
            {
                // Process the search conditions.
                params = new HashMap<String, String>();
                String tmp = p_request.getParameter("s_srcLocale");
                tmp = (tmp != null && !"-1".equals(tmp.trim())) ? tmp.trim()
                        : "-1";
                params.put("sourceLocale", tmp);
                tmp = p_request.getParameter("s_tarLocale");
                tmp = (tmp != null && !"-1".equals(tmp.trim())) ? tmp.trim()
                        : "-1";
                params.put("targetLocale", tmp);
                tmp = p_request.getParameter("s_moduleName");
                tmp = (tmp != null && !"".equals(tmp.trim())) ? tmp.trim() : "";
                params.put("moduleName", tmp);
            }
            session.setAttribute("companyName",
                    CompanyWrapper.getCurrentCompanyName());
            session.setAttribute("mmSearchParam",
                    params == null ? new HashMap<String, String>() : params);
            setModuleMapping(p_request);
            dataForTable(p_request, session, params);

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
        catch (HibernateException e)
        {
            throw new EnvoyServletException(e);
        }

        super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                p_context);
    }

    private void setModuleMapping(HttpServletRequest p_request)
            throws RemoteException, NamingException, GeneralException
    {
        Vector sourceLocales = ServerProxy.getLocaleManager()
                .getAllSourceLocales();
        Vector targetLocales = ServerProxy.getLocaleManager()
                .getAllTargetLocales();
        SortUtil.sort(sourceLocales, new Comparator()
        {
            public int compare(Object o1, Object o2)
            {
                return ((GlobalSightLocale) o1).getDisplayName(Locale.US)
                        .compareToIgnoreCase(
                                ((GlobalSightLocale) o2)
                                        .getDisplayName(Locale.US));
            }
        });
        SortUtil.sort(targetLocales, new Comparator()
        {
            public int compare(Object o1, Object o2)
            {
                return ((GlobalSightLocale) o1).getDisplayName(Locale.US)
                        .compareToIgnoreCase(
                                ((GlobalSightLocale) o2)
                                        .getDisplayName(Locale.US));
            }
        });
        p_request.setAttribute(ModuleMappingConstants.SOURCE_LOCALE_PAIRS,
                sourceLocales);
        p_request.setAttribute(ModuleMappingConstants.TARGET_LOCALE_PAIRS,
                targetLocales);
    }

    private void dataForTable(HttpServletRequest p_request,
            HttpSession p_session, HashMap<String, String> p_params)
            throws RemoteException, NamingException, GeneralException
    {
        List list = null;
        if (p_params == null)
            list = (List) manager.getAll();
        else
            list = (List) manager.getModuleMappings(p_params);
        SessionManager sessionMgr = (SessionManager) p_session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        sessionMgr.setAttribute(ModuleMappingConstants.MODULE_MAPPING_LIST,
                list);
        Vector servers = vectorizedCollection(list);
        Locale uiLocale = (Locale) p_session
                .getAttribute(WebAppConstants.UILOCALE);

        setTableNavigation(p_request, p_session, servers,
                new ModuleMappingComparator(uiLocale), 10,
                ModuleMappingConstants.MODULE_MAPPING_LIST,
                ModuleMappingConstants.MODULE_MAPPING_KEY);
    }

    private void createModuleMapping(HttpServletRequest p_request)
            throws ModuleMappingException, RemoteException
    {
        ArrayList<ModuleMapping> mms = new ArrayList<ModuleMapping>();
        ModuleMapping mm = null;
        CVSServerManagerLocal serverManager = new CVSServerManagerLocal();
        try
        {
            HttpSession session = p_request.getSession();
            SessionManager sessionMgr = (SessionManager) session
                    .getAttribute(WebAppConstants.SESSION_MANAGER);

            String srcLocale = ServerProxy
                    .getLocaleManager()
                    .getLocaleById(
                            Integer.parseInt(p_request
                                    .getParameter(ModuleMappingConstants.SOURCE_LOCALE)))
                    .toString();
            String srcLocaleLong = ServerProxy.getLocaleManager()
                    .getLocaleByString(srcLocale).getDisplayName();
            String srcModule = p_request
                    .getParameter(ModuleMappingConstants.SOURCE_MODULE);
            String tarLocale = "", tarLocaleLong = "", targetModule = "", subFolder = "";
            long serverId = Long.parseLong(p_request.getParameter("cvsServer"));
            long companyId = CompanyWrapper.getCurrentCompanyIdAsLong();
            boolean createSubFolder = false;
            CVSServer server = serverManager.getServer(serverId);
            ArrayList<String> subFolders = getAllSubFolders(CVSUtil
                    .getBaseDocRoot() + srcModule);
            ArrayList<ModuleMapping> existMMs = new ArrayList<ModuleMapping>();
            File tmpFile = new File(CVSUtil.getBaseDocRoot() + srcModule);

            // Get information from request
            int count = Integer.parseInt(p_request.getParameter("count"));
            for (int i = 0; i < count; i++)
            {
                tarLocale = p_request.getParameter("targetLocale" + i);
                if (tarLocale == null || tarLocale.equals(""))
                    continue;
                tarLocale = ServerProxy.getLocaleManager()
                        .getLocaleById(Integer.parseInt(tarLocale)).toString();
                tarLocaleLong = ServerProxy.getLocaleManager()
                        .getLocaleByString(tarLocale).getDisplayName();
                targetModule = p_request.getParameter("targetModule" + i);
                subFolder = p_request.getParameter("subfolder" + i);
                createSubFolder = "1".equals(subFolder);
                if (tmpFile.isFile())
                    createSubFolder = false;

                mm = new ModuleMapping();
                mm.setCompanyId(companyId);
                mm.setSourceLocale(srcLocale);
                mm.setSourceLocaleLong(srcLocaleLong);
                mm.setSourceModule(srcModule);
                mm.setTargetLocale(tarLocale);
                mm.setTargetLocaleLong(tarLocaleLong);
                mm.setTargetModule(targetModule);
                mm.setModuleId(serverId);
                mm.setSubFolderMapped(createSubFolder ? "1" : "0");
                if (manager.isModuleMappingExist(mm) == null)
                {
                    mms.add(mm);

                    if (createSubFolder)
                    {
                        generateTargetFolder(targetModule, subFolders, server);
                        String srcPath = "", tarPath = "";
                        for (String path : subFolders)
                        {
                            srcPath = srcModule + File.separator + path;
                            tarPath = targetModule + File.separator + path;

                            mm = new ModuleMapping();
                            mm.setSourceLocale(srcLocale);
                            mm.setSourceLocaleLong(srcLocaleLong);
                            mm.setSourceModule(srcPath);
                            mm.setTargetLocale(tarLocale);
                            mm.setTargetLocaleLong(tarLocaleLong);
                            mm.setTargetModule(tarPath);
                            mm.setCompanyId(companyId);
                            mm.setModuleId(serverId);
                            mm.setSubFolderMapped("2");

                            if (manager.isModuleMappingExist(mm) == null)
                            {
                                mms.add(mm);
                            }
                            else
                                existMMs.add(mm);
                        }
                    }
                }
                else
                    existMMs.add(mm);
            }
            if (mms.size() > 0)
                manager.addModuleMapping(mms);
            sessionMgr.setAttribute("cvsmsg", existMMs);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
        }
    }

    private void updateModuleMapping(HttpServletRequest p_request)
            throws ModuleMappingException, RemoteException
    {
        ArrayList<ModuleMapping> mms = new ArrayList<ModuleMapping>();
        ModuleMapping mm = null, oldMM = null;
        HttpSession session = null;
        try
        {
            session = p_request.getSession(false);
            SessionManager sessionMgr = (SessionManager) session
                    .getAttribute(WebAppConstants.SESSION_MANAGER);
            mm = (ModuleMapping) sessionMgr
                    .getAttribute(ModuleMappingConstants.MODULE_MAPPING_KEY);
            oldMM = manager.getModuleMapping(mm.getId());
            long companyId = CompanyWrapper.getCurrentCompanyIdAsLong();

            // set parameter
            String srcLocale = ServerProxy
                    .getLocaleManager()
                    .getLocaleById(
                            Integer.parseInt(p_request
                                    .getParameter(ModuleMappingConstants.SOURCE_LOCALE)))
                    .toString();
            String srcLocaleLong = ServerProxy.getLocaleManager()
                    .getLocaleByString(srcLocale).getDisplayName();
            String tarLocale = ServerProxy
                    .getLocaleManager()
                    .getLocaleById(
                            Integer.parseInt(p_request
                                    .getParameter(ModuleMappingConstants.TARGET_LOCALE)))
                    .toString();
            String tarLocaleLong = ServerProxy.getLocaleManager()
                    .getLocaleByString(tarLocale).getDisplayName();
            long serverId = Long.parseLong(p_request.getParameter("cvsServer"));
            String srcModule = p_request
                    .getParameter(ModuleMappingConstants.SOURCE_MODULE);
            String tarModule = p_request
                    .getParameter(ModuleMappingConstants.TARGET_MODULE);
            boolean subfolder = "1".equals(p_request.getParameter("subfolder"));
            String tmpFilename = CVSUtil.getBaseDocRoot() + srcModule;
            ArrayList<String> subFolders = getAllSubFolders(tmpFilename);
            CVSServerManagerLocal serverManager = new CVSServerManagerLocal();
            CVSServer server = serverManager.getServer(serverId);
            ArrayList<ModuleMapping> existMMs = new ArrayList<ModuleMapping>();
            File tmpFile = new File(tmpFilename);
            if (tmpFile.isFile())
                subfolder = false;

            mm.setSourceLocale(srcLocale);
            mm.setSourceLocaleLong(srcLocaleLong);
            mm.setSourceModule(srcModule);
            mm.setTargetLocale(tarLocale);
            mm.setTargetLocaleLong(tarLocaleLong);
            mm.setTargetModule(tarModule);
            mm.setCompanyId(companyId);
            mm.setModuleId(serverId);
            mm.setSubFolderMapped(subfolder ? "1" : "0");
            if (manager.isModuleMappingExist(mm) == null)
            {
                mms.add(mm);

                // if
                // (!mm.getSubFolderMapped().equals(oldMM.getSubFolderMapped()))
                // {
                if (subfolder)
                {
                    // Create multiple module mapping with sub-folders in source
                    // module, and create folder under the target module
                    generateTargetFolder(tarModule, subFolders, server);
                    String srcPath = "", tarPath = "";
                    for (String path : subFolders)
                    {
                        srcPath = srcModule + File.separator + path;
                        tarPath = tarModule + File.separator + path;

                        mm = new ModuleMapping();
                        mm.setSourceLocale(srcLocale);
                        mm.setSourceLocaleLong(srcLocaleLong);
                        mm.setSourceModule(srcPath);
                        mm.setTargetLocale(tarLocale);
                        mm.setTargetLocaleLong(tarLocaleLong);
                        mm.setTargetModule(tarPath);
                        mm.setCompanyId(companyId);
                        mm.setModuleId(serverId);
                        mm.setSubFolderMapped("2");
                        if (manager.isModuleMappingExist(mm) == null)
                            mms.add(mm);
                        // else
                        // existMMs.add(mm);
                    }
                }
            }
            else
                existMMs.add(mm);
            if (mms.size() > 0)
                manager.updateModuleMapping(mms);
            sessionMgr.setAttribute("cvsmsg", existMMs);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, e);
        }
    }

    private void removeModuleMapping(HttpServletRequest p_request)
    {
        String id = p_request.getParameter("id");
        if (id != null && !id.trim().equals(""))
            manager.removeModuleMapping(Long.parseLong(id));
    }

    private ArrayList<String> getAllSubFolders(String p_srcModule)
    {
        ArrayList<String> result = new ArrayList<String>();
        if (p_srcModule == null || "".equals(p_srcModule))
            return result;
        ArrayList<String> tmp = processSubFolder(p_srcModule);
        int len = p_srcModule.length();
        for (String t : tmp)
        {
            t = t.substring(len + 1);
            result.add(t);
        }
        return result;
    }

    private ArrayList<String> processSubFolder(String p_srcModule)
    {
        ArrayList<String> folders = new ArrayList<String>();
        try
        {
            File folder = new File(p_srcModule);
            File[] list = folder.listFiles();
            File tmp = null;
            String tmpPath = "";
            if (list != null)
            {
                for (int i = 0; i < list.length; i++)
                {
                    tmp = list[i];
                    if (tmp.isDirectory()
                            && !"CVS".equals(tmp.getName().toUpperCase()))
                    {
                        tmpPath = tmp.getAbsolutePath();
                        folders.add(tmpPath);
                        folders.addAll(processSubFolder(tmpPath));
                    }
                }
            }
        }
        catch (Exception e)
        {
            logger.error("Error::" + e.toString());
        }
        return folders;
    }

    private void generateTargetFolder(String p_tarModule,
            ArrayList<String> p_folders, CVSServer p_server)
    {
        if (p_folders == null || p_folders.size() == 0)
            return;
        String baseTargetPath = CVSUtil.getBaseDocRoot() + p_tarModule
                + File.separator;
        try
        {
            File folder = null;
            for (String t : p_folders)
            {
                folder = new File(baseTargetPath + t);
                if (folder.exists() && folder.isDirectory())
                    continue;
                folder.mkdirs();
                String[] cmd = new String[]
                { "cvs", "-d", p_server.getCVSRootEnv(), "add", t };
                CVSUtil.exeCVSCmd(cmd, baseTargetPath);
            }
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
        }
    }
}
