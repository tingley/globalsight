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

package com.globalsight.terminology.termleverager;

//
// globalsight imports
//
import com.globalsight.everest.util.system.RemoteServer;
import com.globalsight.everest.util.system.SystemStartupException;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.everest.page.SourcePage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.rmi.RemoteException;

public final class TermLeverageManagerWLImpl extends RemoteServer implements
        TermLeverageManagerWLRemote
{
    private TermLeverageManagerLocal m_termLeverageManagerLocal;

    public TermLeverageManagerWLImpl() throws RemoteException
    {
        super(TermLeverageManager.SERVICE_NAME);
        m_termLeverageManagerLocal = new TermLeverageManagerLocal();
    }

    /**
     * Bind the remote server to the ServerRegistry.
     * 
     * @throws SystemStartupException
     *             when a NamingException or other Exception occurs.
     */
    public void init() throws SystemStartupException
    {
        super.init();
    }

    //
    // TermLeverageManager interface methods
    //

    public TermLeverageResult leverageTerms(Collection p_tuvs,
            TermLeverageOptions p_options) throws GeneralException,
            RemoteException
    {
        return m_termLeverageManagerLocal.leverageTerms(p_tuvs, p_options);
    }

    public TermLeverageResult leverageTerms(Collection p_tuvs,
            TermLeverageOptions p_options, String p_companyId)
            throws GeneralException, RemoteException
    {
        return m_termLeverageManagerLocal.leverageTerms(p_tuvs, p_options,
                p_companyId);
    }

    public TermLeverageMatchResultSet getTermMatchesForPage(
            SourcePage p_sourcePage, GlobalSightLocale p_targetPageLocale)
            throws GeneralException, RemoteException
    {
        return m_termLeverageManagerLocal.getTermMatchesForPage(p_sourcePage,
                p_targetPageLocale);
    }

    public ArrayList<TermLeverageMatchResult> getTermMatchesForSegment(
            long p_srcTuvId, long p_subId, GlobalSightLocale p_targetPageLocale)
            throws GeneralException, RemoteException
    {
        return m_termLeverageManagerLocal.getTermMatchesForSegment(p_srcTuvId,
                p_subId, p_targetPageLocale);
    }
    
    public Map<Long, Set<TermLeverageMatch>> getTermMatchesForPages(
            Collection<SourcePage> p_sourcePages,
            GlobalSightLocale p_targetPageLocale) throws GeneralException,
            RemoteException
    {
        return m_termLeverageManagerLocal.getTermMatchesForPages(p_sourcePages,
                p_targetPageLocale);
    }
}
