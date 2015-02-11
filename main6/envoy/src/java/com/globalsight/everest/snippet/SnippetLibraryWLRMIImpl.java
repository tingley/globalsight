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

package com.globalsight.everest.snippet;

import com.globalsight.util.GlobalSightLocale;
import com.globalsight.everest.util.system.RemoteServer;
import com.globalsight.importer.IImportManager;
import com.globalsight.importer.ImporterException;

import java.util.ArrayList;
import java.util.Collection;

import java.rmi.RemoteException;

/**
 * This class represents the remote implementation of the
 * SnippetLibrary that manages snippets.
 *
 * Note that all of the methods of this class throw the following
 * exceptions:
 * 1. SnippetException - For snippet related errors.
 * 2. RemoteException - For network related exception.
 */
public class SnippetLibraryWLRMIImpl
    extends RemoteServer
    implements SnippetLibraryWLRemote
{
    // passes all calls off to the local instance (serves as a proxy)
    private SnippetLibraryLocal m_localInstance = null;

    /**
     * Construct a remote Snippet Library.
     *
     * @exception java.rmi.RemoteException Network related exception.
     */
    public SnippetLibraryWLRMIImpl()
        throws RemoteException, SnippetException
    {
        super(SnippetLibrary.SERVICE_NAME);
        m_localInstance = new SnippetLibraryLocal();
    }

    /**
     * @see SnippetLibrary.addSnippet(Snippet, boolean)
     */
    public Snippet addSnippet(String p_user, Snippet p_snippet,
        boolean p_validate)
        throws RemoteException, SnippetException
    {
        return m_localInstance.addSnippet(p_user, p_snippet, p_validate);
    }

    /**
     * @see SnippetLibrary.addSnippet(String, String, String, String,
     * String, boolean)
     */
    public Snippet addSnippet(String p_user, String p_name, String p_desc,
        String p_locale, String p_version, String p_value, boolean p_validate)
        throws RemoteException, SnippetException
    {
        return m_localInstance.addSnippet(p_user, p_name, p_desc, p_locale,
            p_version, p_value, p_validate);
    }

    /**
     * @see SnippetLibrary.cloneSnippet(Snippet)
     */
    public Snippet cloneSnippet(String p_user, Snippet p_snippet)
        throws RemoteException, SnippetException
    {
        return m_localInstance.cloneSnippet(p_user, p_snippet);
    }

    /**
     * @see SnippetLibrary.cloneSnippet(Snippet, GlobalSightLocale)
     */
    public Snippet cloneSnippet(String p_user, Snippet p_snippet,
        GlobalSightLocale p_locale)
        throws RemoteException, SnippetException
    {
        return m_localInstance.cloneSnippet(p_user, p_snippet, p_locale);
    }

    /**
     * @see SnippetLibrary.cloneSnippet(Snippet, String)
     */
    public Snippet cloneSnippet(String p_user, Snippet p_snippet,
        String p_localeAsString)
        throws RemoteException, SnippetException
    {
        return m_localInstance.cloneSnippet(
            p_user, p_snippet, p_localeAsString);
    }

    /**
     * @see SnippetLibrary.getGenericSnippetNames()
     */
    public ArrayList getGenericSnippetNames()
        throws RemoteException, SnippetException
    {
        return m_localInstance.getGenericSnippetNames();
    }

    /**
     * @see SnippetLibrary.getSnippetsByLocale(GlobalSightLocale)
     */
    public ArrayList getSnippetsByLocale(GlobalSightLocale p_locale)
        throws RemoteException, SnippetException
    {
        return m_localInstance.getSnippetsByLocale(p_locale);
    }

    /**
     * @see SnippetLibrary.getSnippetsByLocale(String)
     */
    public ArrayList getSnippetsByLocale(String p_localeAsString)
        throws RemoteException, SnippetException
    {
        return m_localInstance.getSnippetsByLocale(p_localeAsString);
    }

    /**
     * @see SnippetLibrary.getSnippets()
     */
    public ArrayList getSnippets()
        throws RemoteException, SnippetException
    {
        return m_localInstance.getSnippets();
    }

    /**
     * @see SnippetLibrary.getSnippets(String)
     */
    public ArrayList getSnippets(String p_name)
        throws RemoteException, SnippetException
    {
        return m_localInstance.getSnippets(p_name);
    }

    /**
     * @see SnippetLibrary.getSnippets(String, GlobalSightLocale)
     */
    public ArrayList getSnippets(String p_name, GlobalSightLocale p_locale)
        throws RemoteException, SnippetException
    {
        return m_localInstance.getSnippets(p_name, p_locale);
    }

    /**
     * @see SnippetLibrary.getSnippets(String, String)
     */
    public ArrayList getSnippets(String p_name, String p_localeAsString)
        throws RemoteException, SnippetException
    {
        return m_localInstance.getSnippets(p_name, p_localeAsString);
    }

    /**
     * @see SnippetLibrary.getSnippets(Collection, GlobalSightLocale)
     */
    public ArrayList getSnippets(Collection p_names, GlobalSightLocale p_locale)
        throws RemoteException, SnippetException
    {
        return m_localInstance.getSnippets(p_names, p_locale);
    }

    /**
     * @see SnippetLibrary.getSnippets(Collection, String)
     */
    public ArrayList getSnippets(Collection p_names, String p_localeAsString)
        throws RemoteException, SnippetException
    {
        return m_localInstance.getSnippets(p_names, p_localeAsString);
    }

    /**
     * @see SnippetLibrary.getSnippet(String, GlobalSightLocale, long)
     */
    public Snippet getSnippet(String p_name, GlobalSightLocale p_locale,
        long p_id)
        throws RemoteException, SnippetException
    {
        return m_localInstance.getSnippet(p_name, p_locale, p_id);
    }


    /**
     * @see SnippetLibrary.getSnippet(String, String, long)
     */
    public Snippet getSnippet(String p_name, String p_localeAsString,
        long p_id)
        throws RemoteException, SnippetException
    {
        return m_localInstance.getSnippet(p_name, p_localeAsString, p_id);
    }

    /**
     * @see SnippetLibrary.modifySnippet(Snippet, boolean)
     */
    public Snippet modifySnippet(String p_user, Snippet p_snippet,
        boolean p_validate)
        throws RemoteException, SnippetException
    {
        return m_localInstance.modifySnippet(p_user, p_snippet, p_validate);
    }

    public Snippet modifySnippet(String p_user, String p_name, String p_desc,
        String p_locale, String p_version, String p_value, boolean p_validate)
        throws RemoteException, SnippetException
    {
        return m_localInstance.modifySnippet(p_user, p_name, p_desc,
            p_locale, p_version, p_value, p_validate);
    }

    /**
     * @see SnippetLibrary.removeSnippet(Snippet)
     */
    public void removeSnippet(String p_user, Snippet p_snippet)
        throws RemoteException, SnippetException
    {
        m_localInstance.removeSnippet(p_user, p_snippet);
    }

    /**
     * @see SnippetLibrary.removeSnippet(String, GlobalSightLocale, long)
     */
    public void removeSnippet(String p_user, String p_name,
        GlobalSightLocale p_locale,
        long p_version)
        throws RemoteException, SnippetException
    {
        m_localInstance.removeSnippet(p_user, p_name, p_locale, p_version);
    }

    /**
     * @see SnippetLibrary.removeSnippet(String, String, String)
     */
    public void removeSnippet(String p_user, String p_name,
        String p_locale, String p_version)
        throws SnippetException, RemoteException
    {
        m_localInstance.removeSnippet(p_user, p_name, p_locale, p_version);
    }

    /**
     * see SnippetLibrary.removeSnippets(String)
     */
    public void removeSnippets(String p_user, String p_name)
        throws RemoteException, SnippetException
    {
        m_localInstance.removeSnippets(p_user, p_name);
    }

    /**
     * Returns an object that allows to import snippet files.
     *
     * @return an object that implements the general IImportManager
     * interface.
     */
    public IImportManager getImporter(String p_user)
        throws RemoteException, ImporterException
    {
        return m_localInstance.getImporter(p_user);
    }
}
