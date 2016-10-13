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

package com.globalsight.everest.tm;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import org.apache.log4j.Logger;

import com.globalsight.everest.integration.ling.LingServerProxy;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.tm.exporter.ExportManager;
import com.globalsight.everest.tm.importer.ImportManager;
import com.globalsight.everest.tm.searchreplace.JobSearchReplaceManager;
import com.globalsight.everest.tm.searchreplace.JobSearchReplaceManagerLocal;
import com.globalsight.everest.tm.searchreplace.SearchReplaceManager;
import com.globalsight.everest.tm.searchreplace.SearchReplaceManagerLocal;
import com.globalsight.everest.util.comparator.LocaleComparator;
import com.globalsight.everest.util.system.RemoteServer;
import com.globalsight.everest.util.system.SystemShutdownException;
import com.globalsight.everest.util.system.SystemStartupException;
import com.globalsight.exporter.ExporterException;
import com.globalsight.exporter.IExportManager;
import com.globalsight.importer.IImportManager;
import com.globalsight.importer.ImporterException;
import com.globalsight.ling.tm2.SegmentTmTu;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.ling.tm2.segmenttm.TuReader;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.SessionInfo;
import com.globalsight.util.SortUtil;

/**
 * The TmManagerLocal class is an implementation of TmManager interface.
 */
public final class TmManagerLocal extends RemoteServer implements TmManager,
        TmManagerExceptionMessages
{
    private static final Logger CATEGORY = Logger
            .getLogger(TmManagerLocal.class);

    /** Name column width in database */
    public static final int MAX_NAME_LEN = 40;
    /** Domain column width in database */
    public static final int MAX_DOMAIN_LEN = 200;
    /** Organization column width in database */
    public static final int MAX_ORGANIZATION_LEN = 200;
    /** Description column width in database */
    public static final int MAX_DESCRIPTION_LEN = 4000;

    /**
     * Constructs a TmManagerLocal.
     */
    public TmManagerLocal() throws RemoteException
    {
        super(RemoteServer.getServiceName(TmManager.class));
    }

    //
    // RemoteServer method overwrites
    //

    /**
     * <p>
     * Binds the remote server to the ServerRegistry.
     * </p>
     * 
     * @throws SystemStartupException
     *             when a NamingException or other Exception occurs.
     */
    public void init() throws SystemStartupException
    {
        super.init();
    }

    /**
     * <p>
     * Unbinds the remote server from the ServerRegistry.
     * </p>
     * 
     * @throws SystemShutdownException
     *             when a NamingException or other Exception occurs.
     */
    public void destroy() throws SystemShutdownException
    {
        super.destroy();
    }

    //
    // TmManager interface methods
    //

    /**
     * Returns a list of languages contained in the TM, as GlobalSightLocale
     * objects, and sorted by the given uiLocale. If the list is empty, it means
     * the TM is empty.
     * 
     * @param p_uiLocale
     *            if null, Locale.US is used.
     */
    static public ArrayList<GlobalSightLocale> getProjectTmLocales(
            String p_name, Locale p_uiLocale) throws TmManagerException,
            RemoteException
    {
        if (p_name == null || p_name.length() == 0)
        {
            String[] args =
            { "name is null" };
            throw new TmManagerException(MSG_INVALID_ARG, args, null);
        }

        Tm tm = null;
        try
        {
            tm = ServerProxy.getProjectHandler().getProjectTMByName(p_name, false);
        }
        catch (Throwable ignore)
        {
        }

        if (tm == null)
        {
            String[] args =
            { p_name };
            throw new TmManagerException(MSG_TM_DOES_NOT_EXIST, args, null);
        }

        if (p_uiLocale == null)
        {
            p_uiLocale = Locale.US;
        }

        return doGetProjectTmLocales(tm, p_uiLocale);
    }

    /** Returns an import manager to import TMX files into a TM. */
    static public IImportManager getProjectTmImporter(String p_name)
            throws ImporterException, TmManagerException
    {
        if (p_name == null || p_name.length() == 0)
        {
            String[] args =
            { "name is null" };
            throw new TmManagerException(MSG_INVALID_ARG, args, null);
        }

        Tm tm = null;
        try
        {
            tm = ServerProxy.getProjectHandler().getProjectTMByName(p_name, false);
        }
        catch (Throwable ignore)
        {
        }

        if (tm == null)
        {
            String[] args =
            { p_name };
            throw new TmManagerException(MSG_TM_DOES_NOT_EXIST, args, null);
        }

        return new ImportManager(tm, new SessionInfo("", ""));
    }

    /** Returns an export manager to export TMs to TMX files. */
    static public IExportManager getProjectTmExporter(String p_name)
            throws ExporterException, TmManagerException
    {
        if (p_name == null || p_name.length() == 0)
        {
            String[] args =
            { "name is null" };
            throw new TmManagerException(MSG_INVALID_ARG, args, null);
        }

        Tm tm = null;
        try
        {
            tm = ServerProxy.getProjectHandler().getProjectTMByName(p_name, false);
        }
        catch (Throwable ignore)
        {
        }

        if (tm == null)
        {
            String[] args =
            { p_name };
            throw new TmManagerException(MSG_TM_DOES_NOT_EXIST, args, null);
        }

        return new ExportManager(tm, new SessionInfo("", ""));
    }

    /**
     * Returns a manager for basic search and replace TM maintenance.
     * 
     * @param p_tmNames
     *            list of tm names (String)
     */
    public SearchReplaceManager getSearchReplacer(ArrayList p_tmNames)
            throws RemoteException, TmManagerException
    {
        if (p_tmNames == null || p_tmNames.size() == 0)
        {
            String[] args =
            { "name is null" };
            throw new TmManagerException(MSG_INVALID_ARG, args, null);
        }

        ArrayList selectedTms = new ArrayList();
        try
        {
            Collection allTms = ServerProxy.getProjectHandler()
                    .getAllProjectTMs();

            for (Iterator it = allTms.iterator(); it.hasNext();)
            {
                Tm temp = (Tm) it.next();

                if (p_tmNames.contains(temp.getName()))
                {
                    selectedTms.add(temp);
                }
            }
        }
        catch (Throwable ignore)
        {
        }

        if (selectedTms.size() == 0)
        {
            String[] args =
            { p_tmNames.toString() };
            throw new TmManagerException(MSG_TM_DOES_NOT_EXIST, args, null);
        }

        return new SearchReplaceManagerLocal(selectedTms, new SessionInfo("",
                ""));
    }

    /**
     * Returns a manager for basic search and replace TM maintenance.
     */
    public JobSearchReplaceManager getJobSearchReplaceManager()
            throws RemoteException, TmManagerException
    {
        return new JobSearchReplaceManagerLocal();
    }

    /**
     * Returns the locales stored in the TM.
     * 
     * @return ArrayList with GlobalSightLocales sorted by display name in
     *         uiLocale.
     */
    @SuppressWarnings("unchecked")
    static private ArrayList<GlobalSightLocale> doGetProjectTmLocales(Tm p_tm,
            Locale p_uiLocale) throws TmManagerException
    {
        Set<GlobalSightLocale> locales = Collections.emptySet();
        try
        {
            locales = LingServerProxy.getTmCoreManager().getTmLocales(p_tm);
        }
        catch (Exception e)
        {
            CATEGORY.warn("can't read TM locale data", e);
        }

        ArrayList<GlobalSightLocale> result = new ArrayList<GlobalSightLocale>(
                locales);
        SortUtil.sort(result, new LocaleComparator(2, p_uiLocale));
        return result;
    }
}
