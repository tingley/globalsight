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
import com.globalsight.util.GlobalSightLocale;

/**
 * The TuvManager interface is intended to provide management of Tuv objects.
 * Management include create, read, update, and delete. It also includes
 * management of state and versions of the Tuv.
 * <p>
 * Methods specific to the high level process that call them are provided to
 * help target the behavior to the process. For example, a method that obtains
 * Tuvs for export may need less information in Tuv than other processes. This
 * allows for optimization.
 */
public interface TuvManager
{
    // The name bound to the remote object.
    public static final String SERVICE_NAME = "TuvManager";

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
            RemoteException;

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
            throws TuvException, RemoteException;

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
            RemoteException;

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
            RemoteException;

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
    public void saveTuvsFromOffline(List<TuvImplVo> p_tuvs, long jobId)
            throws TuvException, RemoteException;

    /**
     * Clone a source Tuv and associate it with a target locale.
     * 
     * @param p_sourceLocaleTuv
     *            source locale Tuv.
     * @param p_targetLocale
     *            target locale to associate with the cloned Tuv.
     * @return cloned Tuv associated it with a target locale.
     * @throws TuvException
     *             when an error occurs.
     * @throws RemoteException
     *             when a communication-related error occurs.
     */
    public Tuv cloneToTarget(Tuv p_sourceLocaleTuv,
            GlobalSightLocale p_targetLocale) throws TuvException,
            RemoteException;

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
     *            LeverageMatchType.LEVERAGE_GROUP_EXACT_MATCH,
     *            LeverageMatchType.EXACT_MATCH or
     *            LeverageMatchType.UNVERIFIED_EXACT_MATCH.
     * @return cloned Tuv associated it with a target locale.
     * @throws TuvException
     *             when an error occurs.
     * @throws RemoteException
     *             when a communication-related error occurs.
     */
    public Tuv cloneToTarget(Tuv p_sourceLocaleTuv,
            GlobalSightLocale p_targetLocale, String p_gxml, int p_matchType)
            throws TuvException, RemoteException;

    /**
     * Get the Tuvs for the on-line editor for each specified Tu, in the
     * specified locale.
     * 
     * @param p_tus
     *            array of Tu Ids.
     * @param p_locale
     *            locale Id associated with returned Tuv.
     * @return Collection of Tuv objects.
     * @throws TuvException
     *             when an error occurs.
     * @throws RemoteException
     *             when a communication-related error occurs.
     */
    public Collection getTuvsForOnlineEditor(long[] p_tuIds, long p_localeId,
            long p_jobId) throws TuvException, RemoteException;

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
            throws TuvException, RemoteException;

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
    public Tuv getTuvForSegmentEditor(long p_TuvId, long p_jobId)
            throws TuvException, RemoteException;

    public Tu getTuForSegmentEditor(long p_TuId, long p_jobId)
            throws TuvException, RemoteException;

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
            throws TuvException, RemoteException;

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
            throws TuvException, RemoteException;

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
            throws TuvException, RemoteException;

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
            throws TuvException, RemoteException;

    /**
     * Retrieves the list of TUs belonging to a source page.
     * 
     * @param p_sourcePageId
     *            the id of the source page
     * @return Collection a list of Tu objects
     */
    public Collection<TuImpl> getTus(Long p_sourcePageId) throws TuvException,
            RemoteException;

    /**
     * Computes and updates the exact match keys for a set of tuvs. This
     * function does neither read objects from nor write objects to the
     * database.
     */
    public void setExactMatchKeys(List p_tuvs) throws TuvException,
            RemoteException;

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
            RemoteException;

    /**
     * Judge if current page is WorldServer xliff file.
     * 
     * @param p_sourcePageId
     * @return
     * @throws TuvException
     * @throws RemoteException
     */
    public boolean isWorldServerXliffSourceFile(Long p_sourcePageId)
            throws TuvException, RemoteException;
}
