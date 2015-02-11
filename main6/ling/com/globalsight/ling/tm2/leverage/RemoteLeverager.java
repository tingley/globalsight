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
package com.globalsight.ling.tm2.leverage;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.globalsight.everest.gsedition.GSEdition;
import com.globalsight.everest.gsedition.GSEditionManagerLocal;
import com.globalsight.everest.integration.ling.LingServerProxy;
import com.globalsight.everest.integration.ling.tm2.LeverageMatch;
import com.globalsight.everest.localemgr.LocaleManagerException;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.persistence.tuv.SegmentTuvUtil;
import com.globalsight.everest.projecthandler.ProjectTM;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.tm.Tm;
import com.globalsight.everest.tuv.TuvImpl;
import com.globalsight.ling.tm.LeveragingLocales;
import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.ling.tm2.TmCoreManager;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.webservices.WebServiceException;
import com.globalsight.webservices.client.Ambassador;
import com.globalsight.webservices.client.WebServiceClientHelper;

/**
 * This code is taken from TmCoreManagerLocal and is basically unchanged.
 */
public class RemoteLeverager
{

    private static final Logger c_logger = Logger
            .getLogger(RemoteLeverager.class);

    public void remoteLeveragePage(SourcePage p_sourcePage, List<Tm> p_tms,
            LeverageDataCenter p_leverageDataCenter)
    {
        try
        {
            GlobalSightLocale sourceLocale = p_leverageDataCenter
                    .getSourceLocale();
            LeverageOptions leverageOptions = p_leverageDataCenter
                    .getLeverageOptions();

            // classify original source segments to translatable and localizable
            Collection trSegments = new ArrayList();
            Collection loSegments = new ArrayList();

            Iterator itOriginalSegment = p_leverageDataCenter
                    .getOriginalSeparatedSegments(p_sourcePage.getJobId())
                    .iterator();
            while (itOriginalSegment.hasNext())
            {
                BaseTmTuv originalSegment = (BaseTmTuv) itOriginalSegment
                        .next();
                if (originalSegment.isTranslatable())
                {
                    trSegments.add(originalSegment);
                }
                else
                {
                    loSegments.add(originalSegment);
                }
            }

            // Remote leverage translatable segments
            HashMap remoteLevTranslatableResultMap = null;
            if (trSegments != null && trSegments.size() > 0)
            {
                remoteLevTranslatableResultMap = remoteLeverageSegments(
                        sourceLocale, trSegments, leverageOptions, p_tms, true);
                // Save leverage matches into leverage_match
                if (remoteLevTranslatableResultMap != null
                        && remoteLevTranslatableResultMap.size() > 0)
                {
                    saveRemoteLeverageMatches(p_sourcePage, trSegments,
                            remoteLevTranslatableResultMap, leverageOptions);
                }
            }

            // Remote leverage localizable segments
            if (loSegments != null && loSegments.size() > 0)
            {
                HashMap remoteLevLocalizableResultMap = remoteLeverageSegments(
                        sourceLocale, loSegments, leverageOptions, p_tms, false);
                // Save leverage matches into leverage_match
                if (remoteLevTranslatableResultMap != null
                        && remoteLevTranslatableResultMap.size() > 0)
                {
                    saveRemoteLeverageMatches(p_sourcePage, loSegments,
                            remoteLevLocalizableResultMap, leverageOptions);
                }
            }
        }
        catch (Exception e)
        {
            c_logger.error(e.getMessage(), e);
        }

    }

