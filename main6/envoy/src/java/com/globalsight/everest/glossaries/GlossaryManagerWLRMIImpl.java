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

package com.globalsight.everest.glossaries;

import com.globalsight.everest.glossaries.GlossaryException;
import com.globalsight.everest.util.system.RemoteServer;
import com.globalsight.util.GlobalSightLocale;

import java.util.ArrayList;
import java.util.Collection;

import java.rmi.RemoteException;

public class GlossaryManagerWLRMIImpl
    extends RemoteServer
    implements GlossaryManagerWLRemote
{
    private GlossaryManager m_localInstance;

    public GlossaryManagerWLRMIImpl()
        throws RemoteException
    {
        super(GlossaryManager.SERVICE_NAME);
        m_localInstance = new GlossaryManagerLocal();
    }

    /**
     * Persists a new glossary file in data store.
     */
    public void createGlossary(GlossaryFile p_file)
        throws RemoteException, GlossaryException
    {
        m_localInstance.createGlossary(p_file);
    }

    /**
     * Deletes the specified glossary file.
     */
    public void deleteGlossary(GlossaryFile p_file)
        throws RemoteException, GlossaryException
    {
        m_localInstance.deleteGlossary(p_file);
    }

    /**
     * Returns the absolute file name for the specified glossary file.
     */
    public String getFilename(GlossaryFile p_file, String companyId)
        throws RemoteException
    {
        return m_localInstance.getFilename(p_file, companyId);
    }

    /**
     * Returns a list of absolute file names for the glossary files
     * specified in the argument (as Strings).
     */
    public ArrayList getFilenames(Collection p_files)
        throws RemoteException
    {
        return m_localInstance.getFilenames(p_files);
    }

    /**
     * Retrieves a glossary file descriptor based on in its id.
     */
    public GlossaryFile getGlossary(long p_id)
        throws RemoteException, GlossaryException
    {
        return m_localInstance.getGlossary(p_id);
    }

    /**
     * Retrieves a list of glossary file descriptors matching the
     * given source/target locale (and category).
     */
    public ArrayList /* of GlossaryFile */ getGlossaries(
        GlobalSightLocale p_sourceLocale, GlobalSightLocale p_targetLocale,
        String p_category, String companyId)
        throws RemoteException, GlossaryException
    {
        return m_localInstance.getGlossaries(p_sourceLocale,
            p_targetLocale, p_category, companyId);
    }

}

