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

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;

import org.hibernate.Query;
import org.hibernate.Session;

import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.persistence.PersistenceService;
import com.globalsight.everest.persistence.tuv.TuDescriptorModifier;
import com.globalsight.everest.persistence.tuv.TuvDescriptorModifier;
import com.globalsight.everest.persistence.tuv.TuvQueryConstants;
import com.globalsight.ling.docproc.extractor.xliff.XliffAlt;
import com.globalsight.ling.tm.LeverageMatchType;
import com.globalsight.ling.tm.TuvLing;
import com.globalsight.ling.util.GlobalSightCrc;
import com.globalsight.log.GlobalSightCategory;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.ArrayConverter;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;

/**
 * The TuvManagerLocal class is an implementation of TuvManager interface.
 */
public final class TuvManagerLocal implements TuvManager
{
    static private final Logger CATEGORY = Logger
            .getLogger(TuvManagerLocal.class);

    /**
     * Constructs a TuvManagerLocal.
     */
    TuvManagerLocal()
    {
    }

    //
    // TuvManager interface methods
    //

    /**
     * Factory method for creating Tu instances. Does not persist the Tu.
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
        TuImpl tu = new TuImpl();

        tu.setTmId(p_tmId);
        tu.setDataType(p_dataType);
        tu.setTuType(p_tuType);
        tu.setLocalizableType(p_localizableType);
        tu.setPid(p_pid);

        if (CATEGORY.isDebugEnabled())
        {
            CATEGORY.debug("createTu" + " p_tmId=" + p_tmId + " p_dataType="
                    + p_dataType + " p_tuType=" + p_tuType.toString()
                    + " p_localizableType=" + p_localizableType + " p_pid="
                    + p_pid + " returned TuImpl=" + tu.toString());
        }

        return tu;
    }

    /*
     * Factory method for creating Tuv instances. Does not persist the Tuv. Sets
     * the TUV state to NOT_LOCALIZED.
     * 
     * @param p_wordCount word count in the segment text. @param
     * p_globalSightLocale GlobalSightLocale of the Tuv. @param p_sourcePage
     * SourcePage associated with the Tuv. @return Tuv @throws TuvException when
     * an error occurs.
     */
    public Tuv createTuv(int p_wordCount, GlobalSightLocale p_locale,
            SourcePage p_sourcePage) throws TuvException, RemoteException
    {
        TuvImpl tuv = new TuvImpl();

        tuv.setWordCount(p_wordCount);
        tuv.setGlobalSightLocale(p_locale);
        tuv.setSourcePage(p_sourcePage);

        if (CATEGORY.isDebugEnabled())
        {
            CATEGORY.debug("createTuv" + " p_wordCount=" + p_wordCount
                    + " p_globalSightLocale=" + p_locale.toString()
                    + " p_sourcePage= " + p_sourcePage.getId()
                    + " returned tuvImpl=" + tuv.toString());
        }

        return tuv;
    }

    /**
     * Saves the target segments from an offline edit.
     * 
     * @param p_tuvs
     *            List of Tuvs to be saved
     * @throws TuvException
     *             when an error occurs.
     */
    public void saveTuvsFromOffline(List p_tuvs) throws TuvException
    {
        TuvImpl tuvInDb = null;
        try
        {
            for (Iterator it = p_tuvs.iterator(); it.hasNext();)
            {
                Tuv tuv = (Tuv) it.next();
                tuvInDb = (TuvImpl) HibernateUtil.get(TuvImpl.class,
                        tuv.getId());
                tuvInDb.setExactMatchKey(GlobalSightCrc
                        .calculate(((TuvLing) tuv).getExactMatchFormat()));
                tuvInDb.setSegmentString(tuv.getGxml());
                tuvInDb.setState(TuvState.LOCALIZED);
                tuvInDb.setMergeState(tuv.getMergeState());
                tuvInDb.setLastModifiedUser(tuv.getLastModifiedUser());
                tuvInDb.setLastModified(new Date());
                tuvInDb.setTimestamp(new Timestamp(System.currentTimeMillis()));
                
                HibernateUtil.saveOrUpdate(tuvInDb);
            }
        }
        catch (Exception ex)
        {
            CATEGORY.error("Cannot save offline TUV", ex);
            CATEGORY.error("id: " + tuvInDb.getId());
            CATEGORY.error("segment: " + tuvInDb.getSegmentString());
            Set<XliffAlt> alts = tuvInDb.getXliffAlt();
            CATEGORY.error("xliffAlt size: " + alts.size());
            int i = 0;
            for (XliffAlt alt : alts)
            {
                i++;
                CATEGORY.error("  XliffAlt " + i);
                CATEGORY.error("    id :" + alt.getId());
                CATEGORY.error("    segment :" + alt.getSegment());
                CATEGORY.error("    language :" + alt.getLanguage());
                CATEGORY.error("    quality :" + alt.getQuality());
                CATEGORY.error("    tuv id :" + alt.getTuv().getId());
            }

            throw new TuvException("Cannot save offline TUVs", ex);
        }
    }

