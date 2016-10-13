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

package com.globalsight.everest.webapp.pagehandler.administration.config.uilocale;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;

import com.globalsight.config.SystemParameter;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.util.comparator.UILocaleComparator;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.ActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.GeneralExceptionConstants;
import com.globalsight.util.system.ConfigException;
import com.globalsight.util.zip.ZipIt;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.FileUtil;

/**
 * Deals with some requirements about globalsight UI locale.
 */
public class UILocaleMainHandler extends PageActionHandler
{
    static private final Logger logger = Logger
            .getLogger(UILocaleMainHandler.class);

    private List<String> m_supportedLocales = null;
    private String m_defaultLocale = null;

    /**
     * Handes adding ui locale requirement.
     * <p>
     * If the ui locale is invalid or the ui locale has been exist in database,
     * the ui locale will not be added, and caller can't get any information
     * about it.
     * <p>
     * Because nothing happened if the ui locale has been existed in database,
     * there are no problem with resubmit form.
     * 
     * @param request
     * @param response
     * @param form
     * @throws Exception
     */
    @ActionHandler(action = UILocaleConstant.SAVE, formClass = "com.globalsight.everest.webapp.pagehandler.administration.config.uilocale.UILocale", loadFromDb = false)
    public void save(HttpServletRequest request, HttpServletResponse response, Object form)
            throws Exception
    {
        logger.debug("Saving ui locale...");

        try
        {
            UILocale uiLocale = (UILocale) form;

            if (uiLocale.getShortName() == null || uiLocale.getShortName().equals(""))
            {
                uiLocale.setDefaultLocale(Boolean.parseBoolean(request
                        .getParameter("defaultLocale")));
                uiLocale.setShortName(request.getParameter("shortname"));
                uiLocale.setLongName(request.getParameter("longname"));
            }

            String name = uiLocale.getShortName();
            if (null != name)
            {
                boolean isexisted = isExisted(name);
                boolean added = false;

                if (!isexisted)
                {
                    SystemParameter sp = UILocaleManager
                            .getSystemParameter(SystemConfigParamNames.UI_LOCALES);
                    String newValue = sp.getValue() + SystemConfiguration.DEFAULT_DELIMITER + name;
                    updateUILocales(newValue);
                    added = true;
                }

                if ((isexisted || added) && uiLocale.isDefaultLocale())
                {
                    updateDefaultUILocale(name);
                }

                // copy default properties file for added locale if the
                // propertiies
                // file does not exist
                if (added)
                {
                    File[] proFiles = UILocaleManager.getPropertiesFilesListOfAll(name);
                    makeSureAllTheFilesExistsForOneLocale(name, proFiles);

                    UILocaleManager.CopyOtherFilesForLocale(name);
                }
            }
        }
        catch (Exception e)
        {
            logger.error("setdefault", e);
            throw e;
        }
        logger.debug("Saving ui locale finished");
    }

    private void makeSureAllTheFilesExistsForOneLocale(String uilocale, File[] proFiles)
            throws IOException
    {
        for (File file : proFiles)
        {
            if (!file.exists())
            {
                File defaultProFile = UILocaleManager.getPropertiesFileDefault(file, uilocale);
                FileUtil.copyFile(defaultProFile, file);
            }
        }
    }

    /**
     * Removes some ui locale from database according to name.
     * <p>
     * For nothing happened if the ui locale not exists, there are no problem
     * with resubmet form.
     * 
     * @param request
     * @param response
     * @param form
     * @throws Exception
     */
    @ActionHandler(action = UILocaleConstant.REMOVE, formClass = "")
    public void remove(HttpServletRequest request, HttpServletResponse response, Object form)
            throws Exception
    {
    	// gbs-1389: restrict direct access to remove a language locale
		// without "Remove" permission
    	HttpSession session = request.getSession(false);
		PermissionSet userPerms = (PermissionSet) session
				.getAttribute(WebAppConstants.PERMISSIONS);
		if (!userPerms.getPermissionFor(Permission.UILOCALE_REMOVE)) 
		{
			request.setAttribute("restricted_access", true);
			if (userPerms.getPermissionFor(Permission.UILOCALE_VIEW)) 
			{
				response
						.sendRedirect("/globalsight/ControlServlet?activityName=uiLocaleConfiguration");
			} 
			else 
			{
				response.sendRedirect(request.getContextPath());
			}
			return;
		}
        logger.debug("Removing ui locale...");

        try
        {
            String uilocale = request.getParameter("selectedLocale");

            if (isRemoveable(uilocale))
            {
                m_supportedLocales.remove(uilocale);

                StringBuilder sb = new StringBuilder();
                for (String loc : m_supportedLocales)
                {
                    sb.append(loc).append(SystemConfiguration.DEFAULT_DELIMITER);
                }
                sb.deleteCharAt(sb.length() - 1);

                updateUILocales(sb.toString());

                removeLocaleResouces(uilocale);
            }
        }
        catch (Exception e)
        {
            logger.error("setdefault", e);
            throw e;
        }

        logger.debug("Removing ui locale finished");
    }