    /**
     * Remote leverage translatable/localizable segments from remote Segment
     * Tm(exact and fuzzy match)
     * 
     * @param p_sourceLocale
     *            SegmentTmPersistence
     * @param p_trSegments
     *            Collection of original segments (BaseTmTuv)
     * @param p_leverageOptions
     *            Leverage options
     * @param p_tms
     *            Remote Tms to leverage
     * @param p_translatable
     *            translatable or localizable flag
     * @return LeverageMatchResults object
     * @throws RemoteException
     * @throws com.globalsight.webservices.client.WebServiceException
     */
    private HashMap remoteLeverageSegments(GlobalSightLocale p_sourceLocale,
            Collection p_trSegments, LeverageOptions p_leverageOptions,
            List<Tm> p_tms, boolean p_translatable) throws WebServiceException,
            RemoteException
    {
        HashMap results = new HashMap();

        // target locale id :: leverage locale Ids Map
        HashMap trgLocal2LevLocalesMap = new HashMap();
        LeveragingLocales levLocales = p_leverageOptions.getLeveragingLocales();
        Iterator allTrgLocalesIter = levLocales.getAllTargetLocales()
                .iterator();
        while (allTrgLocalesIter.hasNext())
        {
            StringBuffer levLocaleSB = new StringBuffer();
            GlobalSightLocale trgLocale = (GlobalSightLocale) allTrgLocalesIter
                    .next();
            Iterator levLocalesIter = levLocales
                    .getLeveragingLocales(trgLocale).iterator();
            while (levLocalesIter.hasNext())
            {
                long levLocaleId = ((GlobalSightLocale) levLocalesIter.next())
                        .getId();
                if (levLocaleSB.length() == 0)
                {
                    levLocaleSB.append(levLocaleId);
                }
                else
                {
                    levLocaleSB.append(",").append(levLocaleId);
                }
            }
            trgLocal2LevLocalesMap.put(trgLocale.getId(),
                    levLocaleSB.toString());
        }

        // leverage from remote tm one by one
        for (Tm tm : p_tms)
        {
            ProjectTM projectTM = (ProjectTM) tm;
            HashMap originalTuvId2MatchesMap = new HashMap();

            // get client ambassador
            long gsEditionId = projectTM.getGsEditionId();
            GSEditionManagerLocal editionManager = new GSEditionManagerLocal();
            GSEdition gsEdition = editionManager.getGSEditionByID(gsEditionId);
            Ambassador clientAmbassador = null;
            try
            {
                clientAmbassador = WebServiceClientHelper.getClientAmbassador(
                        gsEdition.getHostName(), gsEdition.getHostPort(),
                        gsEdition.getUserName(), gsEdition.getPassword(),
                        gsEdition.getEnableHttps());
            }
            catch (Exception e)
            {
                c_logger.debug(
                        "Failed to get client ambassador for gsedition id: "
                                + gsEditionId, e);
            }

            //
            if (clientAmbassador != null)
            {
                // real access token
                String realAccessToken = null;
                try
                {
                    String fullAccessToken = clientAmbassador.login(
                            gsEdition.getUserName(), gsEdition.getPassword());
                    realAccessToken = WebServiceClientHelper
                            .getRealAccessToken(fullAccessToken);
                }
                catch (Exception e)
                {
                    c_logger.debug(
                            "Web service login() failure,can't get access token",
                            e);
                }

                // remote tm profile id
                long remoteTmProfileId = projectTM.getRemoteTmProfileId();
                Boolean escapeString = new Boolean(true);

                // segments in HashMap
                HashMap segmentMap = new HashMap();
                int size = p_trSegments.size();
                if (p_trSegments != null && p_trSegments.size() > 0)
                {
                    Iterator segmentIter = p_trSegments.iterator();
                    int count = 0;
                    while (segmentIter.hasNext())
                    {
                        BaseTmTuv originalSegment = (BaseTmTuv) segmentIter
                                .next();
                        String segmentStrNoTopTag = originalSegment
                                .getSegmentNoTopTag();
                        long sourceTuvId = originalSegment.getId();
                        segmentMap.put(sourceTuvId, segmentStrNoTopTag);
                        count++;

                        // remote leverage in batch (20)
                        if (count == size || count == 20)
                        {
                            size = size - 20;
                            HashMap tmp = null;
                            tmp = clientAmbassador.searchEntriesInBatch(
                                    realAccessToken,
                                    new Long(remoteTmProfileId), segmentMap,
                                    new Long(p_sourceLocale.getId()),
                                    trgLocal2LevLocalesMap, new Boolean(
                                            p_translatable), escapeString);

                            if (tmp != null)
                            {
                                originalTuvId2MatchesMap.putAll(tmp);
                            }

                            segmentMap.clear();
                            count = 0;
                        }
                    }
                }
            }

            results.put(projectTM.getId(), originalTuvId2MatchesMap);
        }

        return results;
    }