    /**
     * Clones the source page's tuv for a target page based on the specified
     * locale.
     * 
     * @param p_sourceLocaleTuv -
     *            The tuv to be cloned.
     * @param p_targetLocale -
     *            The locale of the tuv.
     * @throws TuvException
     *             when an error occurs.
     * @throws RemoteException
     *             when a communication-related error occurs.
     */
    public Tuv cloneToTarget(Tuv p_sourceTuv, GlobalSightLocale p_targetLocale)
            throws TuvException, RemoteException
    {
        TuvImpl tuv = new TuvImpl((TuvImpl) p_sourceTuv);

        tuv.setGlobalSightLocale(p_targetLocale);
        tuv.setExactMatchKey(0);

        if (CATEGORY.isDebugEnabled())
        {
            CATEGORY.debug("cloneToTarget" + " old tuv id "
                    + p_sourceTuv.getId() + " target locale="
                    + p_targetLocale.toString() + " new tuv id " + tuv.getId());
        }

        return tuv;
    }

    /**
     * Clones the source page's tuv for a target page based on the specified
     * locale.
     * 
     * @param p_sourceLocaleTuv -
     *            The tuv to be cloned.
     * @param p_targetLocale -
     *            The locale of the tuv.
     * @param p_matchType
     *            match type that the Tuv p_gxml came from. Must be
     *            LeverageMatchType.LEVERAGE_GROUP_EXACT_MATCH,
     *            LeverageMatchType.EXACT_MATCH or
     *            LeverageMatchType.UNVERIFIED_EXACT_MATCH.
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
        TuvImpl tuv = (TuvImpl) cloneToTarget(p_sourceLocaleTuv, p_targetLocale);

        switch (p_matchType)
        {
        case LeverageMatchType.LEVERAGE_GROUP_EXACT_MATCH:
            tuv.setState(TuvState.LEVERAGE_GROUP_EXACT_MATCH_LOCALIZED);
            break;
        case LeverageMatchType.EXACT_MATCH:
            tuv.setState(TuvState.EXACT_MATCH_LOCALIZED);
            break;
        case LeverageMatchType.UNVERIFIED_EXACT_MATCH:
            tuv.setState(TuvState.UNVERIFIED_EXACT_MATCH);
            break;
        default:
            TuvException ex = new TuvException("cloneToTarget: "
                    + "p_matchType " + Integer.toString(p_matchType)
                    + " not valid for this method");
            CATEGORY.error(ex);
            throw ex;
        }

        tuv.setGxmlWithSubIds(p_gxml);

        return tuv;
    }

    /**
     * Gets Tuvs in the specified locale for the export process.
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
            GlobalSightLocale p_locale) throws TuvException, RemoteException
    {
        // Vector queryArgs = new Vector(2);
        // queryArgs.add(p_sourcePage.getIdAsLong());
        // queryArgs.add(p_locale.getIdAsLong());

        /*
         * Collection result = executeQuery(
         * TuvQueryNames.EXPORT_TUVS_BY_SOURCE_PAGE_ID, queryArgs,
         * TopLinkPersistence.MAKE_EDITABLE);
         */

        // Hibernate access
        HashMap map = new HashMap();
        map.put(TuvQueryConstants.LOCALE_ID_ARG, p_locale.getIdAsLong());
        map.put(TuvQueryConstants.SOURCE_PAGE_ID_ARG, p_sourcePage
                .getIdAsLong());
        List result = HibernateUtil.searchWithSql(
                TuvDescriptorModifier.TARGET_TUV_BY_SOURCE_PAGE_SQL, map,
                TuvDescriptorModifier.TUV_CLASS);
        if (CATEGORY.isDebugEnabled())
        {
            Collection ids = TuvPersistenceHelper.getIds(result);

            CATEGORY.debug("getExportTuvs" + " p_sourcePage="
                    + p_sourcePage.getId() + " p_locale=" + p_locale.toString()
                    + " returned " + ids.size() + " Tuvs=" + ids.toString());
        }