    /**
     * Download resource file for some ui locale .
     * 
     * @param request
     * @param response
     * @param form
     * @throws Exception
     */
    @ActionHandler(action = UILocaleConstant.DOWNLOAD_RES, formClass = "")
    public void downloadres(HttpServletRequest request, HttpServletResponse response, Object form)
            throws Exception
    {
    	// gbs-1389: restrict direct access to download resource
		// without "Download Resource" permission
    	HttpSession session = request.getSession(false);
		PermissionSet userPerms = (PermissionSet) session
				.getAttribute(WebAppConstants.PERMISSIONS);
		if (!userPerms.getPermissionFor(Permission.UILOCALE_DOWNLOAD_RES)) 
		{
			request.setAttribute("restricted_access", true);
			if (userPerms.getPermissionFor(Permission.UILOCALE_VIEW)) 
			{
				response
						.sendRedirect("/globalsight/ControlServlet?activityName=uiLocaleConfiguration");
			} 
			else 
			{
				response.sendRedirect(request.getContextPath());
			}
			return;
		}
        logger.debug("downloadres ui locale...");

        try
        {
            String uilocale = request.getParameter("selectedLocale");
            ResourceBundle bundle = PageHandler.getBundle(request.getSession());

            // zip
            String zipFileName = "LocaleResources_" + uilocale + ".zip";
            File zipFile = new File(AmbFileStoragePathUtils.getTempFileDir(), zipFileName);
            File[] zipEntries = UILocaleManager.getPropertiesFilesListOfAll(uilocale);
            makeSureAllTheFilesExistsForOneLocale(uilocale, zipEntries);

            ZipIt.addEntriesToZipFile(zipFile, zipEntries, true, String.format(bundle
                    .getString("msg_uilocale_zip"), uilocale));

            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=" + zipFileName + ";");
            UILocaleManager.writeOutFile(zipFile, response);
        }
        catch (Exception e)
        {
            logger.error("download resource");
            throw e;
        }
        finally
        {
        	pageReturn();
        }

        logger.debug("downloadres ui locale finished");
    }

    /**
     * Download resource file for some ui locale .
     * 
     * @param request
     * @param response
     * @param form
     * @throws Exception
     */
    @ActionHandler(action = UILocaleConstant.SETDEFAULT, formClass = "")
    public void setdefault(HttpServletRequest request, HttpServletResponse response, Object form)
            throws Exception
    {
    	// gbs-1389: restrict direct access to set default
		// without "Set Default" permission
    	HttpSession session = request.getSession(false);
		PermissionSet userPerms = (PermissionSet) session
				.getAttribute(WebAppConstants.PERMISSIONS);
		if (!userPerms.getPermissionFor(Permission.UILOCALE_SET_DEFAULT)) 
		{
			request.setAttribute("restricted_access", true);
			if (userPerms.getPermissionFor(Permission.UILOCALE_VIEW)) 
			{
				response
						.sendRedirect("/globalsight/ControlServlet?activityName=uiLocaleConfiguration");
			} 
			else 
			{
				response.sendRedirect(request.getContextPath());
			}
			return;
		}
        logger.debug("setdefault ui locale...");

        try
        {
            String uilocale = request.getParameter("selectedLocale");
            boolean isexisted = isExisted(uilocale);

            if (isexisted)
            {
                updateDefaultUILocale(uilocale);
            }
        }
        catch (Exception e)
        {
            logger.error("setdefault", e);
            throw e;
        }

        logger.debug("setdefault ui locale finished");
    }

