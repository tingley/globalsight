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

import com.globalsight.everest.tm.Tm;
import com.globalsight.everest.tm.TmManagerException;

import com.globalsight.everest.tm.searchreplace.SearchReplaceManager;
import com.globalsight.everest.tm.searchreplace.JobSearchReplaceManager;

import com.globalsight.importer.IImportManager;
import com.globalsight.importer.ImporterException;
import com.globalsight.exporter.IExportManager;
import com.globalsight.exporter.ExporterException;

import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.Vector;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The TmManager interface provides management functions of Tm
 * objects.
 */
public interface TmManager
    extends Remote
{
    /**
     * Retrieves the server name and version info.
     *
     * @return a string like "GlobalSight TM Version 4.5".
     */
    String getVersion()
        throws RemoteException;

    /**
     * Gets all TMs in an ArrayList (or, Collection).
     *
     * @deprecated Gold TM: use getNames() to retrieve a list of
     * names, then call getTm(name).
     */
    public Collection getAllTms()
        throws GeneralException, RemoteException;

    /**
     * Retrieves all the TM names.
     *
     * @return ArrayList of TM names (as String)
     * @throws TmManagerException when an error occurs.
     * @throws RemoteException when a communication-related error occurs.
     */
    ArrayList getNames()
        throws RemoteException;

    /**
     * Retrieves all the TM names, sorted in a UI locale.
     *
     * @return ArrayList of TM names (as String)
     * @throws TmManagerException when an error occurs.
     * @throws RemoteException when a communication-related error occurs.
     */
    ArrayList getNames(Locale uiLocale)
        throws RemoteException;

    /**
     * Retrieves the name of the TM with the given id.
     *
     * @return String if TM was found, else null.
     */
    String getTmName(long id)
        throws RemoteException;

    /**
     * Retrieves the id of the TM with the given name.
     *
     * @return long >= 0 if TM was found, else -1.
     */
    long getTmId(String name)
        throws RemoteException;

    /**
     * Returns a list of TM names and descriptions known to the server
     * sorted in the given locale.
     *
     * @param p_uiLocale -- the UI locale to use for sorting
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
    String getDescriptions(Locale uiLocale)
        throws RemoteException;

    /**
     * Returns the definition of a TM. If the argument
     * <code>clone</code> is true, a copy of the definition is
     * returned (without TM name).
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
    String getDefinition(String name, boolean clone)
        throws TmManagerException, RemoteException;

    /**
     * Returns a default TM definition that can be modified according
     * to taste (currently the empty definition <tm></tm>).
     */
    String getDefaultDefinition()
        throws RemoteException;

    /**
     * Retrieves a TM by name.
     *
     * @param p_name TM name.
     * @return Tm object
     * @throws TmManagerException when an error occurs.
     * @throws RemoteException when a communication-related error occurs.
     */
    Tm getTm(String name)
        throws TmManagerException, RemoteException;

    /**
     * Retrieves a TM by id.
     *
     * @param id TM id.
     * @return Tm object
     * @throws TmManagerException when an error occurs.
     * @throws RemoteException when a communication-related error occurs.
     */
    Tm getTmById(long id)
        throws GeneralException, RemoteException;

    /**
     * Create a TM and persist it.
     *
     * @param name Name of the TM.
     * @param domain Domain attribute of the TM.
     * @param organization Organization attribute of the TM.
     * @param description Description of the TM.
     * @throws TmManagerException when an error occurs.
     * @throws RemoteException when a communication-related error occurs.
     */
    Tm createTm(String name, String domain, String organization,
        String description)
        throws TmManagerException, RemoteException;

    /**
     * Update a TM and persist it.
     *
     * @param id TM id.
     * @param name Name of the TM.
     * @param domain Domain attribute of the TM.
     * @param organization Organization attribute of the TM.
     * @param description Description of the TM.
     * @throws TmManagerException when an error occurs.
     * @throws RemoteException when a communication-related error occurs.
     */
    Tm updateTm(long id, String name, String domain, String organization,
        String description)
        throws TmManagerException, RemoteException;

    /**
     * Deletes a TM from the system.
     */
    void deleteTm(String name)
        throws GeneralException, RemoteException;

    /**
     * Returns an import manager to import TMX files into a TM.
     */
    IImportManager getImporter(String name)
        throws RemoteException, ImporterException, TmManagerException;

    /**
     * Returns an export manager to export TMX files from a TM.
     */
    IExportManager getExporter(String name)
        throws RemoteException, ExporterException, TmManagerException;

    /**
     * Returns a manager for basic search and replace TM maintenance.
     *
     * @param p_tmNames list of tm names (String)
     */
    SearchReplaceManager getSearchReplacer(ArrayList p_tmNames)
        throws RemoteException, TmManagerException;

    JobSearchReplaceManager getJobSearchReplaceManager()
        throws RemoteException, TmManagerException;
}
