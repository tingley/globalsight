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
package com.globalsight.everest.page.pageimport;

import java.io.File;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.globalsight.cxe.adapter.idml.IdmlHelper;
import com.globalsight.cxe.adapter.passolo.PassoloUtil;
import com.globalsight.everest.edit.offline.xliff.XLIFFStandardUtil;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.integration.ling.LingServerProxy;
import com.globalsight.everest.integration.ling.tm2.LeverageMatch;
import com.globalsight.everest.page.ExtractedSourceFile;
import com.globalsight.everest.page.PageException;
import com.globalsight.everest.page.PageState;
import com.globalsight.everest.page.PageWordCounts;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.page.UnextractedFile;
import com.globalsight.everest.projecthandler.MachineTranslationProfile;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.request.Request;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.tuv.IXliffProcessor;
import com.globalsight.everest.tuv.PoProcessor;
import com.globalsight.everest.tuv.Tu;
import com.globalsight.everest.tuv.TuImpl;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.tuv.TuvException;
import com.globalsight.everest.tuv.TuvImpl;
import com.globalsight.everest.tuv.TuvManager;
import com.globalsight.everest.tuv.TuvState;
import com.globalsight.everest.tuv.XliffProcessor;
import com.globalsight.everest.webapp.pagehandler.administration.mtprofile.MTProfileHandlerHelper;
import com.globalsight.everest.webapp.pagehandler.edit.online.OnlineTagHelper;
import com.globalsight.ling.common.Text;
import com.globalsight.ling.common.XmlEntities;
import com.globalsight.ling.docproc.IFormatNames;
import com.globalsight.ling.docproc.extractor.xliff.XliffAlt;
import com.globalsight.ling.tm.ExactMatchedSegments;
import com.globalsight.ling.tm.LeverageMatchLingManager;
import com.globalsight.ling.tm.LeverageMatchType;
import com.globalsight.ling.tm.LeverageSegment;
import com.globalsight.ling.tm2.TmCoreManager;
import com.globalsight.ling.tm2.leverage.LeverageOptions;
import com.globalsight.ling.tm2.leverage.Leverager;
import com.globalsight.ling.tm2.leverage.MatchState;
import com.globalsight.ling.tw.internal.InternalTextUtil;
import com.globalsight.machineTranslation.MTHelper;
import com.globalsight.machineTranslation.MTHelper2;
import com.globalsight.machineTranslation.MachineTranslator;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.terminology.termleverager.TermLeverageResult;
import com.globalsight.terminology.termleverager.TermLeverageResult.MatchRecordList;
import com.globalsight.terminology.termleverager.replacer.TermReplacer;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.NumberUtil;
import com.globalsight.util.StringUtil;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.edit.GxmlUtil;
import com.globalsight.util.edit.SegmentUtil2;

/**
 * Used to persist extracted or un-extracted files.
 * 
 * @author YorkJin
 * @since 2011-09-16
 */