    /**
     * Sets ui locales for display table.
     */
    @Override
    public void afterAction(HttpServletRequest request, HttpServletResponse response)
    {
        logger.debug("Setting table vales...");

        HttpSession session = request.getSession(false);
        Locale uiLocale = (Locale) session.getAttribute(WebAppConstants.UILOCALE);

        setTableNavigation(request, session, getUILocales(uiLocale), new UILocaleComparator(
                uiLocale), 10, UILocaleConstant.UILOCALES, UILocaleConstant.UILOCALE_KEY);

        logger.debug("Setting table vales finished");
    }

    @Override
    public void beforeAction(HttpServletRequest request, HttpServletResponse response)
    {
		// gbs-1389: restrict direct access to language configuration
		// page without view permission
		HttpSession session = request.getSession(false);
		PermissionSet userPerms = (PermissionSet) session
				.getAttribute(WebAppConstants.PERMISSIONS);
		try 
		{
			if (!userPerms.getPermissionFor(Permission.UILOCALE_VIEW)) 
			{
				request.setAttribute("restricted_access", true);
				response.sendRedirect(request.getContextPath());
				return;
			}
		} 
		catch (Exception e) 
		{
			logger.error("Error happens when redirecting restricted access.");
		}
        initialize();
    }

    /**
     * Check if this ui locale exists
     * 
     * @param name
     * @return
     */
    private boolean isExisted(String name)
    {
        if (m_supportedLocales != null && name != null)
        {
            return m_supportedLocales.contains(name);
        }

        return false;
    }

    private boolean isRemoveable(String name)
    {
        if ("en_US".equalsIgnoreCase(name))
            return false;

        if (m_defaultLocale.equalsIgnoreCase(name))
            return false;

        if (isExisted(name) && m_supportedLocales.size() > 1)
            return true;

        return false;
    }

    /**
     * get all the ui locales
     * 
     * @return all ui locales
     * @throws RemoteException
     */
    private List getUILocales(Locale uilocle)
    {
        List data = new ArrayList();

        if (m_supportedLocales != null)
        {
            Vector sources = null;

            try
            {
                sources = UILocaleManager.getAvailableLocales();
            }
            catch (Exception e)
            {
                logger.error("Error in getUILocales", e);
            }

            for (String loc : m_supportedLocales)
            {
                UILocale uil = new UILocale();
                uil.setShortName(loc);
                uil.setLongName(getLongName(sources, loc, uilocle));
                uil.setDefaultLocale(loc.equalsIgnoreCase(m_defaultLocale));

                data.add(uil);
            }
        }

        return data;
    }

    private String getLongName(Vector sources, String loc, Locale uilocale)
    {
        if (sources == null)
            return loc;

        for (Object obj : sources)
        {
            GlobalSightLocale gsl = (GlobalSightLocale) obj;
            if (gsl.toString().equalsIgnoreCase(loc))
                return gsl.getDisplayName(uilocale);
        }

        return loc;
    }

    // Only populate the values once.
    private void initialize()
    {
        try
        {
            m_supportedLocales = UILocaleManager.getSystemUILocaleStrings();

            String defaultLocale = UILocaleManager.getSystemUILocaleDefaultString();
            m_defaultLocale = defaultLocale;
        }
        catch (Exception e)
        {
            m_supportedLocales = new ArrayList<String>();
            m_supportedLocales.add("en_US");
            m_defaultLocale = "en_US";
        }
    }

    private void updateDefaultUILocale(String uilocale)
    {
        SystemParameter sp = UILocaleManager
                .getSystemParameter(SystemConfigParamNames.DEFAULT_UI_LOCALE);
        sp.setValue(uilocale);
        UILocaleManager.updateSystemParameter(sp);

        initialize();
    }

    private void updateUILocales(String newValue)
    {
        SystemParameter sp = UILocaleManager.getSystemParameter(SystemConfigParamNames.UI_LOCALES);
        sp.setValue(newValue);
        UILocaleManager.updateSystemParameter(sp);

        initialize();
    }

    private void removeLocaleResouces(String uilocale)
    {
        // TODO to be decide
    }

}