        return result;
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
            Collection p_trgLocales) throws TuvException, RemoteException
    {
        List tus = null;
        Connection conn = null;

        try
        {
            conn = PersistenceService.getInstance().getConnection();
            TuvJdbcQuery tuvQuery = new TuvJdbcQuery(conn);
            tus = tuvQuery.getTusBySourcePageIdAndLocales(p_sourcePage,
                    p_trgLocales);
        }
        catch (Exception ex)
        {
            CATEGORY.error(ex);

            throw new TuvException(ex);
        }
        finally
        {
            if (conn != null)
            {
                try
                {
                    PersistenceService.getInstance().returnConnection(conn);
                }
                catch (Exception ignore)
                {
                }
            }
        }

        return new PageSegments(tus, p_sourcePage, p_trgLocales);
    }

    /**
     * Resets the status of the target tuv to NOT_LOCALIZED.
     * 
     * @param p_sourcePage
     *            source locale page.
     * @param p_sourceGlobalSightLocale
     *            the source locale.
     * @param p_sourceGlobalSightLocale
     *            the target locale.
     * @return Map of target locale Tuvs keyed by source locale TuvId.
     * @throws TuvException
     *             when an error occurs.
     * @throws RemoteException
     *             when a communication-related error occurs.
     */
    public void resetTargetTuvStatus(SourcePage p_sourcePage,
            GlobalSightLocale p_sourceLocale, GlobalSightLocale p_targetLocale)
            throws TuvException, RemoteException
    {
        try
        {
            // get the target tuvs
            // Vector queryArgs1 = new Vector(2);
            // queryArgs1.add(p_sourcePage.getIdAsLong());
            // queryArgs1.add(p_targetLocale.getIdAsLong());
            /*
             * Collection targetTuvs = executeQuery(
             * TuvQueryNames.EXPORT_TUVS_BY_SOURCE_PAGE_ID, queryArgs1,
             * TopLinkPersistence.MAKE_EDITABLE);
             */
            // Hibernate access
            HashMap map = new HashMap();
            map.put(TuvQueryConstants.LOCALE_ID_ARG, p_targetLocale
                    .getIdAsLong());
            map.put(TuvQueryConstants.SOURCE_PAGE_ID_ARG, p_sourcePage
                    .getIdAsLong());
            List targetTuvs = HibernateUtil.searchWithSql(
                    TuvDescriptorModifier.TARGET_TUV_BY_SOURCE_PAGE_SQL, map,
                    TuvDescriptorModifier.TUV_CLASS);

            Object[] tuvs = targetTuvs.toArray();
            int size = tuvs.length;
            for (int i = 0; i < size; i++)
            {
                TuvImpl tuv = (TuvImpl) tuvs[i];
                tuv.setState(TuvState.NOT_LOCALIZED);
                tuv.setLastModified(new Date());
                HibernateUtil.update(tuv);
                // getPersistence().updateObject(tuv);
            }
        }
        catch (Exception ex)
        {
            CATEGORY.error(ex);
        }
    }

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
    public Collection getTuvsForOnlineEditor(long[] p_tuIds, long p_localeId)
            throws TuvException, RemoteException
    {
        /*
         * List tus = (List) getObjectsByPKs(TuvPersistenceHelper
         * .makeInArgumentListFromIds(ArrayConverter.asList(p_tuIds)),
         * TuImpl.class, TopLinkPersistence.MAKE_NOT_EDITABLE);
         */
        List tus = (List) getObjectsByPKs(new Vector(ArrayConverter
                .asList(p_tuIds)), TuImpl.class);
        ArrayList tuvs = new ArrayList(tus.size());

        for (int i = 0; i < tus.size(); i++)
        {
            Object object = ((Tu) tus.get(i)).getTuv(p_localeId);

            if (object != null)
            {
                tuvs.add(object);
            }
        }

        if (CATEGORY.isDebugEnabled())
        {
            Collection ids = new ArrayList(0);

            if (!tuvs.isEmpty())
            {
                ids = TuvPersistenceHelper.getIds(tuvs);
            }

            CATEGORY.debug("getTuvsForOnlineEditor" + " p_tuIds="
                    + ArrayConverter.asList(p_tuIds).toString() + " returned "
                    + ids.size() + " Tuvs=" + ids.toString());
        }

        return tuvs;
    }

    /**
     * Gets the Tuv for the segment editor for the specified Tu, in the
     * specified locale.
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
    public Tuv getTuvForSegmentEditor(long p_tuId, long p_localeId)
            throws TuvException, RemoteException
    {
        // return ((Tu)getObjectByPK(p_tuId, TuImpl.class,
        // TopLinkPersistence.MAKE_EDITABLE)).getTuv(p_localeId);

        // Vector args = new Vector(2);
        // args.add(new Long(p_tuId));
        // args.add(new Long(p_localeId));

        /*
         * return (Tuv) executeQuery(TuvQueryNames.TUV_BY_TU_LOCALE, args,
         * TopLinkPersistence.MAKE_EDITABLE).iterator().next();
         * 
         */

        String sql = TuvDescriptorModifier.TUV_BY_TU_LOCALE_SQL;
        Map map = new HashMap();
        map.put(TuvQueryConstants.TUV_ID_ARG, new Long(p_tuId));
        map.put(TuvQueryConstants.TARGET_LOCALE_ID_ARG, new Long(p_localeId));

        Tuv tuv = null;
        List result = HibernateUtil.searchWithSql(sql, map, TuvImpl.class);
        if (result != null && result.size() > 0)
        {
            tuv = (Tuv) result.get(0);
        }
        return tuv;
    }

    /*
     * Gets the Tuv for the segment editor for the specified Tuv identifier.
     * 
     * @param p_tuvId unique identifier of a Tuv object. @return Tuv. @throws
     * TuvException when an error occurs. @throws RemoteException when a
     * communication-related error occurs.
     */
    public Tuv getTuvForSegmentEditor(long p_tuvId) throws TuvException,
            RemoteException
    {
        return (Tuv) getObjectByPK(p_tuvId, TuvImpl.class);
        /*
         * return (Tuv) getObjectByPK(p_tuvId, TuvImpl.class,
         * TopLinkPersistence.MAKE_EDITABLE);
         */
    }
    
    public Tu getTuForSegmentEditor(long p_tuId) throws TuvException, 
                                                         RemoteException
    {
        return (Tu) getObjectByPK(p_tuId, TuImpl.class);
    }

    /**
     * Gets the TaskTuvs from the end of each of the previous stages of the
     * workflow in reverse chronological order. The parameter p_maxResultNum is
     * ignored.
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
        // Vector queryArgs = new Vector(1);
        // queryArgs.add(new Long(p_tuvId));
        // queryArgs.add(new Integer(p_maxResultNum));
        /*
         * List taskTuvs = (List) executeQuery(
         * TaskTuvQueryNames.PREVIOUS_TASK_TUVS, queryArgs,
         * TopLinkPersistence.MAKE_NOT_EDITABLE);
         */
        String hql = " from TaskTuv t where t.currentTuv= :tuvId order by t.version desc";
        Session session = HibernateUtil.getSession();
        Query query = session.createQuery(hql);
        query.setMaxResults(p_maxResultNum);
        query.setLong("tuvId", p_tuvId);
        List taskTuvs = query.list();
        // session.close();

        if (CATEGORY.isDebugEnabled())
        {
            CATEGORY.debug("getPreviousTaskTuvs" + " p_tuvId=" + p_tuvId
                    + " returned " + taskTuvs.size() + " TaskTuvs="
                    + taskTuvs.toString());
        }

        return taskTuvs;
    }

    /**
     * Updates a changed Tuv in the database.
     * 
     * @param p_tuv
     *            changed Tuv.
     * @throws TuvException
     *             when an error occurs.
     * @throws RemoteException
     *             when a communication-related error occurs.
     */
    public void updateTuv(Tuv p_tuv) throws TuvException, RemoteException
    {
        try
        {
            TuvImpl tuv = (TuvImpl) p_tuv;

            tuv.setExactMatchKey(GlobalSightCrc.calculate(tuv
                    .getExactMatchFormat()));
            tuv.setState(TuvState.LOCALIZED);
            tuv.setLastModified(new Date());
            HibernateUtil.update(tuv);
            // getPersistence().updateObject(tuv);
        }
        catch (Exception ex)
        {
            throw new TuvException("updateTuv " + p_tuv.toString(), ex);
        }
    }

    /**
     * Gets source locale Tuvs for Statistics based on the SourcePage.
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
        // Vector queryArgs = new Vector(2);
        // queryArgs.add(p_sourcePage.getIdAsLong());
        // queryArgs.add(p_sourcePage.getGlobalSightLocale().getIdAsLong());

        // Collection result = executeQuery(
        // TuvQueryNames.SOURCE_TUVS_FOR_STATISTICS_BY_SOURCE_PAGE_ID,
        // queryArgs, TopLinkPersistence.MAKE_NOT_EDITABLE);

        HashMap map = new HashMap();
        map.put(TuvQueryConstants.LOCALE_ID_ARG, p_sourcePage
                .getGlobalSightLocale().getIdAsLong());
        map.put(TuvQueryConstants.SOURCE_PAGE_ID_ARG, p_sourcePage
                .getIdAsLong());
        List result = HibernateUtil.searchWithSql(
                TuvDescriptorModifier.TUV_BY_SOURCE_PAGE_SQL, map,
                TuvDescriptorModifier.TUV_CLASS);
        if (CATEGORY.isDebugEnabled())
        {
            Collection ids = TuvPersistenceHelper.getIds(result);

            CATEGORY.debug("getSourceTuvsForStatistics" + " p_sourcePage="
                    + p_sourcePage.getId() + " returned " + ids.size()
                    + " Tuvs=" + ids.toString());
        }

        return result;
    }

    /**
     * Gets target locale tuvs for statistics based on the TargetPage.
     * 
     * @param p_targetPage
     *            target locale page.
     * @return Collection Tuvs
     * @throws TuvException
     *             when an error occurs.
     * @throws RemoteException
     *             when a communication-related error occurs.
     */
    public Collection getTargetTuvsForStatistics(TargetPage p_targetPage)
            throws TuvException, RemoteException
    {
        // Vector queryArgs = new Vector(2);
        // queryArgs.add(p_targetPage.getIdAsLong());
        // queryArgs.add(p_targetPage.getGlobalSightLocale().getIdAsLong());

        // Collection result = executeQuery(
        // TuvQueryNames.TARGET_TUVS_FOR_STATISTICS_BY_TARGET_PAGE_ID,
        // queryArgs, TopLinkPersistence.MAKE_EDITABLE);
        HashMap map = new HashMap();
        map.put(TuvQueryConstants.TARGET_PAGE_ID_ARG, p_targetPage
                .getIdAsLong());
        map.put(TuvQueryConstants.LOCALE_ID_ARG, p_targetPage
                .getGlobalSightLocale().getIdAsLong());
        List result = HibernateUtil.searchWithSql(
                TuvDescriptorModifier.TUV_BY_TARGET_PAGE_SQL, map,
                TuvDescriptorModifier.TUV_CLASS);

        if (CATEGORY.isDebugEnabled())
        {
            Collection ids = TuvPersistenceHelper.getIds(result);

            CATEGORY.debug("getTargetTuvsForStatistics" + " p_targetPage="
                    + p_targetPage.getId() + " returned " + ids.size()
                    + " Tuvs=" + ids.toString());
        }

        return result;
    }

    /**
     * Retrieves the list of TUs belonging to a source page.
     * 
     * @param p_sourcePageId
     *            the id of the source page
     * @return Collection a sorted list of tus from the source page
     */
    public Collection getTus(Long p_sourcePageId) throws TuvException,
            RemoteException
    {
        // Vector queryArgs = new Vector(1);
        // queryArgs.add(p_sourcePageId);

        // Collection result = executeQuery(TuQueryNames.TUS_BY_SOURCE_PAGE_ID,
        // queryArgs, TopLinkPersistence.MAKE_NOT_EDITABLE);

        String sql = TuDescriptorModifier.TU_BY_SOURCE_PAGE_ID_SQL;
        Map map = new HashMap();
        map.put(TuvQueryConstants.SOURCE_PAGE_ID_ARG, p_sourcePageId);

        return HibernateUtil.searchWithSql(sql, map, TuImpl.class);
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
        boolean result = false;

        Collection tus = getTus(p_sourcePageId);
        if (tus != null && tus.size() > 0)
        {
            Iterator it = tus.iterator();
            while (it.hasNext())
            {
                Tu tu = (Tu) it.next();
                if (tu.getGenerateFrom() != null
                        && tu.getGenerateFrom().equals(TuImpl.FROM_WORLDSERVER))
                {
                    result = true;
                    break;
                }
            }
        }

        return result;
    }

    /**
     * Computes and updates the exact match keys for a set of tuvs. This
     * function does neither read objects from nor write objects to the
     * database.
     */
    public void setExactMatchKeys(List p_tuvs)
    {
        for (int i = 0, max = p_tuvs.size(); i < max; i++)
        {
            TuvImpl tuv = (TuvImpl) p_tuvs.get(i);
            String exactMatchFormat = tuv.getExactMatchFormat();

            // generate exact match key
            long crc = GlobalSightCrc.calculate(exactMatchFormat);
            tuv.setExactMatchKey(crc);
        }
    }

    //
    // package methods
    //

    Collection getTuvsByPK(Collection p_tuvIds, boolean p_makeEditable)
            throws TuvException
    {
        Vector tuvIds = new Vector(p_tuvIds);

        try
        {
            // TopLinkPersistence persistence =
            // PersistenceService.getInstance();
            // return TuvPersistenceHelper.executeObjectsByPKsQuery(tuvIds,
            // TuvImpl.class);
            return getObjectsByPKs(tuvIds, TuvImpl.class);
        }
        catch (Throwable t)
        {
            CATEGORY.error("p_tuvIds " + p_tuvIds.toString(), t);
            throw new TuvException(GeneralException.getStackTraceString(t));
        }
    }

    List getObjectsByPKs(Vector p_Ids, Class p_class) throws TuvException
    {
        try
        {
            List objList = new ArrayList();
            for (int i = 0; i < p_Ids.size(); i++)
            {
                long p_id = ((Integer) p_Ids.get(i)).longValue();
                Object obj = getObjectByPK(p_id, p_class);
                if (obj != null)
                {
                    objList.add(obj);
                }
            }
            return objList;
            /*
             * return (List)
             * TuvPersistenceHelper.executeObjectsByPKsQuery(p_Ids, p_class,
             * p_makeEditable);
             */
        }
        catch (Throwable t)
        {
            CATEGORY.error("p_class=" + p_class.toString() + " p_Ids "
                    + p_Ids.toString(), t);
            throw new TuvException(GeneralException.getStackTraceString(t));
        }
    }

    Object getObjectByPK(long p_Id, Class p_class) throws TuvException
    {
        try
        {
            return HibernateUtil.get(p_class, p_Id);
            /*
             * return TuvPersistenceHelper.executeObjectByPKQuery(p_Id, p_class,
             * p_makeEditable);
             */
        }
        catch (Throwable t)
        {
            CATEGORY.error("p_class=" + p_class.toString() + " p_Id "
                    + Long.toString(p_Id) + GlobalSightCategory.getLineContinuation()
            // + TuvPersistenceHelper.dumpDescriptor(SESSION, p_class)
                    // .toString()
                    , t);
            throw new TuvException(GeneralException.getStackTraceString(t));
        }
    }

    // private TopLinkPersistence getPersistence() throws PersistenceException
    // {
    // if (m_tlp == null)
    // {
    // m_tlp = PersistenceService.getInstance();
    // }
    //
    // return m_tlp;
    // }

    // private void syncTopLinkCache(Collection p_tuvs) throws Exception
    // {
    // Session session = getPersistence().acquireClientSession();
    //
    // try
    // {
    // for (Iterator it = p_tuvs.iterator(); it.hasNext();)
    // {
    // Tuv tuvNoTopLink = (Tuv) it.next();
    // TuvImpl tuvTopLink = new TuvImpl();
    // tuvTopLink.setId(tuvNoTopLink.getId());
    //
    // session.refreshObject(tuvTopLink);
    // }
    // }
    // catch (Exception e)
    // {
    // throw new Exception(e);
    // }
    // finally
    // {
    // if (session != null)
    // {
    // session.release();
    // }
    // }
    // }

}
