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

// hack.
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.globalsight.everest.integration.ling.LingServerProxy;
import com.globalsight.everest.localemgr.LocaleManager;
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
import com.globalsight.ling.tm2.segmenttm.TuReader;
import com.globalsight.log.GlobalSightCategory;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.terminology.util.SqlUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.SessionInfo;
import com.globalsight.util.edit.EditUtil;
/**
 * The TmManagerLocal class is an implementation of TmManager
 * interface.
 */
public final class TmManagerLocal
    extends RemoteServer
    implements TmManager, TmManagerExceptionMessages
{
    private static final GlobalSightCategory CATEGORY =
        (GlobalSightCategory)GlobalSightCategory.getLogger(
            TmManagerLocal.class);

    /** Name column width in database */
    public static final int MAX_NAME_LEN = 40;
    /** Domain column width in database */
    public static final int MAX_DOMAIN_LEN = 200;
    /** Organization column width in database */
    public static final int MAX_ORGANIZATION_LEN = 200;
    /** Description column width in database */
    public static final int MAX_DESCRIPTION_LEN = 4000;

    //
    // Member Variables
    //
    static public final String VERSION = "GlobalSight TM Version 4.5";

    /**
     * Constructs a TmManagerLocal.
     */
    public TmManagerLocal()
        throws RemoteException
    {
        super(RemoteServer.getServiceName(TmManager.class));
    }

    //
    // RemoteServer method overwrites
    //

    /**
     * <p>Binds the remote server to the ServerRegistry.</p>
     *
     * @throws SystemStartupException when a NamingException or other
     * Exception occurs.
     */
    public void init()
        throws SystemStartupException
    {
        super.init();
    }

    /**
     * <p>Unbinds the remote server from the ServerRegistry.</p>
     *
     * @throws SystemShutdownException when a NamingException or other
     * Exception occurs.
     */
    public void destroy()
        throws SystemShutdownException
    {
        super.destroy();
    }

    //
    // TmManager interface methods
    //

    /**
     * Retrieves the server name and version info.
     *
     * @return a string like "GlobalSight Termbase Version 1.0".
     */
    public String getVersion()
        throws RemoteException
    {
        return VERSION;
    }

    /**
     * Gets all TMs in an ArrayList (or, Collection).
     */
    public Collection getAllTms()
        throws RemoteException, GeneralException
    {
        // return TmList.getAll();

        Collection tms = null;

        try
        {
        	String hql = "from TmImpl";
        	tms = HibernateUtil.search(hql);
        	
        }
        catch (Exception e)
        {
            CATEGORY.error(e.toString(), e);
            throw new GeneralException(e.toString(), e);
        }

        return tms;
    }

    /**
     * Retrieves all the TM names, sorted by locale en_US.
     *
     * @return ArrayList of TM names (as String)
     * @throws TmManagerException when an error occurs.
     * @throws RemoteException when a communication-related error occurs.
     */
    public ArrayList getNames()
        throws RemoteException
    {
        return TmList.getNames();
    }

    /**
     * Retrieves all the TM names, sorted by a UI locale.
     *
     * @return ArrayList of TM names (as String)
     * @throws TmManagerException when an error occurs.
     * @throws RemoteException when a communication-related error occurs.
     */
    public ArrayList getNames(Locale p_uiLocale)
        throws RemoteException
    {
        return TmList.getNames(p_uiLocale);
    }


    /**
     * Retrieves the name of the TM with the given id.
     *
     * @return String if TM was found, else null.
     */
    public String getTmName(long p_id)
        throws RemoteException
    {
        Tm tm = TmList.get(p_id);

        if (tm != null)
        {
            return tm.getName();
        }

        return null;
    }

    /**
     * Retrieves the id of the TM with the given name.
     *
     * @return long >= 0 if TM was found, else -1.
     */
    public long getTmId(String p_name)
        throws RemoteException
    {
        Tm tm = TmList.get(p_name);

        if (tm != null)
        {
            return tm.getId();
        }

        return -1;
    }

    /**
     * Returns a list of TM names and descriptions known to
     * the server sorted in the given locale.
     *
     * @param p_uiLocale -- the UI locale to use for sorting
     * @return an XML string:
     * <tms>
     *   <tm>
     *     <name>NAME</name>
     *     <domain>DESC</domain>
     *     <organization>DESC</organization>
     *     <description>DESC</description>
     *   </tm>
     * </tms>
     *
     * @throws TmManagerException when an error occurs.
     * @throws RemoteException when a communication-related error occurs.
     */
    public String getDescriptions(Locale p_uiLocale)
        throws RemoteException
    {
        return TmList.getDescriptions(p_uiLocale);
    }

    /**
     * Returns the definition of a TM.
     *
     * @return an XML string:
     * <tms>
     *   <tm id="1000">
     *     <name>NAME</name>
     *     <domain>DESC</domain>
     *     <organization>DESC</organization>
     *     <description>DESC</description>
     *     ...
     *   </tm>
     * </tms>
     *
     * @throws TmManagerException when an error occurs.
     * @throws RemoteException when a communication-related error occurs.
     */
    public String getDefinition(String p_name, boolean p_clone)
        throws TmManagerException, RemoteException
    {
        if (p_name == null || p_name.length() == 0)
        {
            throw new TmManagerException(MSG_INVALID_NAME, null, null);
        }

        TmImpl tm = TmList.get(p_name);

        if (tm == null)
        {
            CATEGORY.error("TM " + p_name + " does not exist");

            String[] args = { p_name };

            throw new TmManagerException(MSG_TM_DOES_NOT_EXIST, args, null);
        }

        // The definition of a TM is its standard attributes - for now.

        return TmList.tmAsXml(tm, p_clone);
    }

    /**
     * Returns a default TM definition that can be modified according
     * to taste (currently the empty definition <tm></tm>).
     */
    public String getDefaultDefinition()
        throws RemoteException
    {
        return "<tm><name></name><domain></domain><organization></organization><description></description></tm>";
    }

    /**
     * Retrieves a TM by name.
     */
    public Tm getTm(String p_name)
        throws TmManagerException, RemoteException
    {
        if (p_name == null || p_name.length() == 0)
        {
            throw new TmManagerException(MSG_INVALID_NAME, null, null);
        }

        Tm tm = TmList.get(p_name);

        if (tm == null)
        {
            CATEGORY.error("TM " + p_name + " does not exist");

            String[] args = { p_name };

            throw new TmManagerException(MSG_TM_DOES_NOT_EXIST, args, null);
        }

        return tm;
    }

    /**
     * Retrieves a TM by id.
     *
     * @param p_id TM id.
     * @return Tm object, or null if the TM does not exist.
     * @throws RemoteException when a communication-related error occurs.
     */
    public Tm getTmById(long p_id)
        throws RemoteException, GeneralException
    {
        return getTmById(p_id, false);
    }

    /**
     * Create a TM and persist it.
     */
    public Tm createTm(String p_name, String p_domain,
        String p_organization, String p_description)
        throws TmManagerException, RemoteException
    {
        if (p_name == null || p_name.length() == 0)
        {
            throw new TmManagerException(MSG_INVALID_NAME, null, null);
        }

        if (p_domain == null || p_organization == null ||
            p_description == null)
        {
            String[] args = { "a required attribute is null" };
            throw new TmManagerException(MSG_INVALID_ARG, args, null);
        }

        Tm tm = TmList.get(p_name);

        if (tm != null)
        {
            String[] args = { p_name };
            throw new TmManagerException(MSG_TM_DOES_EXIST, args, null);
        }

        return doCreateTm(p_name, p_domain, p_organization, p_description);
    }

    /**
     * Update a TM and persist it.
     */
    public Tm updateTm(long p_id, String p_name, String p_domain,
        String p_organization, String p_description)
        throws TmManagerException, RemoteException
    {
        if (p_id <= 0)
        {
            String[] args = { "TM id is <= 0" };
            throw new TmManagerException(MSG_INVALID_ARG, args, null);
        }

        if (p_name == null || p_name.length() == 0)
        {
            String[] args = { "name is null" };
            throw new TmManagerException(MSG_INVALID_ARG, args, null);
        }

        Tm tm = TmList.get(p_name);

        if (tm == null)
        {
            CATEGORY.error("TM " + p_name + " does not exist");

            String[] args = { p_name };

            throw new TmManagerException(MSG_TM_DOES_NOT_EXIST, args, null);
        }

        return doUpdateTm(p_id, p_name, p_domain, p_organization, p_description);
    }

    /**
     * Deletes a TM from the system.
     */
    public void deleteTm(String p_name)
        throws GeneralException, RemoteException
    {
        throw new GeneralException(new Exception("not implemented yet"));
    }

    /**
     * Returns a single TU from the TM (for debugging).
     *
     * @return an XML string with languages sorted by p_uiLocale (if
     * null, Locale.US is used).
     * 
     * @deprecated This does not work with tm3 and should not be used
     */
    static public String getProjectTmTu(String p_name, long p_tuId)
        throws TmManagerException
    {
        if (p_name == null || p_name.length() == 0)
        {
            String[] args = { "name is null" };
            throw new TmManagerException(MSG_INVALID_ARG, args, null);
        }

        if (p_tuId <= 0)
        {
            String[] args = { "tu id is invalid" };
            throw new TmManagerException(MSG_INVALID_ARG, args, null);
        }

        Tm tm = null;
        Collection tms;

        try
        {
            tms = ServerProxy.getProjectHandler().getAllProjectTMs();

            for (Iterator it = tms.iterator(); it.hasNext(); )
            {
                Tm temp = (Tm)it.next();

                if (temp.getName().equals(p_name))
                {
                    tm = temp;
                    break;
                }
            }
        }
        catch (Throwable ignore)
        {
        }

        if (tm == null)
        {
            String[] args = { p_name };
            throw new TmManagerException(MSG_TM_DOES_NOT_EXIST, args, null);
        }

        SegmentTmTu result = null;

        ArrayList args = new ArrayList();
        args.add(new Long(p_tuId));

        try
        {
            TuReader reader = new TuReader(HibernateUtil.getSession());

            reader.batchReadTus(args, 0, args.size(), null);
            result = reader.getNextTu();
            reader.batchReadDone();

            return result.toDebugString(true);
        }
        catch (Exception e)
        {
            throw new TmManagerException(MSG_INTERNAL_ERROR, null, e);
        }
    }

    /**
     * Returns a list of languages contained in the TM, as
     * GlobalSightLocale objects, and sorted by the given uiLocale. If
     * the list is empty, it means the TM is empty.
     *
     * @param p_uiLocale if null, Locale.US is used.
     */
    static public ArrayList<GlobalSightLocale> getProjectTmLocales(String p_name, Locale p_uiLocale)
        throws TmManagerException, RemoteException
    {
        if (p_name == null || p_name.length() == 0)
        {
            String[] args = { "name is null" };
            throw new TmManagerException(MSG_INVALID_ARG, args, null);
        }

        Tm tm = null;
        Collection tms;

        try
        {
            tms = ServerProxy.getProjectHandler().getAllProjectTMs();

            for (Iterator it = tms.iterator(); it.hasNext(); )
            {
                Tm temp = (Tm)it.next();

                if (temp.getName().equals(p_name))
                {
                    tm = temp;
                    break;
                }
            }
        }
        catch (Throwable ignore)
        {
        }

        if (tm == null)
        {
            String[] args = { p_name };
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
            String[] args = { "name is null" };
            throw new TmManagerException(MSG_INVALID_ARG, args, null);
        }

        Tm tm = null;
        Collection tms;

        try
        {
            tms = ServerProxy.getProjectHandler().getAllProjectTMs();

            for (Iterator it = tms.iterator(); it.hasNext(); )
            {
                Tm temp = (Tm)it.next();

                if (temp.getName().equals(p_name))
                {
                    tm = temp;
                    break;
                }
            }
        }
        catch (Throwable ignore)
        {
        }

        if (tm == null)
        {
            String[] args = { p_name };
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
            String[] args = { "name is null" };
            throw new TmManagerException(MSG_INVALID_ARG, args, null);
        }

        Tm tm = null;
        Collection tms;

        try
        {
            tms = ServerProxy.getProjectHandler().getAllProjectTMs();

            for (Iterator it = tms.iterator(); it.hasNext(); )
            {
                Tm temp = (Tm)it.next();

                if (temp.getName().equals(p_name))
                {
                    tm = temp;
                    break;
                }
            }
        }
        catch (Throwable ignore)
        {
        }

        if (tm == null)
        {
            String[] args = { p_name };
            throw new TmManagerException(MSG_TM_DOES_NOT_EXIST, args, null);
        }

        return new ExportManager(tm, new SessionInfo("", ""));
    }

    /** Returns an import manager to import TMX files into a TM. */
    public IImportManager getImporter(String p_name)
        throws ImporterException, TmManagerException, RemoteException
    {
        if (p_name == null || p_name.length() == 0)
        {
            String[] args = { "name is null" };
            throw new TmManagerException(MSG_INVALID_ARG, args, null);
        }

        Tm tm = null;
        Collection tms;

        try
        {
            tms = ServerProxy.getProjectHandler().getAllProjectTMs();

            for (Iterator it = tms.iterator(); it.hasNext(); )
            {
                Tm temp = (Tm)it.next();

                if (temp.getName().equals(p_name))
                {
                    tm = temp;
                    break;
                }
            }
        }
        catch (Throwable ignore)
        {
        }

        if (tm == null)
        {
            String[] args = { p_name };
            throw new TmManagerException(MSG_TM_DOES_NOT_EXIST, args, null);
        }

        return new ImportManager(tm, new SessionInfo("", ""));
    }

    /** Returns an export manager to export TMX files from a TM. */
    public IExportManager getExporter(String p_name)
        throws ExporterException, TmManagerException, RemoteException
    {
        if (p_name == null || p_name.length() == 0)
        {
            String[] args = { "name is null" };
            throw new TmManagerException(MSG_INVALID_ARG, args, null);
        }

        Tm tm = null;
        Collection tms;

        try
        {
            tms = ServerProxy.getProjectHandler().getAllProjectTMs();

            for (Iterator it = tms.iterator(); it.hasNext(); )
            {
                Tm temp = (Tm)it.next();

                if (temp.getName().equals(p_name))
                {
                    tm = temp;
                    break;
                }
            }
        }
        catch (Throwable ignore)
        {
        }

        if (tm == null)
        {
            String[] args = { p_name };
            throw new TmManagerException(MSG_TM_DOES_NOT_EXIST, args, null);
        }

        return new ExportManager(tm, new SessionInfo("", ""));
    }

    /**
     * Returns a manager for basic search and replace TM maintenance.
     *
     * @param p_tmNames list of tm names (String)
     */
    public SearchReplaceManager getSearchReplacer(ArrayList p_tmNames)
        throws RemoteException, TmManagerException
    {
        if (p_tmNames == null || p_tmNames.size() == 0)
        {
            String[] args = { "name is null" };
            throw new TmManagerException(MSG_INVALID_ARG, args, null);
        }

        ArrayList selectedTms = new ArrayList();
        try
        {
            Collection allTms
                = ServerProxy.getProjectHandler().getAllProjectTMs();

            for (Iterator it = allTms.iterator(); it.hasNext(); )
            {
                Tm temp = (Tm)it.next();

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
            String[] args = { p_tmNames.toString() };
            throw new TmManagerException(MSG_TM_DOES_NOT_EXIST, args, null);
        }

        return new SearchReplaceManagerLocal(
            selectedTms, new SessionInfo("", ""));
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
     * @return ArrayList with GlobalSightLocales sorted by display
     * name in uiLocale.
     */
    static private ArrayList<GlobalSightLocale> doGetProjectTmLocales(Tm p_tm, Locale p_uiLocale)
        throws TmManagerException
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

        ArrayList<GlobalSightLocale> result = new ArrayList<GlobalSightLocale>(locales);
        Collections.sort(result, new LocaleComparator(2, p_uiLocale));
        return result;
    }

    /**
     * <p>Releases all TMs during system shutdown.</p>
     */
    static protected void shutdownTms()
    {
        ArrayList names = TmList.getNames();

        for (int i = 0; i < names.size(); i++)
        {
            String name = (String)names.get(i);
            TmList.remove(name);

            CATEGORY.info("Stopped TM " + name);
        }

    }

    static private Tm getTmById(long p_id, boolean p_makeEditable)
        throws GeneralException
    {
        Tm tm = null;
        try
        {
            tm = (Tm)HibernateUtil.get(TmImpl.class, new Long(p_id));
        }
        catch (Exception pe)
        {
            CATEGORY.error(pe, pe);
            throw new GeneralException(pe.toString(), pe);
        }

        if (tm == null)
        {
            CATEGORY.error("getTmById queryResult empty: " + p_id);
            String[] args = { String.valueOf(p_id) };
            throw new TmManagerException(
                MSG_TM_DOES_NOT_EXIST, args, null);
        }

        return tm;
    }

    static protected Tm doCreateTm(String p_name, String p_domain,
        String p_organization, String p_description)
        throws TmManagerException
    {
        Tm tm;
        String name = p_name;

        try
        {
            // Avoid writing too much data into the various field.

            if (p_name.length() > 0)
            {
                p_name = EditUtil.truncateUTF8Len(p_name, MAX_NAME_LEN);
            }
            if (p_domain.length() > 0)
            {
                p_domain = EditUtil.truncateUTF8Len(p_domain, MAX_DOMAIN_LEN);
            }
            if (p_organization.length() > 0)
            {
                p_organization = EditUtil.truncateUTF8Len(p_organization,
                    MAX_ORGANIZATION_LEN);
            }
            if (p_description.length() > 0)
            {
                p_description = EditUtil.truncateUTF8Len(p_description,
                    MAX_DESCRIPTION_LEN);
            }

            Timestamp now = new Timestamp(System.currentTimeMillis());

            tm = createPhysicalTm(name, p_domain, p_organization,
                p_description, now);

            TmList.add(tm.getName(), tm);

            CATEGORY.info("TM " + name + " created.");

            return tm;
        }
        catch (Exception ex)
        {
            CATEGORY.error("TM " + name + " could not be created.", ex);

            String[] args = { name };
            throw new TmManagerException (MSG_FAILED_TO_CREATE_TM, args, ex);
        }
    }

    static protected Tm doUpdateTm(long p_id, String p_name, String p_domain,
        String p_organization, String p_description)
        throws TmManagerException
    {
        Tm currentTm = null;

        try
        {
        	currentTm = getTmById(p_id, true);
            currentTm.setName(p_name);
            currentTm.setDomain(p_domain);
            currentTm.setOrganization(p_organization);
            currentTm.setDescription(p_description);
            HibernateUtil.update(currentTm);
        }
        catch (Exception ex)
        {
            CATEGORY.error("TM " + p_name + " could not be updated.", ex);

            String[] args = { p_name };
            throw new TmManagerException (MSG_FAILED_TO_UPDATE_TM, args, ex);
        }

        return currentTm;
    }

    static private Tm createPhysicalTm(String p_name, String p_domain,
        String p_organization, String p_description, Timestamp p_now)
        throws TmManagerException
    {
        Tm tm = new TmImpl(p_name);
        tm.setDomain(p_domain);
        tm.setOrganization(p_organization);
        tm.setDescription(p_description);

        try
        {
            HibernateUtil.save(tm);
        }
        catch (Exception e)
        {
            CATEGORY.error(e.getMessage(), e);

            String[] args = { p_name };

            throw new TmManagerException(MSG_FAILED_TO_CREATE_TM, args, e);
        }

        return tm;
    }


    /**
     * <p>Stops the in-memory TM object by setting it's status to
     * DELETED and removing it from the TmList. The object may still
     * be referenced by many clients, but all their calls to the
     * object will fail. Once the last client has released his
     * reference to the TM object, it will be garbage-collected.</p>
     */
    static protected void doDeleteTm(String p_name)
        throws TmManagerException
    {
        String[] args = { p_name };
        throw new TmManagerException (MSG_FAILED_TO_DELETE_TM, args, null);
    }
}