public abstract class AbstractTargetPagePersistence implements
        TargetPagePersistence
{
    private static Logger s_logger = Logger
            .getLogger(AbstractTargetPagePersistence.class);

    private MachineTranslator machineTranslator = null;
    // private boolean autoCommitToTm = false;
    private IXliffProcessor processor;

    public Collection<TargetPage> persistObjectsWithUnextractedFile(
            SourcePage p_sourcePage, Collection p_targetLocales)
            throws PageException
    {
        ArrayList<TargetPage> targetPages = new ArrayList<TargetPage>();

        try
        {
            for (Iterator it = p_targetLocales.iterator(); it.hasNext();)
            {
                GlobalSightLocale targetLocale = (GlobalSightLocale) it.next();
                TargetPage targetPage = new TargetPage(targetLocale,
                        p_sourcePage);
                targetPage.setTimestamp(new Timestamp(System
                        .currentTimeMillis()));

                // specify where to put the UnextractedFile
                StringBuffer fileName = new StringBuffer(
                        Long.toString(p_sourcePage.getRequest().getId()));

                fileName.append(File.separator);
                fileName.append("PTF");
                fileName.append(File.separator);
                fileName.append(targetLocale.toString());
                fileName.append(File.separator);
                fileName.append(targetPage.getExternalPageId());

                UnextractedFile uf = (UnextractedFile) targetPage
                        .getPrimaryFile();
                uf.setStoragePath(fileName.toString());
                uf.setLastModifiedDate(new Date(System.currentTimeMillis()));

                targetPages.add(targetPage);
            }

            HibernateUtil.save(targetPages);

            if (s_logger.isDebugEnabled())
            {
                s_logger.debug("Persisting target pages for a source page with an unextracted file.");
            }
        }
        catch (Exception e)
        {
            s_logger.error("The target page could not be persisted", e);
            throw new PageException(e);
        }

        return targetPages;
    }

    /**
     * Persists target pages that failed import. So only the page(s) itself
     * needs to be persisted with a state of IMPORT_FAIL and the exception
     * message to explain the error. None of its segments (the TUs, TUVs,
     * etc...) are persisted.
     */
    public Collection<TargetPage> persistFailedObjectsWithExtractedFile(
            SourcePage p_sourcePage, Hashtable p_targetLocaleErrors)
            throws PageException
    {
        ArrayList<TargetPage> targetPages = new ArrayList<TargetPage>();

        try
        {
            Set targetLocales = p_targetLocaleErrors.keySet();
            for (Iterator itl = targetLocales.iterator(); itl.hasNext();)
            {
                GlobalSightLocale targetLocale = (GlobalSightLocale) itl.next();
                TargetPage targetPage = new TargetPage(targetLocale,
                        p_sourcePage);

                targetPage.setPageState(PageState.IMPORT_FAIL);
                GeneralException ge = (GeneralException) p_targetLocaleErrors
                        .get(targetLocale);
                targetPage.setError(ge);
                targetPage.setTimestamp(new Timestamp(System
                        .currentTimeMillis()));
                if (targetPage.getPrimaryFileType() == Request.UNEXTRACTED_LOCALIZATION_REQUEST)
                {
                    targetPage.getUnextractedFile().setLastModifiedDate(
                            new Date(System.currentTimeMillis()));
                }

                targetPages.add(targetPage);
            }
            HibernateUtil.save(targetPages);

            if (s_logger.isDebugEnabled())
            {
                s_logger.debug("Persisting failed target pages for "
                        + "a source page with an extracted file.");
            }

        }
        catch (Exception e)
        {
            s_logger.error("The failed target page could not be persisted", e);
            throw new PageException(e);
        }

        return targetPages;
    }

    /**
     * @see TargetPagePersistence.getTargetTuvs(SourcePage p_sourcePage,
     *      HashMap<Tu, Tuv> p_sourceTuvMap, GlobalSightLocale p_targetLocale,
     *      TermLeverageResult p_termMatches, boolean p_useLeveragedTerms,
     *      ExactMatchedSegments p_exactMatchedSegments)
     */

    @SuppressWarnings("unchecked")
    public ArrayList<Tuv> getTargetTuvs(SourcePage p_sourcePage,
            HashMap<Tu, Tuv> p_sourceTuvMap, GlobalSightLocale p_targetLocale,
            TermLeverageResult p_termMatches, boolean p_useLeveragedTerms,
            ExactMatchedSegments p_exactMatchedSegments) throws PageException
    {
        long jobId = p_sourcePage.getJobId();

        Set<Tu> unAppliedTus = new HashSet<Tu>();
        unAppliedTus.addAll(p_sourceTuvMap.keySet());
        ArrayList<Tuv> result = new ArrayList<Tuv>(unAppliedTus.size());
        // Store results that have been applied matches
        HashMap<Tu, Tuv> appliedTuTuvMap = new HashMap<Tu, Tuv>();
        GlobalSightLocale sourceLocale = p_sourcePage.getGlobalSightLocale();
        ExtractedSourceFile esf = (ExtractedSourceFile) p_sourcePage
                .getExtractedFile();
        String dataType = esf.getDataType();
        MachineTranslationProfile mtProfile = MTProfileHandlerHelper
                .getMtProfileBySourcePage(p_sourcePage, p_targetLocale);

        processor = xliffProFactory(p_sourcePage);

        if (s_logger.isDebugEnabled())
        {
            s_logger.debug("Starting useLeveragedSegments() at " + new Date());
        }

        try
        {
            /****** Priority 1 : Handle XLF/PO/ matches ******/
            if (IFormatNames.FORMAT_XLIFF.equalsIgnoreCase(dataType)
                    || IFormatNames.FORMAT_XLIFF20.equalsIgnoreCase(dataType)
                    || IFormatNames.FORMAT_PO.equalsIgnoreCase(dataType)
                    || IFormatNames.FORMAT_PASSOLO.equalsIgnoreCase(dataType))
            {
                appliedTuTuvMap = applyXlfOrPoMatches(p_sourcePage,
                        p_sourceTuvMap, sourceLocale, p_targetLocale,
                        unAppliedTus, appliedTuTuvMap);
            }

            /****** For entire internal text segment(GBS-3279) ******/
            unAppliedTus.removeAll(appliedTuTuvMap.keySet());
            for (Iterator<Tu> it = unAppliedTus.iterator(); it.hasNext();)
            {
                TuImpl tu = (TuImpl) it.next();
                Tuv sourceTuv = (Tuv) p_sourceTuvMap.get(tu);
                if (GxmlUtil.isEntireInternalText(sourceTuv.getGxmlElement()))
                {
                    Tuv targetTuv = getTuvManager().cloneToTarget(sourceTuv,
                            p_targetLocale);
                    targetTuv.setState(TuvState.DO_NOT_TRANSLATE);
                    tu.addTuv(targetTuv);
                    appliedTuTuvMap.put(tu, targetTuv);
                }
            }

            /****** Priority 2 : Handle local TM matches ******/
            if (mtProfile == null || !mtProfile.isIgnoreTMMatch())
            {
                unAppliedTus.removeAll(appliedTuTuvMap.keySet());
                appliedTuTuvMap = applyLocalTmMatches(p_sourcePage, p_sourceTuvMap, sourceLocale,
                        p_targetLocale, p_termMatches, p_useLeveragedTerms, p_exactMatchedSegments,
                        unAppliedTus, appliedTuTuvMap);
            }

            /****** Priority 3 : Handle MT hitting ******/
            boolean useMtOnJobCreation = p_sourcePage.getRequest().getL10nProfile()
                    .getUseMtOnJobCreation();
            if (mtProfile != null && mtProfile.isActive() && useMtOnJobCreation)
            {
                String mtEngine = mtProfile.getMtEngine();
                machineTranslator = MTHelper.initMachineTranslator(mtEngine);
                HashMap paramMap = mtProfile.getParamHM();
                paramMap.put(MachineTranslator.SOURCE_PAGE_ID,
                        p_sourcePage.getId());
                paramMap.put(MachineTranslator.TARGET_LOCALE_ID,
                        p_targetLocale.getIdAsLong());
                paramMap.put(MachineTranslator.MT_PROFILE, mtProfile);
                boolean isXlf = MTHelper2.isXlf(p_sourcePage.getId());
                paramMap.put(
                        MachineTranslator.NEED_SPECAIL_PROCESSING_XLF_SEGS,
                        isXlf ? "true" : "false");
                paramMap.put(MachineTranslator.DATA_TYPE,
                        MTHelper2.getDataType(p_sourcePage.getId()));
                if (MachineTranslator.ENGINE_MSTRANSLATOR
                        .equalsIgnoreCase(machineTranslator.getEngineName())
                        && p_targetLocale.getLanguage().equalsIgnoreCase("sr"))
                {
                    String srLang = mtProfile
                            .getPreferedLangForSr(p_targetLocale.toString());
                    paramMap.put(MachineTranslator.SR_LANGUAGE, srLang);
                }
                machineTranslator.setMtParameterMap(paramMap);
                boolean isLocalePairSupportedByMT = isLocalePairSupportedByMT(
                        sourceLocale, p_targetLocale);
                if (isLocalePairSupportedByMT)
                {
                    unAppliedTus.removeAll(appliedTuTuvMap.keySet());
                    appliedTuTuvMap = applyMTMatches(p_sourcePage,
                            p_sourceTuvMap, sourceLocale, p_targetLocale,
                            unAppliedTus, appliedTuTuvMap);
                }
            }
            else
            {
                machineTranslator = null;
            }

            /****** Priority 4 : remove all internal text segments match ******/
            Set<Tu> internalTextTus = new HashSet<Tu>();
            if (appliedTuTuvMap != null && appliedTuTuvMap.size() > 0)
            {
                for (Iterator it = appliedTuTuvMap.keySet().iterator(); it
                        .hasNext();)
                {
                    TuImpl tu = (TuImpl) it.next();
                    Tuv tuv = (Tuv) p_sourceTuvMap.get(tu);
                    if (InternalTextUtil.isInternalText(tuv
                            .getGxmlExcludeTopTags())
                            && !GxmlUtil.isEntireInternalText(tuv
                                    .getGxmlElement()))
                    {
                        internalTextTus.add(tu);
                    }
                }
            }
            if (internalTextTus.size() > 0)
            {
                for (Iterator it = internalTextTus.iterator(); it.hasNext();)
                {
                    TuImpl tu = (TuImpl) it.next();
                    appliedTuTuvMap.remove(tu);
                }
            }

            /****** Priority 5 : Handle Rest TUs ******/
            if (p_sourceTuvMap.size() > appliedTuTuvMap.size())
            {
                unAppliedTus.removeAll(appliedTuTuvMap.keySet());
                for (Iterator it = unAppliedTus.iterator(); it.hasNext();)
                {
                    TuImpl tu = (TuImpl) it.next();
                    Tuv tuv = (Tuv) p_sourceTuvMap.get(tu);
                    Tuv newTuv = getTuvManager().cloneToTarget(tuv,
                            p_targetLocale);
                    if (tuv.getXliffAlt(false) != null
                            && tuv.getXliffAlt(false).size() > 0)
                    {
                        processor.addAltTrans(newTuv, tuv, p_targetLocale,
                                jobId);
                    }

                    if (tu.getTuv(newTuv.getLocaleId(), false, jobId) == null
                            || internalTextTus.contains(tu))
                    {
                        tu.addTuv(newTuv);
                        appliedTuTuvMap.put(tu, newTuv);
                    }
                }
            }

            /****** Priority 6 : add source comment ******/
            Iterator iter = p_sourceTuvMap.entrySet().iterator();
            iter = p_sourceTuvMap.entrySet().iterator();
            while (iter.hasNext())
            {
                Map.Entry entry = (Map.Entry) iter.next();
                Tuv oldTuv = (Tuv) entry.getValue();
                Tu key = (Tu) entry.getKey();

                Object newTuv = appliedTuTuvMap.get(key);

                if (newTuv != null && oldTuv != null)
                {
                    ((Tuv) newTuv).setSrcComment(oldTuv.getSrcComment());
                }
            }

            result.addAll(appliedTuTuvMap.values());
        }
        catch (Exception e)
        {
            throw new PageException(e);
        }
        finally
        {
            unAppliedTus = null;
            appliedTuTuvMap = null;
        }

        if (s_logger.isDebugEnabled())
        {
            s_logger.debug("Ending useLeveragedSegments() at " + new Date());
        }

        return result;
    }

    private IXliffProcessor xliffProFactory(SourcePage p_sourcePage)
    {
        ExtractedSourceFile esf = (ExtractedSourceFile) p_sourcePage
                .getExtractedFile();
        boolean isPO = IFormatNames.FORMAT_PO.equals(esf.getDataType());
        IXliffProcessor processor = new XliffProcessor();

        if (isPO)
            processor = new PoProcessor();

        return processor;
    }

    /**
     * Apply xliff or PO target matches from target or alt-trans.
     */
    private HashMap<Tu, Tuv> applyXlfOrPoMatches(SourcePage p_sourcePage,
            HashMap<Tu, Tuv> p_sourceTuvMap, GlobalSightLocale p_sourceLocale,
            GlobalSightLocale p_targetLocale, Set<Tu> p_unAppliedTus,
            HashMap<Tu, Tuv> p_appliedTuTuvMap) throws TuvException,
            RemoteException, Exception
    {
        if (p_unAppliedTus == null || p_unAppliedTus.size() == 0)
        {
            return p_appliedTuTuvMap;
        }

        long jobId = p_sourcePage.getJobId();

        // 1. Some basic method variables
        TranslationMemoryProfile tmProfile = getTmProfile(p_sourcePage);
        MachineTranslationProfile mtProfile = MTProfileHandlerHelper
                .getMtProfileBySourcePage(p_sourcePage, p_targetLocale);
        long threshold = getThresholdForMatchDataSources(mtProfile, tmProfile);

        // 2. Loop to add target TUVs that don't need go through local TM/MT
        // matches into "p_appliedTuTuvMap".
        Collection<LeverageMatch> lmCollection = new ArrayList<LeverageMatch>();
        for (Iterator it = p_unAppliedTus.iterator(); it.hasNext();)
        {
            TuImpl tu = (TuImpl) it.next();
            Tuv sourceTuv = (Tuv) p_sourceTuvMap.get(tu);
            Tuv targetTuv = getTuvManager().cloneToTarget(sourceTuv, p_targetLocale);

            // 3. Add all valid "alt-trans" to target TUV, and find the best
            // "alt-trans" which is to be stored into "leverage_match".
            if (sourceTuv.getXliffAlt(false) != null
                    && sourceTuv.getXliffAlt(false).size() > 0)
            {
                processor.addAltTrans(targetTuv, sourceTuv, p_targetLocale, jobId);
            }

            ExtractedSourceFile esf = (ExtractedSourceFile) p_sourcePage.getExtractedFile();
            boolean isPO = IFormatNames.FORMAT_PO.equals(esf.getDataType());

            // 4. For XLF/PO/Passolo files
            boolean savePassoloAltTrans = true;
            String xlfOrPoTargetLan = processor.getTargetLanguage(tu, p_sourceLocale,
                    p_targetLocale);
            boolean isTargetLangMatched = isTargetLanguagesMatched(p_targetLocale, xlfOrPoTargetLan);
            if (tu.getXliffTarget() != null)
            {
                boolean isPassolo = PassoloUtil.isPassoloFile(p_sourcePage);
                if (isPassolo || isTargetLangMatched)
                {
                    // "src"
                    String sourceGxmlWithoutTopTags = sourceTuv.getGxmlExcludeTopTags();
                    String src = sourceGxmlWithoutTopTags.trim();
                    // "trg"
                    String targetGxml = tu.getXliffTargetGxml().toGxml();
                    targetGxml = ((TuvImpl) targetTuv).encodeGxmlAttributeEntities(targetGxml);
                    String targetGxmlWithoutTopTags = GxmlUtil.stripRootTag(targetGxml);
                    String trg = targetGxmlWithoutTopTags.trim();
                    String textValue = tu.getXliffTargetGxml().getTextValue();
                    if (textValue.trim().isEmpty())
                    {
                        trg = "";
                    }

                    float tmScore = getTMScore(tu, src, trg, isPassolo, tu.getPassoloState());

                    boolean isValidTarget = hasValidTarget(tu, src, trg, sourceTuv, tmScore);

                    // handle passolo target
                    if (isPassolo)
                    {
                        boolean equals = isSrcEqualsTrg(src, trg);
                        if (!Text.isBlank(trg) && "New".equals(tu.getPassoloState())
                                && ("no".equalsIgnoreCase(tu.getTranslate()) || !equals))
                        {
                            targetTuv.setGxml(targetGxml);
                            tu.addTuv(targetTuv);
                            p_appliedTuTuvMap.put(tu, targetTuv);
                            savePassoloAltTrans = false;
                        }
                    }
                    // normal xliff and PO
                    else if (isValidTarget)
                    {
                        // handle target
                        IXliffMatchesProcessor xlfProcess = new XliffMatchesProcess();
                        if (tu.hasXliffTMScoreStr())
                        {
                            xlfProcess = new WSMatchesProcess();
                        }
                        if (!trg.isEmpty())
                        {
                            xlfProcess.addTargetTuvToTu(tu, sourceTuv, targetTuv, src, trg,
                                    p_appliedTuTuvMap, tmScore, threshold, isPO);
                        }

                        // save xlf/po target into leverage match table
                        LeverageMatch lm = toLeverageMatch(p_sourcePage, sourceTuv, targetTuv,
                                isPO, tmProfile);
                        lm.setMatchedText(tu.getXliffTarget());
                        lm.setMatchedOriginalSource(sourceTuv.getGxml());
                        lm.setScoreNum(tmScore);
                        if (tmScore < 100)
                        {
                            lm.setMatchType(MatchState.FUZZY_MATCH.getName());
                        }
                        else if (isPO)
                        {
                            lm.setMatchType(MatchState.PO_EXACT_MATCH.getName());
                        }
                        else
                        {
                            lm.setMatchType(MatchState.XLIFF_EXACT_MATCH.getName());
                        }
                        lm.setProjectTmIndex(isPO ? Leverager.PO_TM_PRIORITY
                                : Leverager.XLIFF_PRIORITY);

                        lmCollection.add(lm);
                    }
                }
            }

            //save all alt-trans to "leverage_match"
            if (isTargetLangMatched && savePassoloAltTrans && sourceTuv.getXliffAlt(false) != null
                    && sourceTuv.getXliffAlt(false).size() > 0)
            {
                for (XliffAlt alt : sourceTuv.getXliffAlt(false))
                {
                    LeverageMatch lm = toLeverageMatch(p_sourcePage, sourceTuv, targetTuv, isPO,
                            tmProfile);
                    lm.setMatchedText(XLIFFStandardUtil.convertToTmx(alt.getSegment()));
                    lm.setMatchedOriginalSource(XLIFFStandardUtil.convertToTmx(alt
                            .getSourceSegment()));
                    float quality = (float) NumberUtil.PecentToDouble(alt.getQuality());
                    lm.setScoreNum(quality);
                    if (quality < 100)
                    {
                        lm.setMatchType(MatchState.FUZZY_MATCH.getName());
                    }
                    else if (isPO)
                    {
                        lm.setMatchType(MatchState.PO_EXACT_MATCH.getName());
                    }
                    else
                    {
                        lm.setMatchType(MatchState.XLIFF_EXACT_MATCH.getName());
                    }
                    lm.setProjectTmIndex(isPO ? Leverager.PO_TM_PRIORITY
                            : Leverager.XLIFF_ALT_TRANS_PRIORITY);
                    lmCollection.add(lm);
                }
            }
        }
        // Save all leverage matches into DB.
        LingServerProxy.getLeverageMatchLingManager().saveLeveragedMatches(lmCollection, jobId);

        return p_appliedTuTuvMap;
    }

    private LeverageMatch toLeverageMatch(SourcePage p_sourcePage, Tuv sourceTuv, Tuv targetTuv,
            boolean isPO, TranslationMemoryProfile tmProfile)
    {
        LeverageMatch lm = new LeverageMatch();
        lm.setSourcePageId(p_sourcePage.getIdAsLong());
        lm.setOriginalSourceTuvId(sourceTuv.getIdAsLong());
        lm.setSubId("0");
        lm.setMatchedClob(null);
        lm.setTargetLocale(targetTuv.getGlobalSightLocale());
        // XLF and PO target use "-1" as "order_num".
        lm.setOrderNum((short) TmCoreManager.LM_ORDER_NUM_START_XLF_PO_TARGET);
        lm.setMatchedTuvId(-1);
        lm.setTmId(0);
        lm.setTmProfileId(tmProfile.getIdAsLong());
        lm.setMtName(null);

        lm.setSid(sourceTuv.getSid());
        lm.setCreationUser(sourceTuv.getCreatedUser());
        lm.setCreationDate(sourceTuv.getLastModified());
        lm.setModifyDate(sourceTuv.getLastModified());
        return lm;
    }

    /**
     * Apply local TM matches to target TUVs.
     */
    private HashMap<Tu, Tuv> applyLocalTmMatches(SourcePage p_sourcePage,
            HashMap<Tu, Tuv> p_sourceTuvMap, GlobalSightLocale p_sourceLocale,
            GlobalSightLocale p_targetLocale, TermLeverageResult p_termMatches,
            boolean p_useLeveragedTerms,
            ExactMatchedSegments p_exactMatchedSegments,
            Set<Tu> p_unAppliedTus, HashMap<Tu, Tuv> p_appliedTuTuvMap)
            throws Exception
    {
        if (p_unAppliedTus == null || p_unAppliedTus.size() == 0)
        {
            return p_appliedTuTuvMap;
        }

        long jobId = p_sourcePage.getJobId();
        TranslationMemoryProfile tmProfile = getTmProfile(p_sourcePage);
        MachineTranslationProfile mtProfile = MTProfileHandlerHelper
                .getMtProfileBySourcePage(p_sourcePage, p_targetLocale);
        long threshold = getThresholdForMatchDataSources(mtProfile, tmProfile);

        // Get mode
        int mode;
        if (tmProfile.isLatestMatchForReimport())
        {
            mode = LeverageOptions.PICK_LATEST;
        }
        else
        {
            mode = tmProfile.getMultipleExactMatcheMode();
        }

        LeverageMatchLingManager leverageMatchLingManager = LingServerProxy
                .getLeverageMatchLingManager();
        // All 100% matches (=100)
        Map<Long, ArrayList<LeverageSegment>> exactMap = leverageMatchLingManager
                .getExactMatchesWithSetInside(p_sourcePage.getIdAsLong(),
                        p_targetLocale.getIdAsLong(), mode, tmProfile);
        // All fuzzy matches (<100)
        Map<Long, Set<LeverageMatch>> fuzzyLeverageMatchesMap = null;
        try
        {
            fuzzyLeverageMatchesMap = leverageMatchLingManager.getFuzzyMatches(
                    p_sourcePage.getIdAsLong(), p_targetLocale.getIdAsLong());
        }
        catch (Exception ex)
        {

        }

        for (Iterator<Tu> it = p_unAppliedTus.iterator(); it.hasNext();)
        {
            TuImpl tu = (TuImpl) it.next();
            Tuv sourceTuv = (Tuv) p_sourceTuvMap.get(tu);
            Tuv targetTuv = getTuvManager().cloneToTarget(sourceTuv,
                    p_targetLocale);

            if (sourceTuv.getXliffAlt(false) != null
                    && sourceTuv.getXliffAlt(false).size() > 0)
            {
                processor.addAltTrans(targetTuv, sourceTuv, p_targetLocale,
                        jobId);
            }

            // Has 100% matches flag (=100)
            boolean hasOneHundredMatch = false;
            // Has exact matches flag (=100 and tag matching)
            boolean tuvGotChanged = false;
            if (exactMap != null && exactMap.size() > 0)
            {
                ArrayList<LeverageSegment> lss = exactMap.get(sourceTuv
                        .getIdAsLong());
                if (lss != null && lss.size() > 0)
                {
                    int hashMatchedNum = getHashMatchedNum(sourceTuv, lss);
                    LeverageSegment segment = SegmentUtil2
                            .getNextBestLeverageSegment(sourceTuv, lss);
                    while (segment != null)
                    {
                        hasOneHundredMatch = true;
                        boolean isTagMatched = SegmentUtil2.canBeModified(
                                targetTuv, segment.getSegment(), jobId);
                        // for most cases
                        if (isTagMatched)
                        {
                            targetTuv = modifyTUV(targetTuv, segment);
                            tuvGotChanged = true;
                            // If there is only one hash matched match in
                            // multiple exact matches, target TUV state should
                            // be exact match instead of not_localized for
                            // multiple translation.     
                            //GBS-4360: Part of ICE matches can't be locked in offline file
                            if (hashMatchedNum == 1
                                    && isHashValueMatched(sourceTuv, segment)
                                    && !MatchState.MULTIPLE_TRANSLATION.getName().equals(
                                            segment.getMatchType()))
                            {
                                targetTuv.setState(TuvState.EXACT_MATCH_LOCALIZED);
                            }
                            break;
                        }
                        // for special cases, had better to have
                        else
                        {
                            String segment2 = SegmentUtil2.adjustSegmentAttributeValues(
                                targetTuv.getGxmlElement(), 
                                    SegmentUtil2.getGxmlElement(segment.getSegment()),
                                        tu.getDataType());
                            isTagMatched = SegmentUtil2.canBeModified(
                                    targetTuv, segment2, jobId);
                            if (isTagMatched)
                            {
                                segment2 = getTargetGxmlFitForItsOwnSourceContent(
                                        sourceTuv, segment2, jobId);
                                segment.setSegment(segment2);
                                targetTuv = modifyTUV(targetTuv, segment);
                                tuvGotChanged = true;
                              //GBS-4360: Part of ICE matches can't be locked in offline file
                                if (hashMatchedNum == 1
                                        && isHashValueMatched(sourceTuv, segment)
                                        && !segment.getMatchType().equals(
                                                MatchState.MULTIPLE_TRANSLATION.getName()))
                                {
                                    targetTuv.setState(TuvState.EXACT_MATCH_LOCALIZED);
                                }
                                break;
                            }
                        }

                        segment = SegmentUtil2.getNextBestLeverageSegment(
                                sourceTuv, lss);
                    }
                }
            }
            // Probably these codes won't be run at all
            else if (p_exactMatchedSegments != null)
            {
                LeverageSegment ls = (LeverageSegment) p_exactMatchedSegments
                        .getLeveragedSegment(p_targetLocale,
                                sourceTuv.getIdAsLong());
                Map exactMatches = leverageMatchLingManager.getExactMatches(
                        p_sourcePage.getIdAsLong(),
                        p_targetLocale.getIdAsLong());
                // Found an exact match that can be leveraged
                if (ls != null)
                {
                    s_logger.info("****** Find match from 'p_exactMatchedSegments'. ****** ");
                    hasOneHundredMatch = true;
                    if (SegmentUtil2.canBeModified(targetTuv, ls.getSegment(),
                            jobId))
                    {
                        // CvdL: LeverageSegment match type is a string
                        // from LeverageMatchType (not LeveragedTu).
                        targetTuv = modifyTUV(targetTuv, ls);
                        tuvGotChanged = true;
                    }
                }
                else if (exactMatches.size() > 0)
                {
                    s_logger.debug("****** Find match from 'exactMatches'. ****** ");
                    LeverageSegment leverageSegment = (LeverageSegment) exactMatches
                            .get(sourceTuv.getIdAsLong());
                    if (leverageSegment != null)
                    {
                        hasOneHundredMatch = true;
                        if (SegmentUtil2.canBeModified(targetTuv,
                                leverageSegment.getSegment(), jobId))
                        {
                            targetTuv = modifyTUV(targetTuv, leverageSegment);
                            tuvGotChanged = true;
                        }
                    }
                }
            }

            // If no exact match, try if there's term matches available, unless
            // we're going to hit MT. (MT has higher priority than TB???)
            if (!tuvGotChanged && p_useLeveragedTerms && p_termMatches != null
                    && machineTranslator == null)
            {
                MatchRecordList matches = p_termMatches
                        .getMatchesForTuv(sourceTuv);

                if (matches != null && matches.size() > 0)
                {
                    TermReplacer replacer = TermReplacer.getInstance(
                            p_sourceLocale, p_targetLocale);
                    replacer.replaceTerms(targetTuv, matches);
                    tuvGotChanged = true;
                }
            }

            // Get max score_num for current TUV
            float maxScoreNum = 0f;
            if (hasOneHundredMatch)
            {
                maxScoreNum = 100;
            }
            else
            {
                maxScoreNum = getMaxScoreNum(fuzzyLeverageMatchesMap,
                        sourceTuv, "0");
            }

            // Root segment must have matches &&
            // boolean rootSegmentHasLeverage = false;
            // SortedSet tuvLeverage = leverageMatchLingManager.getTuvMatches(
            // sourceTuv.getIdAsLong(), targetTuv.getLocaleId(), "0",
            // tmProfile.isTmProcendence(), null);
            // if (tuvLeverage != null && tuvLeverage.size() > 0
            // && maxScoreNum >= tmProfileThreshold) {
            // rootSegmentHasLeverage = true;
            // }

            // Put target TUV that need not go on into result map.
            if (/* rootSegmentHasLeverage || */tuvGotChanged
                    || targetTuv.isLocalizable(jobId)
                    || maxScoreNum >= threshold)
            {
                tu.addTuv(targetTuv);
                p_appliedTuTuvMap.put(tu, targetTuv);
            }
        }

        return p_appliedTuTuvMap;
    }

    /**
     * Return the number of leverage segments that have same previous hash and
     * next hash as source TUV's.
     */
    private int getHashMatchedNum(Tuv srcTuv, List<LeverageSegment> lss)
    {
        long preHash = srcTuv.getPreviousHash();
        long nextHash = srcTuv.getNextHash();
        int num = 0;
        for (LeverageSegment ls : lss)
        {
            if (preHash != -1 && nextHash != -1
                    && preHash == ls.getPreviousHash()
                    && nextHash == ls.getNextHash())
            {
                num++;
            }
        }
        return num;
    }

    private boolean isHashValueMatched(Tuv srcTuv, LeverageSegment ls)
    {
        if (srcTuv.getPreviousHash() != -1 && srcTuv.getNextHash() != -1
                && srcTuv.getPreviousHash() == ls.getPreviousHash()
                && srcTuv.getNextHash() == ls.getNextHash())
        {
            return true;
        }
        return false;
    }

    /**
     * Hit MT to get matches for un-applied segments. MT matches will be stored
     * in DB, and 100% matches(auto-commit) will be populated into target TUVs.
     */
    private HashMap<Tu, Tuv> applyMTMatches(SourcePage p_sourcePage,
            HashMap<Tu, Tuv> p_sourceTuvMap, GlobalSightLocale p_sourceLocale,
            GlobalSightLocale p_targetLocale, Set<Tu> p_unAppliedTus,
            HashMap<Tu, Tuv> p_appliedTuTuvMap) throws Exception
    {
        if (p_unAppliedTus == null || p_unAppliedTus.size() == 0)
        {
            return p_appliedTuTuvMap;
        }

        long jobId = p_sourcePage.getJobId();
        MachineTranslationProfile mtProfile = MTProfileHandlerHelper
                .getMtProfileBySourcePage(p_sourcePage, p_targetLocale);
        TranslationMemoryProfile tmProfile = getTmProfile(p_sourcePage);

        HashMap<Tu, Tuv> needHitMTTuTuvMap = new HashMap<Tu, Tuv>();
        needHitMTTuTuvMap = formTuTuvMap(p_unAppliedTus, p_sourceTuvMap,
                p_targetLocale, jobId);

        XmlEntities xe = new XmlEntities();
        // put all TUs into array.
        Object[] key_tus = needHitMTTuTuvMap.keySet().toArray();
        Tu[] tusInArray = new Tu[key_tus.length];
        for (int key = 0; key < key_tus.length; key++)
        {
            tusInArray[key] = (Tu) key_tus[key];
        }
        // put all target TUVs into array
        Object[] value_tuvs = needHitMTTuTuvMap.values().toArray();
        Tuv[] targetTuvsInArray = new Tuv[value_tuvs.length];
        for (int value = 0; value < value_tuvs.length; value++)
        {
            targetTuvsInArray[value] = (Tuv) value_tuvs[value];
        }
        // put all GXML into array
        String[] p_segments = new String[targetTuvsInArray.length];
        String[] p_segmentsWithoutTags = new String[targetTuvsInArray.length];
        for (int index = 0; index < targetTuvsInArray.length; index++)
        {
            String segment = targetTuvsInArray[index].getGxml();
            TuvImpl tuv = new TuvImpl();
            if (p_sourcePage.getExternalPageId().endsWith(".idml"))
            {
                segment = IdmlHelper.formatForOfflineDownload(segment);
            }

            tuv.setSegmentString(segment);
            p_segments[index] = segment;

            // Asia Online text value can't include "<" and ">"
            String textValue = tuv.getGxmlElement().getTextValue();
            String engineName = machineTranslator.getEngineName();
            if (engineName != null
                    && engineName
                            .equalsIgnoreCase(MachineTranslator.ENGINE_ASIA_ONLINE))
            {
                textValue = xe.encodeStringBasic(textValue);
            }
            p_segmentsWithoutTags[index] = textValue;
        }

        // Send all segments to MT engine for translation.
        s_logger.info("Begin to hit " + machineTranslator.getEngineName()
                + "(Segment number:" + p_segments.length + "; SourcePageID:"
                + p_sourcePage.getIdAsLong() + "; TargetLocale:"
                + p_targetLocale.getLocaleCode() + ").");
        String[] translatedSegments = machineTranslator.translateBatchSegments(
                p_sourceLocale.getLocale(), p_targetLocale.getLocale(),
                p_segments, LeverageMatchType.CONTAINTAGS, true);

        String[] translatedSegmentsWithoutTags = null;
        if (MTHelper.willHitTwice(machineTranslator.getEngineName()))
        {
            translatedSegmentsWithoutTags = machineTranslator
                    .translateBatchSegments(p_sourceLocale.getLocale(),
                            p_targetLocale.getLocale(), p_segmentsWithoutTags,
                            LeverageMatchType.WITHOUTTAGS, false);
        }
        s_logger.info("End hit " + machineTranslator.getEngineName()
                + "(SourcePageID:" + p_sourcePage.getIdAsLong()
                + "; TargetLocale:" + p_targetLocale.getLocaleCode() + ").");
        // handle translate result one by one.
        Collection<LeverageMatch> lmCollection = new ArrayList<LeverageMatch>();
        for (int tuvIndex = 0; tuvIndex < targetTuvsInArray.length; tuvIndex++)
        {
            Tu currentTu = tusInArray[tuvIndex];
            Tuv sourceTuv = (Tuv) p_sourceTuvMap.get(currentTu);
            Tuv currentNewTuv = targetTuvsInArray[tuvIndex];

            String machineTranslatedGxml = null;
            if (translatedSegments != null
                    && translatedSegments.length == targetTuvsInArray.length)
            {
                machineTranslatedGxml = translatedSegments[tuvIndex];
            }
            boolean isGetMTResult = isValidMachineTranslation(machineTranslatedGxml);

            boolean tagMatched = true;
            if (isGetMTResult
                    && MTHelper.needCheckMTTranslationTag(machineTranslator
                            .getEngineName()))
            {
                tagMatched = SegmentUtil2.canBeModified(currentNewTuv,
                        machineTranslatedGxml, jobId);
            }
            // replace the content in target tuv with mt result
            if (isGetMTResult && tagMatched)
            {
                // GBS-3722
                if (mtProfile.isIncludeMTIdentifiers())
                {
                    String leading = mtProfile.getMtIdentifierLeading();
                    String trailing = mtProfile.getMtIdentifierTrailing();
                    if (!StringUtil.isEmpty(leading)
                            || !StringUtil.isEmpty(trailing))
                    {
                        machineTranslatedGxml = MTHelper
                                .tagMachineTranslatedContent(
                                        machineTranslatedGxml, leading,
                                        trailing);
                    }
                }
                currentNewTuv.setGxml(MTHelper
                        .fixMtTranslatedGxml(machineTranslatedGxml));
                currentNewTuv.setMatchType(LeverageMatchType.UNKNOWN_NAME);
                currentNewTuv.setLastModifiedUser(machineTranslator
                        .getEngineName() + "_MT");
                // mark TUVs as localized so they get committed to the TM
                TuvImpl t = (TuvImpl) currentNewTuv;
                t.setState(com.globalsight.everest.tuv.TuvState.LOCALIZED);
            }

            // save MT match into "leverage_match"
            if (isGetMTResult == true)
            {
                LeverageMatch lm = new LeverageMatch();
                lm.setSourcePageId(p_sourcePage.getIdAsLong());

                lm.setOriginalSourceTuvId(sourceTuv.getIdAsLong());
                lm.setSubId("0");
                lm.setMatchedText(machineTranslatedGxml);
                lm.setMatchedClob(null);
                lm.setTargetLocale(currentNewTuv.getGlobalSightLocale());
                // This is the first MT matches,its order number is 301.
                lm.setOrderNum((short) TmCoreManager.LM_ORDER_NUM_START_MT);
                lm.setScoreNum(MachineTranslator.MT_SCORE);
                lm.setMatchType(MatchState.MACHINE_TRANSLATION.getName());
                lm.setMatchedTuvId(-1);
                lm.setProjectTmIndex(Leverager.MT_PRIORITY);
                lm.setTmId(0);
                lm.setTmProfileId(tmProfile.getIdAsLong());
                lm.setMtName(machineTranslator.getEngineName() + "_MT");
                lm.setMatchedOriginalSource(sourceTuv.getGxml());

                lm.setCreationUser(machineTranslator.getEngineName() + "_MT");
                lm.setCreationDate(sourceTuv.getLastModified());
                lm.setModifyDate(sourceTuv.getLastModified());

                lmCollection.add(lm);
            }

            // save without tags mt match into "leverage_match", save only if
            // string contains tags
            // If source has no tags, no need to leverage
            // translatedSegmentsWithoutTags
            String segment = sourceTuv.getGxml();
            if (p_sourcePage.getExternalPageId().endsWith(".idml"))
            {
                segment = IdmlHelper.formatForOfflineDownload(segment);
            }
            String segWithoutRootTag = GxmlUtil.stripRootTag(segment);
            String textValue = sourceTuv.getGxmlElement().getTextValue();
            boolean hasTagInSource = !textValue.equals(xe
                    .decodeStringBasic(segWithoutRootTag));

            if (hasTagInSource
                    && translatedSegmentsWithoutTags != null
                    && translatedSegmentsWithoutTags.length == targetTuvsInArray.length)
            {
                String startTag = targetTuvsInArray[tuvIndex].getGxmlElement()
                        .getStartTag();
                String endTag = targetTuvsInArray[tuvIndex].getGxmlElement()
                        .getEndTag();

                // encode is needed here to convert tags (', <, > and
                // etc) in sentence
                String matchString = translatedSegmentsWithoutTags[tuvIndex];
                if (matchString == null || "null".equalsIgnoreCase(matchString))
                {
                    matchString = "";
                }
                String target = startTag + xe.encodeStringBasic(matchString)
                        + endTag;
                String origin = startTag
                        + xe.encodeStringBasic(p_segmentsWithoutTags[tuvIndex])
                        + endTag;

                if (!"".equals(matchString.trim()) && !target.equals(origin))
                {
                    LeverageMatch lm = new LeverageMatch();
                    lm.setSourcePageId(p_sourcePage.getIdAsLong());
                    lm.setOriginalSourceTuvId(sourceTuv.getIdAsLong());
                    lm.setSubId("0");
                    lm.setMatchedText(target);
                    lm.setMatchedClob(null);
                    lm.setTargetLocale(currentNewTuv.getGlobalSightLocale());
                    // This is the second MT matches,its order number is 302.
                    lm.setOrderNum((short) (TmCoreManager.LM_ORDER_NUM_START_MT + 1));
                    lm.setScoreNum(MachineTranslator.MT_SCORE);
                    lm.setMatchType(MatchState.MACHINE_TRANSLATION.getName());
                    lm.setMatchedTuvId(-1);
                    lm.setProjectTmIndex(Leverager.MT_PRIORITY);
                    lm.setTmId(0);
                    lm.setTmProfileId(tmProfile.getIdAsLong());
                    lm.setMtName(machineTranslator.getEngineName() + "_MT");
                    lm.setMatchedOriginalSource(sourceTuv.getGxml());

                    // lm.setSid(sourceTuv.getSid());
                    lm.setCreationUser(machineTranslator.getEngineName() + "_MT");
                    lm.setCreationDate(sourceTuv.getLastModified());
                    lm.setModifyDate(sourceTuv.getLastModified());

                    lmCollection.add(lm);
                }
            }

            // reset newTuv in result
            currentTu.addTuv(currentNewTuv);
            p_appliedTuTuvMap.put((TuImpl) currentTu, currentNewTuv);
        }
        // Save the LMs into DB
        LingServerProxy.getLeverageMatchLingManager().saveLeveragedMatches(
                lmCollection, jobId);

        /****** END :: Hit MT to get matches if configured ******/

        return p_appliedTuTuvMap;
    }

    // ///////////////////// Sub Private Methods //////////////////////
    /**
     * Get TmProfile by source page.
     */
    private TranslationMemoryProfile getTmProfile(SourcePage p_sourcePage)
    {
        L10nProfile l10nProfile = p_sourcePage.getRequest().getL10nProfile();
        TranslationMemoryProfile tmProfile = l10nProfile
                .getTranslationMemoryProfile();

        return tmProfile;
    }

    /**
     * To invoke MT, extra parameters must be transformed to MT engine for
     * "PROMT","MS_TRANSLATOR" or "ASIA_ONLINE".
     * 
     * @param tmProfile
     */

    /**
     * Checks to see if the locale pair is supported by the MT engine
     * 
     * @param p_sourceLocale
     * @param p_targetLocale
     * @return true | false
     */
    private boolean isLocalePairSupportedByMT(GlobalSightLocale p_sourceLocale,
            GlobalSightLocale p_targetLocale)
    {
        if (machineTranslator == null)
        {
            return false;
        }

        boolean isSupported = false;

        try
        {
            isSupported = machineTranslator.supportsLocalePair(
                    p_sourceLocale.getLocale(), p_targetLocale.getLocale());
        }
        catch (Exception e)
        {
            s_logger.error(
                    "Failed to find if locale pair ("
                            + p_sourceLocale.getLocale() + "->"
                            + p_targetLocale.getLocale()
                            + " is supported by MT "
                            + machineTranslator.getEngineName(), e);
        }

        if (isSupported)
        {
            if (s_logger.isDebugEnabled())
            {
                s_logger.info("Using machine translation to convert non-exact matches for locale pair "
                        + p_sourceLocale.getLocale()
                        + "->"
                        + p_targetLocale.getLocale()
                        + " with MT engine "
                        + machineTranslator.getEngineName());
            }
        }
        else
        {
            s_logger.warn("Machine translation is desired, but not supported for locale pair "
                    + p_sourceLocale.getLocale()
                    + "->"
                    + p_targetLocale.getLocale()
                    + " for MT engine "
                    + machineTranslator.getEngineName());
        }

        return isSupported;
    }

    private TuvManager getTuvManager() throws Exception
    {
        return ServerProxy.getTuvManager();
    }

    /**
     * Check if the target language from current TU/TUV is same with target
     * locale of current workflow.
     */
    private boolean isTargetLanguagesMatched(GlobalSightLocale p_locale,
            String p_xlfOrPoTargetLanguage)
    {
        boolean result = false;
        // Some xliff file target language maybe use "fr-FR",not "fr_FR", that
        // also should be considered same.
        String newLang = p_locale.getLanguage() + "-" + p_locale.getCountry();
        if (p_xlfOrPoTargetLanguage != null
                && (p_xlfOrPoTargetLanguage.equalsIgnoreCase(p_locale
                        .toString()) || p_xlfOrPoTargetLanguage
                        .equalsIgnoreCase(newLang)))
        {
            result = true;
        }

        return result;
    }

    private boolean isSrcEqualsTrg(String p_srcSegment, String p_trgSegment)
    {
        boolean equals = p_srcSegment.equals(p_trgSegment)
                || EditUtil.decodeXmlEntities(p_trgSegment).equals(
                        EditUtil.decodeXmlEntities(p_srcSegment));

        return equals;
    }

    private boolean hasValidTarget(TuImpl tu, String p_srcSegment,
            String p_trgSegment, Tuv p_sourceTuv, float p_tmScore)
    {
        if (Text.isBlank(p_srcSegment) || Text.isBlank(p_trgSegment))
            return false;

        if ("no".equals(tu.getTranslate()))
        {
            return true;
        }

        boolean equals = isSrcEqualsTrg(p_srcSegment, p_trgSegment);
        if (!tu.hasXliffTMScoreStr())
        {
            if (!equals)
            {
                return true;
            }
        }
        // If tmscore is 100, but target is null or empty, should not save the
        // empty leverage match.
        else if (p_tmScore == 100)
        {
            return true;
        }
        else
        {
            if (!equals)
            {
                return true;
            }
        }

        return false;
    }

    private float getTMScore(TuImpl tu, String p_srcSegment,
            String p_trgSegment, boolean isPassolo, String passoloState)
    {
        // IWS XLIFF
        if (tu.hasXliffTMScoreStr())
        {
            try
            {
                return Float.valueOf(tu.getIwsScore());
            }
            catch (NumberFormatException ignore)
            {

            }
        }

        // Passolo
        if (isPassolo
                && ("Translated and reviewed".equals(passoloState) || "Translated"
                        .equals(passoloState)))
        {
            return 100;
        }

        // translate="no"
        boolean equals = isSrcEqualsTrg(p_srcSegment, p_trgSegment);
        if (!Text.isBlank(p_trgSegment)
                && ("no".equalsIgnoreCase(tu.getTranslate()) || !equals))
        {
            return 100;
        }

        return 0;
    }

    private Tuv modifyTUV(Tuv tuv, LeverageSegment leverageSegment)
            throws Exception
    {
        if (leverageSegment != null)
        {
            try
            {
                tuv.setGxml(leverageSegment.getSegment());
            }
            catch (Exception e)
            {
                s_logger.error(
                        "Exception when getting the gxml for a leveraged segment.",
                        e);
                s_logger.error("+++The leverage match string is "
                        + leverageSegment.getSegment() + "+++");
                throw e;
            }
            tuv.setMatchType(leverageSegment.getMatchType());

            if (leverageSegment.getMatchType() == null)
            {
                tuv.setMatchType(LeverageMatchType.UNKNOWN_NAME);
            }

            if (s_logger.isDebugEnabled())
            {
                s_logger.debug("Leveraged target tuv with match type "
                        + leverageSegment.getMatchType() + " is " + tuv);
            }
        }

        return tuv;
    }

    private float getMaxScoreNum(
            Map<Long, Set<LeverageMatch>> p_fuzzyLeverageMatchesMap,
            Tuv p_sourceTuv, String p_subId)
    {
        float maxScoreNum = 0f;

        Set<LeverageMatch> leverageMatches = new HashSet<LeverageMatch>();
        if (p_fuzzyLeverageMatchesMap != null
                && p_fuzzyLeverageMatchesMap.size() > 0)
        {
            leverageMatches = p_fuzzyLeverageMatchesMap.get(p_sourceTuv
                    .getIdAsLong());
        }

        if (leverageMatches != null && leverageMatches.size() > 0)
        {
            Iterator<LeverageMatch> lmIt = leverageMatches.iterator();
            while (lmIt.hasNext())
            {
                LeverageMatch leverageMatch = lmIt.next();
                if (p_subId != null && p_subId.equals(leverageMatch.getSubId()))
                {
                    float tmpScoreNum = leverageMatch.getScoreNum();
                    if (tmpScoreNum > maxScoreNum)
                    {
                        maxScoreNum = tmpScoreNum;
                    }
                }
            }
        }

        return maxScoreNum;
    }

    private HashMap<Tu, Tuv> formTuTuvMap(Set<Tu> p_unAppliedTus,
            HashMap<Tu, Tuv> p_sourceTuvMap, GlobalSightLocale p_targetLocale,
            long p_jobId) throws TuvException, RemoteException, Exception
    {
        HashMap<Tu, Tuv> result = new HashMap<Tu, Tuv>();

        for (Iterator it = p_unAppliedTus.iterator(); it.hasNext();)
        {
            TuImpl tu = (TuImpl) it.next();
            Tuv sourceTuv = (Tuv) p_sourceTuvMap.get(tu);
            Tuv targetTuv = getTuvManager().cloneToTarget(sourceTuv,
                    p_targetLocale);

            if (sourceTuv.getXliffAlt(false) != null
                    && sourceTuv.getXliffAlt(false).size() > 0)
            {
                processor.addAltTrans(targetTuv, sourceTuv, p_targetLocale,
                        p_jobId);
            }

            result.put(tu, targetTuv);
        }

        return result;
    }

    private long getThresholdForMatchDataSources(MachineTranslationProfile mt,
            TranslationMemoryProfile tmProfile)
    {
        // Default is TM profile's threshold.
        long result = tmProfile.getFuzzyMatchThreshold();

        if (mt != null && mt.isActive())
        {
            result = mt.getMtThreshold();
        }

        return result;
    }

    /**
     * A machine translated gxml can't be null, empty, only tags, and should be
     * valid gxml.
     * 
     * @param machineTranslatedGxml
     * @return
     */
    private boolean isValidMachineTranslation(String machineTranslatedGxml)
    {
        boolean result = false;

        if (machineTranslatedGxml != null
                && !"".equals(machineTranslatedGxml)
                && !"".equals(GxmlUtil.stripRootTag(machineTranslatedGxml)
                        .trim()))
        {
            // As the MT returned translation may be invalid XML string,it
            // should not fail the job creation process.
            try
            {
                // Perhaps the MT results include nothing except for tags
                String textValue = SegmentUtil2.getGxmlElement(
                        machineTranslatedGxml).getTextValue();
                if (!"".equals(textValue.trim()))
                {
                    result = true;
                }
            }
            catch (Exception ignore)
            {
                s_logger.warn("The machine translation is not valid, will be ignored.");
            }
        }

        return result;
    }

    /**
     * When hit MT, MT may return word count info. This info will be cached in
     * "MTHelper2", here store them into target pages in DB.
     * 
     * @param p_sourcePage
     * @param p_targetPages
     */
    protected void updateMtEngineWordCount(SourcePage p_sourcePage,
            List<TargetPage> p_targetPages)
    {
        long spId = p_sourcePage.getId();
        for (TargetPage tp : p_targetPages)
        {
            GlobalSightLocale targetLocale = tp.getGlobalSightLocale();
            long trgLocaleId = targetLocale.getId();
            String key = spId + "_" + trgLocaleId;
            Integer value = MTHelper2.getValue(key);
            if (value != null && value.intValue() > 0)
            {
                PageWordCounts wc = tp.getWordCount();
                int curWc = wc.getMtEngineWordCount() + value.intValue();
                wc.setMtEngineWordCount(curWc);
                HibernateUtil.update(tp);

                MTHelper2.removeValue(key);
            }
        }
    }

    private String getTargetGxmlFitForItsOwnSourceContent(Tuv p_sourceTuv,
            String p_gxml, long p_jobId)
    {
        try
        {
            String srcGxml = p_sourceTuv.getGxml();
            int index = srcGxml.indexOf(">");
            String startSegment = srcGxml.substring(0, index + 1);

            OnlineTagHelper sourceTagHelper = new OnlineTagHelper();
            sourceTagHelper.setInputSegment(
                    p_sourceTuv.getGxmlExcludeTopTags(), "",
                    p_sourceTuv.getDataType(p_jobId));
            sourceTagHelper.getCompact();// This step is required

            OnlineTagHelper targetTagHelper = new OnlineTagHelper();
            targetTagHelper.setInputSegment(GxmlUtil.stripRootTag(p_gxml), "",
                    p_sourceTuv.getDataType(p_jobId));
            String compact = targetTagHelper.getCompact();
            // Combine source tag info and target content info
            String targetGxml = sourceTagHelper.getTargetDiplomat(compact);

            return startSegment + targetGxml + "</segment>";
        }
        catch (Exception e)
        {
            return p_gxml;
        }
    }
}
