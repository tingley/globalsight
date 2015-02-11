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

package com.globalsight.everest.tuv;

//
// globalsight imports
//
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.List;

import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.util.system.RemoteServer;
import com.globalsight.everest.util.system.SystemStartupException;
import com.globalsight.util.GlobalSightLocale;

public final class TuvManagerWLImpl extends RemoteServer implements
        TuvManagerWLRemote
{
    private TuvManagerLocal m_tuvManagerLocal;

    public TuvManagerWLImpl() throws RemoteException
    {
        super(RemoteServer.getServiceName(TuvManagerWLRemote.class));
        m_tuvManagerLocal = new TuvManagerLocal();
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
    // TuvManager interface methods
    //

    /**
     * Factory method for creating Tu instances. Does not persist Tu.
     * 
     * @param p_tmId
     *            Tm identifier.
     * @param p_dataType
     *            DataType.
     * @param p_tuType
     *            TuType.
     * @param p_localizableType
     *            LocalizableType.
     * @param p_pid
     *            paragraph id (= GXML blockId)
     * @return Tu
     * @throws TuvException
     *             when an error occurs.
     * @throws RemoteException
     *             when a communication-related error occurs.
     */
    public Tu createTu(long p_tmId, String p_dataType, TuType p_tuType,
            char p_localizableType, long p_pid) throws TuvException,
            RemoteException
    {
        return m_tuvManagerLocal.createTu(p_tmId, p_dataType, p_tuType,
                p_localizableType, p_pid);
    }

    /**
     * Factory method for creating Tuv instances. Does not persist Tuv.
     * 
     * @pararm p_wordCount word count in the segment text.
     * @pararm p_globalSightLocale GlobalSightLocale of the Tuv.
     * @param p_sourcePage
     *            SourcePage associated with the Tuv.
     * @return Tuv
     * @throws TuvException
     *             when an error occurs.
     * @throws RemoteException
     *             when a communication-related error occurs.
     */
    public Tuv createTuv(int p_wordCount,
            GlobalSightLocale p_globalSightLocale, SourcePage p_sourcePage)
            throws TuvException, RemoteException
    {
        return m_tuvManagerLocal.createTuv(p_wordCount, p_globalSightLocale,
                p_sourcePage);
    }

    /**
     * Get Tuvs in the specified locale for the export process.
     * 
     * @param p_sourcePage
     *            source locale page.
     * @param p_globalSightLocale
     *            GlobalSightLocale to return Tuvs in.
     * @return Collection Tuvs
     * @throws TuvException
     *             when an error occurs.
     * @throws RemoteException
     *             when a communication-related error occurs.
     */
    public Collection getExportTuvs(SourcePage p_sourcePage,
            GlobalSightLocale p_globalSightLocale) throws TuvException,
            RemoteException
    {
        return m_tuvManagerLocal.getExportTuvs(p_sourcePage,
                p_globalSightLocale);
    }

    /**
     * Gets PageSegments.
     * 
     * @param p_sourcePage
     *            source page.
     * @param p_trgLocales
     *            a collection of target locales
     * @return PageSegments.
     * @throws RemoteException
     *             when a communication-related error occurs.
     */
    public PageSegments getPageSegments(SourcePage p_sourcePage,
            Collection<GlobalSightLocale> p_trgLocales) throws TuvException,
            RemoteException
    {
        return m_tuvManagerLocal.getPageSegments(p_sourcePage, p_trgLocales);
    }

    /**
     * <p>
     * Saves the target segments from an offline edit.
     * </p>
     * 
     * @param p_tuvs
     *            List of Tuvs to be saved
     * @throws TuvException
     *             when an error occurs.
     */
    public void saveTuvsFromOffline(List<TuvImplVo> p_tuvs, long p_jobId)
            throws TuvException, RemoteException
    {
        m_tuvManagerLocal.saveTuvsFromOffline(p_tuvs, p_jobId);
    }

    /**
     * Clone a source Tuv and associate it with a target locale.
     * 
     * @param p_sourceLocaleTuv
     *            source locale Tuv.
     * @param p_targetLocale
     *            target locale to associate with the cloned Tuv.
     * @returns cloned Tuv associated it with a target locale.
     * @throws TuvException
     *             when an error occurs.
     * @throws RemoteException
     *             when a communication-related error occurs.
     */
    public Tuv cloneToTarget(Tuv p_sourceLocaleTuv,
            GlobalSightLocale p_targetLocale) throws TuvException,
            RemoteException
    {
        return m_tuvManagerLocal.cloneToTarget(p_sourceLocaleTuv,
                p_targetLocale);
    }

    /**
     * Clone the source page's tuv for a target page based on the specified
     * locale.
     * 
     * @param p_sourceLocaleTuv
     *            - The tuv to be cloned.
     * @param p_targetLocale
     *            - The locale of the tuv.
     * @param p_matchType
     *            match type that the Tuv p_gxml came from. Must be
     *            LeverageMatchType.LEVERAGE_GROUP_EXACT_MATCH or
     *            LeverageMatchType.EXACT_MATCH.
     * @returns cloned Tuv associated it with a target locale.
     * @throws TuvException
     *             when an error occurs.
     * @throws RemoteException
     *             when a communication-related error occurs.
     */
    public Tuv cloneToTarget(Tuv p_sourceLocaleTuv,
            GlobalSightLocale p_targetLocale, String p_gxml, int p_matchType)
            throws TuvException, RemoteException
    {
        return m_tuvManagerLocal.cloneToTarget(p_sourceLocaleTuv,
                p_targetLocale, p_gxml, p_matchType);
    }

    /**
     * Get the Tuvs for the on-line editor for each specified Tu, in the
     * specified locale.
     * 
     * @param p_tuIds
     *            array of Tu Ids.
     * @param p_localeId
     *            locale Id associated with returned Tuv.
     * @param p_jobId
     * @return Collection of Tuv objects.
     * @throws TuvException
     *             when an error occurs.
     * @throws RemoteException
     *             when a communication-related error occurs.
     */
    public Collection getTuvsForOnlineEditor(long[] p_tuIds, long p_localeId,
            long p_jobId) throws TuvException, RemoteException
    {
        return m_tuvManagerLocal.getTuvsForOnlineEditor(p_tuIds, p_localeId,
                p_jobId);
    }

    /**
     * Get the Tuv for the segment editor for the specified Tu, in the specified
     * locale.
     * 
     * @param p_tuId
     *            unique identifier of a Tu object.
     * @param p_localeId
     *            identifier of a GlobalSightLocale associated with returned
     *            Tuv.
     * @return Tuv.
     * @throws TuvException
     *             when an error occurs.
     * @throws RemoteException
     *             when a communication-related error occurs.
     */
    public Tuv getTuvForSegmentEditor(long p_tuId, long p_localeId, long p_jobId)
            throws TuvException, RemoteException
    {
        return m_tuvManagerLocal.getTuvForSegmentEditor(p_tuId, p_localeId,
                p_jobId);
    }

    /**
     * Get the Tuv for the segment editor for the specified Tuv identifier..
     * 
     * @param p_tuvId
     *            unique identifier of a Tuv object.
     * @return Tuv.
     * @throws TuvException
     *             when an error occurs.
     * @throws RemoteException
     *             when a communication-related error occurs.
     */
    public Tuv getTuvForSegmentEditor(long p_tuvId, long p_jobId)
            throws TuvException, RemoteException
    {
        return m_tuvManagerLocal.getTuvForSegmentEditor(p_tuvId, p_jobId);
    }

    public Tu getTuForSegmentEditor(long p_tuId, long p_jobId)
            throws TuvException, RemoteException
    {
        return m_tuvManagerLocal.getTuForSegmentEditor(p_tuId, p_jobId);
    }

    /**
     * Get the TaskTuvs from the end of each of the previous stages of the
     * workflow in reverse chronological order.
     * 
     * @param p_tuvId
     *            unique identifier of a Tuv object.
     * @param p_maxResultNum
     *            maximum number of TaskTuvs to return.
     * @return reverse chronological ordered List of TaskTuvs.
     * @throws TuvException
     *             when an error occurs.
     * @throws RemoteException
     *             when a communication-related error occurs.
     */
    public List getPreviousTaskTuvs(long p_tuvId, int p_maxResultNum)
            throws TuvException, RemoteException
    {
        return m_tuvManagerLocal.getPreviousTaskTuvs(p_tuvId, p_maxResultNum);
    }

    /**
     * Update in the database a changed Tuv.
     * 
     * @param p_tuv
     *            changed Tuv.
     * @throws TuvException
     *             when an error occurs.
     * @throws RemoteException
     *             when a communication-related error occurs.
     */
    public void updateTuvToLocalizedState(Tuv p_tuv, long p_jobId)
            throws TuvException, RemoteException
    {
        m_tuvManagerLocal.updateTuvToLocalizedState(p_tuv, p_jobId);
    }

    /**
     * Get source locale Tuvs for Statistics based on the SourcePage.
     * 
     * @param p_sourcePage
     *            source locale page.
     * @return Collection Tuvs
     * @throws TuvException
     *             when an error occurs.
     * @throws RemoteException
     *             when a communication-related error occurs.
     */
    public Collection getSourceTuvsForStatistics(SourcePage p_sourcePage)
            throws TuvException, RemoteException
    {
        return m_tuvManagerLocal.getSourceTuvsForStatistics(p_sourcePage);
    }

    /**
     * Get larget locale Tuvs for Statistics based on the TargetPage.
     * 
     * @param p_targetPage
     *            target locale page.
     * @return Collection Tuvs
     * @throws TuvException
     *             when an error occurs.
     * @throws RemoteException
     *             when a communication-related error occurs.
     */
    public Collection<Tuv> getTargetTuvsForStatistics(TargetPage p_targetPage)
            throws TuvException, RemoteException
    {
        return m_tuvManagerLocal.getTargetTuvsForStatistics(p_targetPage);
    }

    /**
     * Retrieves the list of TUs belonging to a source page.
     * 
     * @param p_sourcePageId
     *            the id of the source page
     * @return Collection a sorted list of tu ids as Long objects
     */
    public Collection<TuImpl> getTus(Long p_sourcePageId) throws TuvException,
            RemoteException
    {
        return m_tuvManagerLocal.getTus(p_sourcePageId);
    }

    /**
     * Computes and updates the exact match keys for a set of tuvs. This
     * function does neither read objects from nor write objects to the
     * database.
     */
    public void setExactMatchKeys(List p_tuvs) throws TuvException,
            RemoteException
    {
        m_tuvManagerLocal.setExactMatchKeys(p_tuvs);
    }

    /**
     * reset the status of target tuv to NOT_LOCALIZED
     * 
     * @param p_sourcePage
     *            source locale page.
     * @param p_targetLocale
     *            the target locale.
     * @return Map of target locale Tuvs keyed by source locale TuvId.
     * @throws TuvException
     *             when an error occurs.
     * @throws RemoteException
     *             when a communication-related error occurs.
     */
    public void resetTargetTuvStatus(SourcePage p_sourcePage,
            GlobalSightLocale p_targetLocale) throws TuvException,
            RemoteException
    {
        m_tuvManagerLocal.resetTargetTuvStatus(p_sourcePage, p_targetLocale);
    }

    /**
     * Judge if current page is WorldServer xliff file.
     * 
     * @param p_sourcePageId
     * @return
     * @throws TuvException
     * @throws RemoteException
     */
    public boolean isWorldServerXliffSourceFile(Long p_sourcePageId)
            throws TuvException, RemoteException
    {
        return m_tuvManagerLocal.isWorldServerXliffSourceFile(p_sourcePageId);
    }
}
