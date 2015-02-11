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



package com.globalsight.everest.integration.ling.tm;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.globalsight.everest.util.system.RemoteServer;
import com.globalsight.everest.util.system.SystemStartupException;
import com.globalsight.ling.tm.LeverageProperties;
import com.globalsight.ling.tm.TuvLing;
import com.globalsight.ling.tm.fuzzy.FuzzyIndexManagerException;
import com.globalsight.util.GlobalSightLocale;

public final class FuzzyIndexManagerWLRMIImpl
    extends RemoteServer
    implements FuzzyIndexManagerWLRemote
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            FuzzyIndexManagerWLRMIImpl.class.getName());

    private FuzzyIndexManagerLocal m_fuzzyIndexManagerLocal = null;

    public FuzzyIndexManagerWLRMIImpl()
        throws RemoteException
    {
        super(SERVICE_NAME);
        m_fuzzyIndexManagerLocal = new FuzzyIndexManagerLocal();
    }

    /**
       Bind the remote server to the ServerRegistry.
       @throws SystemStartupException when a NamingException
       or other Exception occurs.
    */
    public void init()
        throws SystemStartupException
    {
        super.init();
        CATEGORY.debug("FuzzyIndexManagerWLRMIImpl started");
    }

    /**
       Query the FUZZY_INDEX table and get all matches for each token
       in p_tokens.  Limit the search to the supplied TM id and
       locale.

       @param p_tokens - A collection of Token objects (a hash table
       of tokens and their frequencies).
       @param p_locale - The locale of the Tuv used to generate the token.
       @param p_tmId - The id of the Tm we are searching.
       @return List of FuzzyCandidates sorted by fuzzy score.
    */
    public Collection getFuzzyCandidates(
        HashMap p_tokens,
        GlobalSightLocale p_locale,
        List p_tmIds,
        LeverageProperties p_leverageProperties)
        throws FuzzyIndexManagerException, RemoteException
    {
        return m_fuzzyIndexManagerLocal.getFuzzyCandidates(
            p_tokens,
            p_locale,
            p_tmIds,
            p_leverageProperties);
    }

    // CvdL
    public Collection getFuzzyCandidates(TuvLing p_originalSourceTuv,
        HashMap p_tokens, GlobalSightLocale p_locale, List p_tmIds,
        long p_locType, Collection p_leverageExcludeTypes,
        LeverageProperties p_leverageProperties)
        throws FuzzyIndexManagerException,
               RemoteException
    {
        return m_fuzzyIndexManagerLocal.getFuzzyCandidates(
            p_originalSourceTuv, p_tokens, p_locale, p_tmIds, p_locType,
            p_leverageExcludeTypes, p_leverageProperties);
    }

    /**
       For each token in p_tokens add a row to the FUZZY_INDEX table
       with the p_tuvId, token CRC, token count, locale and tm id.
       @param p_tokens - A collection of Token objects.
    */
    public void updateFuzzyIndex(Vector p_tokens)
        throws FuzzyIndexManagerException, RemoteException
    {
        m_fuzzyIndexManagerLocal.updateFuzzyIndex(p_tokens);
    }


//    /**
//     * @deprecated
//     *   Procedure "fuzzy_idx.crt_fuzzy_idx" is not exists now.
//     */
//    public void callIndexingProcedure(Vector p_paramList)
//        throws FuzzyIndexManagerException,
//               RemoteException
//    {
//        m_fuzzyIndexManagerLocal.callIndexingProcedure(p_paramList);
//    }
    public void persistFuzzyIndexes(Vector p_paramList)
        throws FuzzyIndexManagerException,
        RemoteException
    {
        m_fuzzyIndexManagerLocal.persistFuzzyIndexes(p_paramList);
    }
}