    private void saveRemoteLeverageMatches(SourcePage p_sourcePage,
            Collection p_translatableSegments, HashMap remoteLevResultMap,
            LeverageOptions p_leverageOptions) throws LocaleManagerException,
            RemoteException, GeneralException
    {
        Connection conn = null;
        try
        {
            long jobId = p_sourcePage.getJobId();
            conn = DbUtil.getConnection();
            // For one remote tm
            Iterator iter1 = null;
            if (remoteLevResultMap != null && remoteLevResultMap.size() > 0)
            {
                iter1 = remoteLevResultMap.entrySet().iterator();
            }
            while (iter1 != null && iter1.hasNext())
            {
                Map.Entry entry1 = (Map.Entry) iter1.next();
                HashMap originalTuvId2MatchesMap = (HashMap) entry1.getValue();

                // For one original tuv
                Iterator iter2 = null;
                if (originalTuvId2MatchesMap != null
                        && originalTuvId2MatchesMap.size() > 0)
                {
                    iter2 = originalTuvId2MatchesMap.entrySet().iterator();
                }
                while (iter2 != null && iter2.hasNext())
                {
                    Map.Entry entry2 = (Map.Entry) iter2.next();
                    long originalTuvId = ((Long) entry2.getKey()).longValue();// originalTuvId
                    HashMap localesMatchesMap = (HashMap) entry2.getValue();
                    TuvImpl oriTuv = SegmentTuvUtil.getTuvById(originalTuvId,
                            jobId);

                    // for one target locale
                    Iterator iter3 = null;
                    if (localesMatchesMap != null
                            && localesMatchesMap.size() > 0)
                    {
                        iter3 = localesMatchesMap.entrySet().iterator();
                    }
                    while (iter3 != null && iter3.hasNext())
                    {
                        Map.Entry entry3 = (Map.Entry) iter3.next();
                        long localeId = ((Long) entry3.getKey()).longValue();
                        GlobalSightLocale gsl = ServerProxy.getLocaleManager()
                                .getLocaleById(localeId);
                        Vector matchedVector = (Vector) entry3.getValue();
                        if (matchedVector != null && matchedVector.size() > 0)
                        {
                            Collection<LeverageMatch> c = new ArrayList<LeverageMatch>();
                            Iterator iter4 = matchedVector.iterator();
                            while (iter4.hasNext())
                            {
                                HashMap matchInfoMap = (HashMap) iter4.next();
                                String subId = (String) matchInfoMap
                                        .get("subId");
                                String matchedSegment = (String) matchInfoMap
                                        .get("matchedSegment");
                                String matchType = (String) matchInfoMap
                                        .get("matchType");
                                String tmSourceStr = (String) matchInfoMap
                                        .get("tmSourceStr");
                                int orderNum = ((Integer) matchInfoMap
                                        .get("orderNum")).intValue();
                                float score = ((Float) matchInfoMap
                                        .get("score")).floatValue();

                                LeverageMatch lm = new LeverageMatch();
                                lm.setSourcePageId(p_sourcePage.getIdAsLong());
                                lm.setOriginalSourceTuvId(originalTuvId);
                                lm.setSubId(subId);
                                lm.setMatchedText(matchedSegment);
                                lm.setMatchedOriginalSource(tmSourceStr);
                                lm.setMatchedClob(null);
                                lm.setTargetLocale(gsl);
                                lm.setMatchType(matchType);
                                // Remote TM matches "order num" starts with
                                // 101.
                                lm.setOrderNum((short) (TmCoreManager.LM_ORDER_NUM_START_REMOTE_TM - 1 + orderNum));
                                lm.setScoreNum(score);
                                lm.setMatchedTuvId(-1);// there is no matched
                                                       // tuv id
                                // on local db
                                // Map projectTMIdTmIndexMap =
                                // p_leverageOptions.getTmIndexsToLeverageFrom();
                                // int projectTmIndex = (Integer)
                                // projectTMIdTmIndexMap.get(new
                                // Long(remoteProjectTmIdOnLocal));
                                lm.setProjectTmIndex(Leverager.REMOTE_TM_PRIORITY);
                                // save 0 as its tm id,can't save remote tm id
                                // here
                                // though we can get it.
                                lm.setTmId(0);
                                lm.setTmProfileId(p_leverageOptions
                                        .getTmProfileId());
                                lm.setMtName(null);

                                lm.setSid(oriTuv.getSid());
                                lm.setCreationUser(oriTuv.getCreatedUser());
                                lm.setCreationDate(oriTuv.getCreatedDate());
                                lm.setModifyUser(oriTuv.getLastModifiedUser());
                                lm.setModifyDate(oriTuv.getLastModified());
                                
                                c.add(lm);
                            }
                            // save to leverage_match
                            LingServerProxy.getLeverageMatchLingManager()
                                    .saveLeveragedMatches(c, conn, jobId);
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            c_logger.error("Error when save remote TM leveraging results", e);
        }
        finally
        {
            DbUtil.silentReturnConnection(conn);
        }
    }

}
