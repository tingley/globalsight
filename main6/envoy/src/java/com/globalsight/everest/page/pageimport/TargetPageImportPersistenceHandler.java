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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
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
import java.util.SortedSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.globalsight.cxe.adapter.idml.IdmlHelper;
import com.globalsight.everest.comment.IssueEditionRelation;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.integration.ling.LingServerProxy;
import com.globalsight.everest.integration.ling.tm2.LeverageMatch;
import com.globalsight.everest.page.ExtractedFile;
import com.globalsight.everest.page.ExtractedSourceFile;
import com.globalsight.everest.page.PageException;
import com.globalsight.everest.page.PageState;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.page.UnextractedFile;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.request.Request;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.tda.LeverageTDAResult;
import com.globalsight.everest.tda.TdaHelper;
import com.globalsight.everest.tuv.LeverageGroup;
import com.globalsight.everest.tuv.Tu;
import com.globalsight.everest.tuv.TuImpl;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.tuv.TuvImpl;
import com.globalsight.everest.tuv.TuvManager;
import com.globalsight.everest.tuv.TuvState;
import com.globalsight.ling.common.DiplomatBasicParser;
import com.globalsight.ling.common.DiplomatBasicParserException;
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
import com.globalsight.ling.tw.PseudoCodec;
import com.globalsight.ling.tw.PseudoConstants;
import com.globalsight.ling.tw.PseudoData;
import com.globalsight.ling.tw.PseudoErrorChecker;
import com.globalsight.ling.tw.PseudoParserException;
import com.globalsight.ling.tw.Tmx2PseudoHandler;
import com.globalsight.ling.tw.TmxPseudo;
import com.globalsight.ling.tw.internal.XliffInternalTag;
import com.globalsight.log.GlobalSightCategory;
import com.globalsight.machineTranslation.MachineTranslator;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.terminology.termleverager.TermLeverageResult;
import com.globalsight.terminology.termleverager.TermLeverageResult.MatchRecordList;
import com.globalsight.terminology.termleverager.replacer.TermReplacer;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.FileUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.edit.GxmlUtil;

public class TargetPageImportPersistenceHandler
{
    private static GlobalSightCategory s_logger = (GlobalSightCategory) GlobalSightCategory
            .getLogger(TargetPageImportPersistenceHandler.class);

    private MachineTranslator m_machineTranslator = null;

    private boolean m_autoCommitMtToTm = false;

    /**
     * A map from TUs to TUVs in the source locale for internal use.
     */
    public static class Tu2TuvMap extends HashMap
    {
        private static final long serialVersionUID = 8559871199545960535L;

        public void putTu2Tuv(Tu p_tu, Tuv p_tuv)
        {
            this.put(p_tu, p_tuv);
        }

        public Tuv getTuv(Tu p_tu)
        {
            return (Tuv) this.get(p_tu);
        }

        public Set getTus()
        {
            return this.keySet();
        }
    }

    //
    // Constructor
    //
    public TargetPageImportPersistenceHandler(
            MachineTranslator p_machineTranslator, boolean autoCommitToTm)
    {
        m_machineTranslator = p_machineTranslator;
        m_autoCommitMtToTm = autoCommitToTm;
    }

    public Collection persistObjectsWithUnextractedFile(
            SourcePage p_sourcePage, Collection p_targetLocales)
            throws PageException
    {
        ArrayList targetPages = new ArrayList();

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
                StringBuffer fileName = new StringBuffer(Long
                        .toString(p_sourcePage.getRequest().getId()));

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
                s_logger
                        .debug("Persisting target pages for a source page with an unextracted file.");
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
    public Collection persistFailedObjectsWithExtractedFile(
            SourcePage p_sourcePage, Hashtable p_targetLocaleErrors)
            throws PageException
    {
        ArrayList targetPages = new ArrayList();

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

    public Collection persistObjectsWithExtractedFile(SourcePage p_sourcePage,
            Collection p_targetLocales, TermLeverageResult p_termMatches,
            boolean p_useLeveragedSegments, boolean p_useLeveragedTerms,
            ExactMatchedSegments p_exactMatchedSegments) throws PageException
    {
        ArrayList targetPages = new ArrayList();

        // I think Aswin wanted to say that the sequence numbers are
        // allocated inside a separate transaction and their values
        // will be lost if the any one of the calls fails. Hence the
        // separate try-catch statements.

        List levertages = p_sourcePage.getExtractedFile().getLeverageGroupSet();

        Session session = null;
        Transaction transaction = null;

        try
        {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            for (Iterator it = p_targetLocales.iterator(); it.hasNext();)
            {
                GlobalSightLocale targetLocale = (GlobalSightLocale) it.next();

                TargetPage targetPage = new TargetPage(targetLocale,
                        p_sourcePage);
                targetPage.setTimestamp(new Timestamp(System
                        .currentTimeMillis()));
                targetPage.getExtractedFile().getLeverageGroupSet().addAll(
                        levertages);

                session.save(targetPage);
                targetPages.add(targetPage);
            }

            long time_PERFORMANCE = System.currentTimeMillis();

            Set tuvs = getPersistenceTuvs(p_sourcePage, p_targetLocales,
                    p_termMatches, p_useLeveragedSegments, p_useLeveragedTerms,
                    p_exactMatchedSegments);

            if (s_logger.isDebugEnabled())
            {
                s_logger.debug("Performance:: createTuvPersistenceCommand for "
                        + p_sourcePage.getExternalPageId() + " time = "
                        + (System.currentTimeMillis() - time_PERFORMANCE));

                s_logger
                        .debug("Persisting target pages for a source page with an extracted file.");
            }

            time_PERFORMANCE = System.currentTimeMillis();

            Iterator iterator = tuvs.iterator();
            while (iterator.hasNext())
            {
                session.save(iterator.next());
            }

            if (s_logger.isDebugEnabled())
            {
                s_logger.debug("Performance:: Persists target pages for "
                        + p_sourcePage.getExternalPageId() + " time = "
                        + (System.currentTimeMillis() - time_PERFORMANCE));
            }

            transaction.commit();
        }
        catch (Exception e)
        {
            if (transaction != null)
            {
                transaction.rollback();
            }

            s_logger.error("The target page could not be persisted", e);
            throw new PageException(e);
        }
        finally
        {
            if (session != null)
            {
                // session.close();
            }
        }

        return targetPages;
    }

    private Set getPersistenceTuvs(SourcePage p_sourcePage,
            Collection p_targetLocales, TermLeverageResult p_termMatches,
            boolean p_useLeveragedSegments, boolean p_useLeveragedTerms,
            ExactMatchedSegments p_exactMatchedSegments) throws PageException
    {
        Set tuvs = new HashSet();

        try
        {
            Tu2TuvMap sourceTuvMap = getSourceTuvMap(p_sourcePage);

            for (Iterator it = p_targetLocales.iterator(); it.hasNext();)
            {
                GlobalSightLocale targetLocale = (GlobalSightLocale) it.next();

                ArrayList targetTuvs = getTargetTuvs(p_sourcePage,
                        sourceTuvMap, targetLocale, p_termMatches,
                        p_useLeveragedTerms, p_exactMatchedSegments);
                tuvs.addAll(targetTuvs);
            }
        }
        catch (PageException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new PageException(e);
        }

        return tuvs;
    }

    private ArrayList getTargetTuvs(SourcePage p_sourcePage,
            Tu2TuvMap p_sourceTuvMap, GlobalSightLocale p_targetLocale,
            TermLeverageResult p_termMatches, boolean p_useLeveragedTerms,
            ExactMatchedSegments p_exactMatchedSegments) throws PageException
    {
        GlobalSightLocale sourceLocale = p_sourcePage.getGlobalSightLocale();

        ArrayList result = useLeveragedSegments(p_sourcePage, p_sourceTuvMap,
                sourceLocale, p_targetLocale, p_termMatches,
                p_useLeveragedTerms, p_exactMatchedSegments);

        return result;
    }

    /**
     * For all source TUVs, generates target TUVs in the requested locale and
     * populates them with 100% exact matches from TM leveraging, or copies of
     * the original source TUV. If terminology matches are passed in, then
     * automatic term replacement is performed.
     * 
     * @return a List containing all target TUVs for the requested locale.
     */
    public ArrayList useLeveragedSegments(SourcePage p_sourcePage,
            Tu2TuvMap p_sourceTuvMap, GlobalSightLocale p_sourceLocale,
            GlobalSightLocale p_targetLocale, TermLeverageResult p_termMatches,
            boolean p_useLeveragedTerms,
            ExactMatchedSegments p_exactMatchedSegments) throws PageException
    {
        Set tus = p_sourceTuvMap.getTus();
        ArrayList result = new ArrayList(tus.size());
        TermReplacer replacer = TermReplacer.getInstance(p_sourceLocale,
                p_targetLocale);

        // get threshold for current source page
        Request request = p_sourcePage.getRequest();
        L10nProfile l10nProfile = request.getL10nProfile();
        TranslationMemoryProfile tmProfile = l10nProfile
                .getTranslationMemoryProfile();

        long tmProfileThreshold = tmProfile.getFuzzyMatchThreshold();
        long mtSensitivePenalty = 0;
        if (tmProfile.getIsMTSensitiveLeveraging())
        {
            mtSensitivePenalty = tmProfile.getMtSensitivePenalty();
        }
        long mtThreshold = 100 - mtSensitivePenalty;

        int mode;
        if (tmProfile.isLatestMatchForReimport())
        {
            mode = LeverageOptions.PICK_LATEST;
        }
        else
        {
            mode = tmProfile.getMultipleExactMatcheMode();
        }

        // prepare possible leverage match data from "leverage_match" table
        LeverageMatchLingManager leverageMatchLingManager = LingServerProxy
                .getLeverageMatchLingManager();
        Map<Long, ArrayList<LeverageSegment>> exactMap = leverageMatchLingManager
                .getExactMatchesWithSetInside(p_sourcePage.getIdAsLong(),
                        p_targetLocale.getIdAsLong(), mode, tmProfile);
        Map fuzzyLeverageMatchesMap = null;
        try
        {
            fuzzyLeverageMatchesMap = leverageMatchLingManager.getFuzzyMatches(
                    p_sourcePage.getIdAsLong(), p_targetLocale.getIdAsLong());
        }
        catch (Exception ex)
        {
        }

        // To invoke MT, extra parameters must be transformed to MT engine for
        // "PROMT","MS_TRANSLATOR" or "ASIA_ONLINE"
        setExtraOptionsForMT(tmProfile, p_sourcePage);

        // This must be invoked after "setExtraOptionsForMT()" API invoking.
        boolean isLocalePairSupportedByMT = isLocalePairSupportedByMT(
                p_sourceLocale, p_targetLocale);

        if (s_logger.isDebugEnabled())
        {
            s_logger.debug("Starting useLeveragedSegments() at " + new Date());
        }

        String targetLan = p_targetLocale.getLanguage().toLowerCase();

        int lm_projectTmIndex = Leverager.XLIFF_PRIORITY;
        String lastModifiedUser = IFormatNames.FORMAT_XLIFF_NAME;

        ExtractedSourceFile esf = (ExtractedSourceFile) p_sourcePage
                .getExtractedFile();
        String dataType = esf.getDataType();
        boolean isPO = IFormatNames.FORMAT_PO.equals(dataType);
        if (isPO)
        {
            lm_projectTmIndex = Leverager.PO_TM_PRIORITY;
            lastModifiedUser = IFormatNames.FORMAT_PO.toUpperCase();
        }

        try
        {
            // All Tuvs that need hit MT are put in this map.(Tu :: Tuv)
            HashMap needHitMTTuTuvMap = new HashMap();
            // All Tuvs than need leverage by use TDA TM
            HashMap needHitTDAMap = new HashMap();
            // recorder the tu which don't get leverage results from TDA TM
            HashMap notGetTdaMap = new HashMap();

            for (Iterator it = tus.iterator(); it.hasNext();)
            {
                boolean tuvGotChanged = false; // true for exact matches and MT
                TuImpl tu = (TuImpl) it.next();
                Tuv tuv = (Tuv) p_sourceTuvMap.getTuv(tu);
                Tuv newTuv = getTuvManager().cloneToTarget(tuv, p_targetLocale);

                String xliffLan = tu.getXliffTargetLanguage();

                if (xliffLan != null)
                {
                    xliffLan = xliffLan.toLowerCase();
                }

                if (isPO && xliffLan != null && xliffLan.length() == 2)
                {
                    xliffLan = xliffLan + "_" + p_targetLocale.getCountry();
                }

                XliffAlt maxScoreAlt = new XliffAlt();
                double maxAltTransScore = 0;
                
                if (tuv.getXliffAlt() != null && tuv.getXliffAlt().size() > 0)
                {
                    String tempLan = p_targetLocale.getLanguage();

                    Iterator i = tuv.getXliffAlt().iterator();

                    while (i.hasNext())
                    {
                        XliffAlt me = (XliffAlt) i.next();
                        double temp = 0;

                        try
                        {
                            temp = Double.parseDouble(me.getQuality());
                        }
                        catch (Exception e)
                        {
                        }

                        if (me.getLanguage() == null)
                        {
                            if (xliffLan != null
                                    && xliffLan.indexOf(targetLan) > -1)
                            {
                                XliffAlt xa = new XliffAlt();
                                xa.setSegment(me.getSegment());
                                xa.setSourceSegment(me.getSourceSegment());
                                xa.setLanguage(tempLan);
                                xa.setQuality(me.getQuality());

                                newTuv.addXliffAlt(xa);

                                if (temp > maxAltTransScore)
                                {
                                    maxAltTransScore = temp;
                                    maxScoreAlt = xa;
                                }
                            }
                        }
                        else if (me.getLanguage() != null
                                && me.getLanguage().equals(tempLan))
                        {
                            XliffAlt xa = new XliffAlt();
                            xa.setSegment(me.getSegment());
                            xa.setSourceSegment(me.getSourceSegment());
                            xa.setLanguage(tempLan);
                            xa.setQuality(me.getQuality());

                            newTuv.addXliffAlt(xa);

                            if (temp > maxAltTransScore)
                            {
                                maxAltTransScore = temp;
                                maxScoreAlt = xa;
                            }
                        }
                    }
                }
                
                //if the file is xilff file
                if (tu.getXliffTarget() != null)
                {
                    // some xliff file target language maybe use "fr-FR",not
                    // "fr_FR", that also should be consider as same
                    String country = p_targetLocale.getCountry();
                    String newLan = targetLan + "-" + country;
                    if (xliffLan != null
                            && (xliffLan.equalsIgnoreCase(p_targetLocale
                                    .toString()) || xliffLan
                                    .equalsIgnoreCase(newLan)))
                    {
                        String targetGxml = tu.getXliffTargetGxml().toGxml();
                        targetGxml = ((TuvImpl) newTuv)
                                .encodeGxmlAttributeEntities(targetGxml);
                        String targetGxmlWithoutTopTags = GxmlUtil
                                .stripRootTag(targetGxml);
                        String sourceGxmlWithoutTopTags = tuv
                                .getGxmlExcludeTopTags();

                        String src = sourceGxmlWithoutTopTags.trim();
                        String trg = targetGxmlWithoutTopTags.trim();
                        String textValue = tu.getXliffTargetGxml().getTextValue();
                        
                        if(textValue.trim().isEmpty()) {
                            trg = "";
                        }

                        //-1 present has no iws tm_score
                        float tmScore = getTMScore(tu);
                        boolean equals = src.equals(trg) || EditUtil
                                .decodeXmlEntities(trg).equals(
                                        EditUtil.decodeXmlEntities(src));
                        
                        boolean needLeverageMatch = false;
                        
                        if (tmScore == -1)
                        {
                            if (!equals && !Text.isBlank(trg))
                            {
                                tmScore = 100;
                                needLeverageMatch = true;
                            }
                            else
                            {
                                tmScore = 0;
                                needLeverageMatch = false;
                            }
                        }
                        else if (tmScore == 100)
                        {
                            needLeverageMatch = true;
                        }
                        else
                        {
                            if (!Text.isBlank(trg) && !equals)
                            {
                                needLeverageMatch = true;
                            }
                            else if (tuv.getXliffAlt() != null && tuv
                                            .getXliffAlt().size() > 0)
                            {
                                needLeverageMatch = true;
                            }
                            else
                            {
                                needLeverageMatch = false;
                            }
                        }
                        /*
                         * boolean equals = (src.equals(trg) || EditUtil
                         * .decodeXmlEntities(trg).equals(
                         * EditUtil.decodeXmlEntities(src))) && tmScore < 100;
                         * 
                         * if ((!Text.isBlank(trg) && !equals) ||
                         * (tu.hasXliffTMScoreStr() && (tuv .getXliffAlt() !=
                         * null && tuv .getXliffAlt().size() > 0)))
                         */
                        if (needLeverageMatch)
                        {
                            // save XLF/PO target into "leverage_match"
                            Collection c = new ArrayList();
                            LeverageMatch lm = new LeverageMatch();
                            lm.setSourcePageId(p_sourcePage.getIdAsLong());
                            lm.setOriginalSourceTuvId(tuv.getIdAsLong());
                            lm.setSubId("0");
                            
                            if (!trg.isEmpty())
                            {
                                lm.setMatchedText(tu.getXliffTarget());
                                lm.setMatchedOriginalSource(tuv.getGxml());
                            }
                            else
                            {
                                if (tuv.getXliffAlt() != null
                                        && tuv.getXliffAlt().size() > 0)
                                {
                                    lm.setMatchedText(maxScoreAlt.getSegment());
                                    lm.setMatchedOriginalSource(maxScoreAlt
                                            .getSourceSegment());
                                }
                            }

                            lm.setMatchedClob(null);

                            lm.setTargetLocale(newTuv.getGlobalSightLocale());
                            // XLF and PO target use "-1" as order num.
                            lm.setOrderNum((short) TmCoreManager.LM_ORDER_NUM_START_XLF_PO_TARGET);
                            lm.setScoreNum(tmScore);

                            if (tmScore < 100)
                            {
                                lm.setMatchType("FUZZY_MATCH");
                            }
                            else
                            {
                                lm.setMatchType("XLIFF_EXACT_MATCH");
                            }

                            lm.setMatchedTuvId(-1);
                            lm.setProjectTmIndex(lm_projectTmIndex);
                            lm.setTmId(0);
                            lm.setTmProfileId(tmProfile.getIdAsLong());
                            lm.setMtName(null);
                            c.add(lm);

                            LingServerProxy.getLeverageMatchLingManager()
                                    .saveLeveragedMatches(c);

                            // If the translation_type is
                            // "machine_translation_mt",
                            // sets target same with source.

                            if ((tmScore >= tmProfileThreshold && tmScore < 100)
                                    || tu.isXliffTranslationMT())
                            {
                                tu.addTuv(newTuv);
                                result.add(newTuv);

                                continue;
                            }

                            // if target is not same with source,check
                            // tags
                            // in them
                            // for "offline uploading".
                            // if they have not same tags,target content
                            // can't be
                            // saved to target tuv
                            // even it is different with source
                            // content.(GBS-1211)
                            boolean hasSameTags = compareTags(src, trg);

                            if (tmScore == 100)
                            {
                                if (hasSameTags || tu.isXliffLocked())
                                {
                                    if (!trg.isEmpty())
                                    {
                                        newTuv.setGxml(tu.getXliffTarget());
                                    }

                                    newTuv.setMatchType("XLIFF_EXACT_MATCH");
                                    newTuv.setLastModifiedUser(lastModifiedUser);

                                    // If lock_status="locked",set target
                                    // TUV
                                    // state to "EXACT_MATCH_LOCALIZED" to
                                    // ensure it will be populated into
                                    // storage
                                    // TM when job is finished (GBS-1771)
                                    if (tu.isXliffLocked())
                                    {
                                        newTuv.setState(TuvState.EXACT_MATCH_LOCALIZED);
                                    }
                                    else
                                    {
                                        newTuv.setState(TuvState.NOT_LOCALIZED);
                                    }

                                    tu.addTuv(newTuv);
                                    result.add(newTuv);

                                    continue;
                                }
                                else if (!tu.hasXliffTMScoreStr())
                                {
                                    //1.source tag is diffrent with target tag
                                    //2.is ordinary xliff file
                                    //3.target content has real content except tag
                                    tu.addTuv(newTuv);
                                    result.add(newTuv);

                                    continue;
                                }
                            }
                        }
                    }
                }

                // in ExtractedFileImporte we add the IssueEditionRelation to
                // source tuv
                // now we remove it from source tuv ,and add the
                // IssueEditionRelation
                // to target tuv.
                if (((TuvImpl) tuv).getIssueEditionRelation() != null
                        && ((TuvImpl) tuv).getIssueEditionRelation().size() > 0)
                {

                    Set ieSet = ((TuvImpl) tuv).getIssueEditionRelation();
                    ((TuvImpl) tuv).setIssueEditionRelation(null);
                    ((TuvImpl) newTuv).setIssueEditionRelation(ieSet);

                    Iterator i = ieSet.iterator();

                    while (i.hasNext())
                    {
                        IssueEditionRelation me = (IssueEditionRelation) i
                                .next();
                        me.setTuv((TuvImpl) newTuv);
                    }
                }

                boolean b_haveMatch = false;

                if (exactMap != null && exactMap.size() > 0)
                {
                    ArrayList<LeverageSegment> lss = exactMap.get(tuv
                            .getIdAsLong());

                    if (lss != null && lss.size() > 0)
                    {
                        for (LeverageSegment segment : lss)
                        {
                            if (canBeModified(newTuv, segment))
                            {
                                b_haveMatch = true;
                                newTuv = modifyTUV(newTuv, segment);
                                tuvGotChanged = true;
                                break;
                            }
                        }
                    }
                }
                else
                {
                    if (p_exactMatchedSegments != null)
                    {
                        LeverageSegment ls = (LeverageSegment) p_exactMatchedSegments
                                .getLeveragedSegment(p_targetLocale, tuv
                                        .getIdAsLong());
                        Map exactMatches = leverageMatchLingManager
                                .getExactMatches(p_sourcePage.getIdAsLong(),
                                        p_targetLocale.getIdAsLong());
                        // Found an exact match that can be leveraged?
                        if (ls != null && canBeModified(newTuv, ls))
                        {
                            b_haveMatch = true;

                            // CvdL: LeverageSegment match type is a string
                            // from LeverageMatchType (not LeveragedTu).
                            newTuv = modifyTUV(newTuv, ls);

                            tuvGotChanged = true;
                        }
                        else if (exactMatches.size() > 0)
                        {
                            LeverageSegment leverageSegment = (LeverageSegment) exactMatches
                                    .get(tuv.getIdAsLong());
                            if (leverageSegment != null && ls != null
                                    && canBeModified(newTuv, ls))
                            {
                                b_haveMatch = true;
                                newTuv = modifyTUV(newTuv, leverageSegment);
                                tuvGotChanged = true;
                            }
                        }
                    }
                }

                // If no exact match, try if there's term matches
                // available, unless we're going to hit MT.
                if (m_machineTranslator == null && !b_haveMatch
                        && p_useLeveragedTerms && p_termMatches != null)
                {
                    MatchRecordList matches = p_termMatches
                            .getMatchesForTuv(tuv);

                    if (matches != null && matches.size() > 0)
                    {
                        replacer.replaceTerms(newTuv, matches);
                        tuvGotChanged = true;
                    }
                }

                // get max score_num for current tuv
                Set leverageMatches = null;
                float maxScoreNum = 0;
                if (fuzzyLeverageMatchesMap != null
                        && fuzzyLeverageMatchesMap.size() > 0)
                {
                    leverageMatches = (Set) fuzzyLeverageMatchesMap.get(tuv
                            .getIdAsLong());
                }
                if (leverageMatches != null && leverageMatches.size() > 0)
                {
                    Iterator lmIt = leverageMatches.iterator();
                    while (lmIt.hasNext())
                    {
                        LeverageMatch leverageMatch = (LeverageMatch) lmIt
                                .next();
                        float tmpScoreNum = leverageMatch.getScoreNum();
                        if (tmpScoreNum > maxScoreNum)
                        {
                            maxScoreNum = tmpScoreNum;
                        }
                    }
                }

                /*
                 * Judge if the leverage_match table have the tuv match result,
                 * if have,can't go to TDA or TM, because when TDA or TM write
                 * leverage match result into leverage_match table, the PRIMARY
                 * KEY
                 * (`ORIGINAL_SOURCE_TUV_ID`,`SUB_ID`,`TARGET_LOCALE_ID`,`ORDER_NUM
                 * `) is easy have exist and lead to hibernate save error, and
                 * job creating error.
                 */
                SortedSet tuvLeverage = leverageMatchLingManager.getTuvMatches(
                        tuv.getIdAsLong(), newTuv.getLocaleId(), "0", tmProfile
                                .isTmProcendence(), null);
                boolean hasLeverage = false;

                if (tuvLeverage != null && tuvLeverage.size() > 0
                        && maxScoreNum >= tmProfileThreshold)
                {
                    hasLeverage = true;
                }

                // @add by walter, for TDA feature
                // TDA leverage when maxScoreNum < threshold in tm profile
                boolean needHitTDA = false;
                boolean isSupportByTDA = false;

                if ((tmProfile.getTdatm() != null)
                        && (tmProfile.getTdatm().getEnable() == 1))
                {
                    isSupportByTDA = true;
                }

                if (!hasLeverage && !tuvGotChanged && isSupportByTDA
                        && !newTuv.isLocalizable()
                        && maxScoreNum < tmProfileThreshold)
                {
                    needHitTDA = true;
                    needHitTDAMap.put(tu, newTuv);

                    // first add all tuv into the map, after leveraging TDA,
                    // then remove the tda matched
                    // results from the array.
                    notGetTdaMap.put(tu, newTuv);
                }

                // Hit MT when maxScoreNum < threshold in tm profile.
                boolean needHitMT = false;
                if (!hasLeverage && !tuvGotChanged && isLocalePairSupportedByMT
                        && !newTuv.isLocalizable()
                        && maxScoreNum < tmProfileThreshold
                        && maxAltTransScore < tmProfileThreshold)
                {
                    needHitMT = true;
                    // All Tuvs that need hit MT are stored in this map, if TDA
                    // TM is used,
                    // after leveraging TDA, then remove the tda matched results
                    // from the array.
                    needHitMTTuTuvMap.put(tu, newTuv);
                }

                if (!needHitMT && !needHitTDA)
                {
                    tu.addTuv(newTuv);
                    result.add(newTuv);
                }
            }// iterator loop end

            // begin to deal with the TDA results
            if (needHitTDAMap != null && needHitTDAMap.size() > 0)
            {
                TdaHelper tdaHelper = new TdaHelper();
                String directory = AmbFileStoragePathUtils
                        .getFileStorageDirPath();

                String filepath = directory + File.separator + "TDA"
                        + File.separator + p_targetLocale;
                File originalPath = new File(filepath + File.separator
                        + "original");

                if (!originalPath.exists())
                {
                    originalPath.mkdirs();
                }

                File resultPath = new File(filepath + File.separator
                        + "TDAResult");

                if (!resultPath.exists())
                {
                    resultPath.mkdirs();
                }

                // Generate the xliff file that send to TDA server
                File temp = new File(originalPath, p_sourcePage.getId()
                        + ".xlf");

                FileOutputStream fos = new FileOutputStream(temp);
                BufferedOutputStream bos = new BufferedOutputStream(fos, 4096);
                OutputStreamWriter osw = new OutputStreamWriter(bos,
                        FileUtil.UTF8);
                tdaHelper.WriteTDAXliffFile(osw, p_sourceLocale.toString(),
                        p_targetLocale.toString(), needHitTDAMap);
                osw.flush();
                osw.close();
                bos.close();
                fos.close();

                // package the xliff file into zip file
                String strZipName = filepath + File.separator + "original"
                        + File.separator + p_sourcePage.getId() + ".zip";
                ZipOutputStream zipout = new ZipOutputStream(
                        new FileOutputStream(strZipName));

                FileInputStream xliffStream = new FileInputStream(temp);
                zipout
                        .putNextEntry(new ZipEntry(p_sourcePage.getId()
                                + ".xlf"));
                byte[] buffer = new byte[1024];
                int len;

                while ((len = xliffStream.read(buffer)) > 0)
                {
                    zipout.write(buffer, 0, len);
                }

                zipout.closeEntry();
                zipout.flush();
                zipout.close();

                File zipFile = new File(strZipName);

                tdaHelper.leverageTDA(tmProfile.getTdatm(), zipFile, filepath
                        + File.separator + "TDAResult", p_sourcePage.getId()
                        + ".xlf", p_sourceLocale.toString().replace("_", "-"),
                        p_targetLocale.toString().replace("_", "-"),
                        tmProfileThreshold);

                File leverageFile = new File(filepath + File.separator
                        + "TDAResult" + File.separator + p_sourcePage.getId()
                        + ".xlf");

                if (leverageFile.exists())
                {
                    FileInputStream file = new FileInputStream(leverageFile);
                    ArrayList matchList = tdaHelper.extract(file,
                            tmProfileThreshold);

                    if (matchList != null && matchList.size() > 0)
                    {
                        for (int i = 0; i < matchList.size(); i++)
                        {
                            Collection c = new ArrayList();
                            TuvManager tuvMgr = ServerProxy.getTuvManager();

                            LeverageTDAResult tdaResult = (LeverageTDAResult) matchList
                                    .get(i);
                            Tu currentTu = tuvMgr
                                    .getTuForSegmentEditor(tdaResult.getTuid());
                            Tuv newTuv = (Tuv) needHitTDAMap.get(currentTu);

                            // save xliff target into "leverage_match"
                            Tuv sourceTuv = (Tuv) p_sourceTuvMap
                                    .getTuv(currentTu);

                            LeverageMatch lm = new LeverageMatch();
                            lm.setSourcePageId(p_sourcePage.getIdAsLong());
                            lm.setOriginalSourceTuvId(sourceTuv.getIdAsLong());
                            lm.setSubId("0");
                            // save without tags mt match into "leverage_match",
                            // save only if string contains tags
                            String startTag = newTuv.getGxmlElement()
                                    .getStartTag();
                            String endTag = newTuv.getGxmlElement().getEndTag();

                            String tmpSegment = startTag
                                    + tdaResult.getResultText() + endTag;
                            lm.setMatchedText(tmpSegment);
                            lm.setMatchedClob(null);
                            lm.setTargetLocale(newTuv.getGlobalSightLocale());
                            lm.setOrderNum((short) tdaResult.getOrderNum());
                            lm
                                    .setScoreNum((float) TdaHelper
                                            .PecentToDouble(tdaResult
                                                    .getMatchPercent()));
                            lm.setMatchType("TDA_MATCH");
                            lm.setMatchedTuvId(-1);
                            lm.setProjectTmIndex(Leverager.TDA_TM_PRIORITY);
                            lm.setTmId(0);
                            lm.setTmProfileId(tmProfile.getIdAsLong());
                            lm.setMtName(null);
                            lm.setMatchedOriginalSource(tdaResult
                                    .getSourceText());
                            c.add(lm);
                            LingServerProxy.getLeverageMatchLingManager()
                                    .saveLeveragedMatches(c);

                            newTuv.setMatchType("TDA_MATCH");
                            newTuv.setLastModifiedUser(IFormatNames.FORMAT_TDA);
                            newTuv.setState(TuvState.NOT_LOCALIZED);
                            currentTu.addTuv(newTuv);
                            result.add(newTuv);

                            // if matched for TDA, remove it from
                            // needHitMTTuTuvMap,
                            // so it will not be leveraged by MT
                            if (needHitMTTuTuvMap != null
                                    && needHitMTTuTuvMap.size() > 0)
                            {
                                needHitMTTuTuvMap.remove(currentTu);
                            }

                            // if matched for TDA, remove it from notGetTdaMap,
                            // then at last
                            // the array only leave the not matched segment tuv.
                            notGetTdaMap.remove(currentTu);
                        }
                    }

                }
            }

            // if not hit MT, and not generate TDA result file, add all
            // the tuv into result array.
            if (needHitMTTuTuvMap == null || needHitMTTuTuvMap.size() == 0)
            {
                Iterator it = notGetTdaMap.entrySet().iterator();
                while (it.hasNext())
                {
                    Map.Entry entry = (Map.Entry) it.next();
                    Tu tukey = (Tu) entry.getKey();
                    Tuv tuvValue = (Tuv) entry.getValue();
                    tukey.addTuv(tuvValue);
                    result.add(tuvValue);
                }
            }
            else
            {
                XmlEntities xe = new XmlEntities();

                // put all TUs into array.
                Object[] key_tus = needHitMTTuTuvMap.keySet().toArray();
                Tu[] tusInArray = new Tu[key_tus.length];
                for (int key = 0; key < key_tus.length; key++)
                {
                    tusInArray[key] = (Tu) key_tus[key];
                }
                // put all TUVs into array
                Object[] value_tuvs = needHitMTTuTuvMap.values().toArray();
                Tuv[] tuvsInArray = new Tuv[value_tuvs.length];
                for (int value = 0; value < value_tuvs.length; value++)
                {
                    tuvsInArray[value] = (Tuv) value_tuvs[value];
                }
                // put all GXML into array
                String[] p_segments = new String[tuvsInArray.length];
                String[] p_segmentsWithoutTags = new String[tuvsInArray.length];
                for (int index = 0; index < tuvsInArray.length; index++)
                {
                    String segment = tuvsInArray[index].getGxml();
                    TuvImpl tuv = new TuvImpl();
                    if (p_sourcePage.getExternalPageId().endsWith(".idml"))
                    {
                        segment = IdmlHelper.formatForOfflineDownload(segment);
                    }

                    tuv.setSegmentString(segment);
                    p_segments[index] = segment;

                    // Asia Online text value can't include "<" and ">"
                    String textValue = tuv.getGxmlElement().getTextValue();
                    String engineName = m_machineTranslator.getEngineName();
                    if (engineName != null
                            && engineName
                                    .equalsIgnoreCase(MachineTranslator.ENGINE_ASIA_ONLINE))
                    {
                        textValue = xe.encodeStringBasic(textValue);
                    }
                    p_segmentsWithoutTags[index] = textValue;
                }

                // Send all segments to MT engine for translation.
                s_logger.info("Begin to hit "
                        + m_machineTranslator.getEngineName()
                        + "(Segment number:" + p_segments.length
                        + "; SourcePageID:" + p_sourcePage.getIdAsLong()
                        + "; TargetLocale:"
                        + p_targetLocale.getLocale().getLanguage() + ").");
                String[] translatedSegments = m_machineTranslator
                        .translateBatchSegments(p_sourceLocale.getLocale(),
                                p_targetLocale.getLocale(), p_segments,
                                LeverageMatchType.CONTAINTAGS);

                String[] translatedSegmentsWithoutTags = null;
                // PROMT won't get full text translation
                if (!"ProMT".equalsIgnoreCase(m_machineTranslator
                        .getEngineName()))
                {
                    translatedSegmentsWithoutTags = m_machineTranslator
                            .translateBatchSegments(p_sourceLocale.getLocale(),
                                    p_targetLocale.getLocale(),
                                    p_segmentsWithoutTags,
                                    LeverageMatchType.WITHOUTTAGS);
                }
                s_logger.info("End hit " + m_machineTranslator.getEngineName()
                        + "(SourcePageID:" + p_sourcePage.getIdAsLong()
                        + "; TargetLocale:"
                        + p_targetLocale.getLocale().getLanguage() + ").");
                // handle translate result one by one.
                for (int tuvIndex = 0; tuvIndex < tuvsInArray.length; tuvIndex++)
                {
                    Tu currentTu = tusInArray[tuvIndex];
                    Tuv sourceTuv = (Tuv) p_sourceTuvMap.getTuv(currentTu);
                    Tuv currentNewTuv = tuvsInArray[tuvIndex];

                    boolean isGetMTResult = false;
                    String machineTranslatedGxml = null;
                    if (translatedSegments != null
                            && translatedSegments.length == tuvsInArray.length)
                    {
                        machineTranslatedGxml = translatedSegments[tuvIndex];
                    }

                    if (machineTranslatedGxml != null
                            && !"".equals(machineTranslatedGxml)
                            && !"".equals(GxmlUtil.stripRootTag(
                                    machineTranslatedGxml).trim()))
                    // && !machineTranslatedGxml.equals(currentNewTuv.getGxml())
                    {
                        // Perhaps the MT results include nothing except for
                        // tags
                        Tuv copyTuv = new TuvImpl((TuvImpl) currentNewTuv);
                        copyTuv.setGxml(machineTranslatedGxml);
                        String textValue = 
                            copyTuv.getGxmlElement().getTextValue();
                        if (!"".equals(textValue.trim()))
                        {
                            isGetMTResult = true;
                        }
                    }

                    // replace the content in target tuv with mt result
                    if ((m_autoCommitMtToTm || mtThreshold == 100)
                            && isGetMTResult == true)
                    {
                        currentNewTuv.setGxml(machineTranslatedGxml);
                        currentNewTuv
                                .setMatchType(LeverageMatchType.UNKNOWN_NAME);
                        currentNewTuv.setLastModifiedUser(
                                m_machineTranslator.getEngineName() + "_MT");
                    }
                    if (m_autoCommitMtToTm && isGetMTResult == true)
                    {
                        // mark TUVs as localized so they get committed to the
                        // TM
                        TuvImpl t = (TuvImpl) currentNewTuv;
                        t.setState(com.globalsight.everest.tuv.TuvState.LOCALIZED);
                    }

                    // save MT match into "leverage_match"
                    if (isGetMTResult == true)
                    {
                        Collection c = new ArrayList();
                        LeverageMatch lm = new LeverageMatch();
                        lm.setSourcePageId(p_sourcePage.getIdAsLong());

                        lm.setOriginalSourceTuvId(sourceTuv.getIdAsLong());
                        lm.setSubId("0");
                        lm.setMatchedText(machineTranslatedGxml);
                        lm.setMatchedClob(null);
                        lm.setTargetLocale(currentNewTuv.getGlobalSightLocale());
                        // This is the first MT matches,its order number is 301.
                        lm.setOrderNum((short) TmCoreManager.LM_ORDER_NUM_START_MT);
                        lm.setScoreNum(mtThreshold);
                        if (m_autoCommitMtToTm || mtThreshold == 100)
                        {
                            // Add "MT_EXACT_MATCH" as one new MatchState --
                            // SEGMENT_TM_EXACT_MATCH
                            lm.setMatchType("MT_EXACT_MATCH");
                        }
                        else
                        {
                            lm.setMatchType("FUZZY_MATCH");
                        }
                        lm.setMatchedTuvId(-1);
                        lm.setProjectTmIndex(Leverager.MT_PRIORITY);
                        lm.setTmId(0);
                        lm.setTmProfileId(tmProfile.getIdAsLong());
                        lm.setMtName(m_machineTranslator.getEngineName()
                                + "_MT");
                        lm.setMatchedOriginalSource(sourceTuv.getGxml());
                        c.add(lm);

                        LingServerProxy.getLeverageMatchLingManager()
                                .saveLeveragedMatches(c);
                    }

                    // save without tags mt match into "leverage_match", save
                    // only if string contains tags
                    
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
                    
                    if (hasTagInSource && translatedSegmentsWithoutTags != null
                            && translatedSegmentsWithoutTags.length == tuvsInArray.length)
                    {
                        String startTag = tuvsInArray[tuvIndex]
                                .getGxmlElement().getStartTag();
                        String endTag = tuvsInArray[tuvIndex].getGxmlElement()
                                .getEndTag();

                        // encode is needed here to convert tags (', <, > and
                        // etc) in sentence
                        String matchString = translatedSegmentsWithoutTags[tuvIndex];
                        String target = startTag
                                + xe.encodeStringBasic(matchString) + endTag;
                        String origin = startTag
                                + xe.encodeStringBasic(p_segmentsWithoutTags[tuvIndex])
                                + endTag;

                        if (!"".equals(matchString.trim())
                                && !target.equals(origin))
                        {
                            Collection c = new ArrayList();
                            LeverageMatch lm = new LeverageMatch();
                            lm.setSourcePageId(p_sourcePage.getIdAsLong());
                            lm.setOriginalSourceTuvId(sourceTuv.getIdAsLong());
                            lm.setSubId("0");
                            lm.setMatchedText(target);
                            lm.setMatchedClob(null);
                            lm.setTargetLocale(currentNewTuv
                                    .getGlobalSightLocale());
                            // This is the second MT matches,its order number is
                            // 302.
                            lm.setOrderNum((short) (TmCoreManager.LM_ORDER_NUM_START_MT + 1));
                            lm.setScoreNum(mtThreshold);
                            if (m_autoCommitMtToTm || mtThreshold == 100)
                            {
                                // Add "MT_EXACT_MATCH" as one new MatchState --
                                // SEGMENT_TM_EXACT_MATCH
                                lm.setMatchType("MT_EXACT_MATCH");
                            }
                            else
                            {
                                lm.setMatchType("FUZZY_MATCH");
                            }
                            lm.setMatchedTuvId(-1);
                            lm.setProjectTmIndex(Leverager.MT_PRIORITY);
                            lm.setTmId(0);
                            lm.setTmProfileId(tmProfile.getIdAsLong());
                            lm.setMtName(m_machineTranslator.getEngineName()
                                    + "_MT");
                            lm.setMatchedOriginalSource(sourceTuv.getGxml());
                            c.add(lm);

                            LingServerProxy.getLeverageMatchLingManager()
                                    .saveLeveragedMatches(c);
                        }
                    }

                    // reset newTuv in result
                    currentTu.addTuv(currentNewTuv);
                    result.add(currentNewTuv);
                }

            }
        }
        catch (Exception e)
        {
            throw new PageException(e);
        }

        if (s_logger.isDebugEnabled())
        {
            s_logger.debug("Ending useLeveragedSegments() at " + new Date());
        }

        return result;
    }

    private float getTMScore(TuImpl tu)
    {
        float result = -1;

        if (tu.hasXliffTMScoreStr())
        {
            try
            {
                result = Float.valueOf(tu.getIwsScore());
            }
            catch (Exception e)
            {
            }
        }
        
        return result;
    }

    private PseudoData toPsedoData(String s)
            throws DiplomatBasicParserException
    {
        PseudoCodec codec = new PseudoCodec();
        s = codec.encode(s);
        PseudoData pseudoData = new PseudoData();
        pseudoData.setMode(PseudoConstants.PSEUDO_COMPACT);
        Tmx2PseudoHandler eventHandler = new Tmx2PseudoHandler(pseudoData);
        s = eventHandler.preProcessInternalText(s);
        DiplomatBasicParser parser = new DiplomatBasicParser(eventHandler);
        parser.parse(s);
        pseudoData = eventHandler.getResult();
        return eventHandler.getResult();
    }

    private boolean canBeModified(Tuv tuv, LeverageSegment leverageSegment)
            throws DiplomatBasicParserException, PseudoParserException
    {
        PseudoData pseudoData = toPsedoData(tuv.getGxmlExcludeTopTags());
        pseudoData.setPTagTargetString(toPsedoData(
                GxmlUtil.stripRootTag(leverageSegment.getSegment()))
                .getPTagSourceString());
        PseudoErrorChecker checker = new PseudoErrorChecker();

        return checker.check(pseudoData, tuv.getGxmlExcludeTopTags(), 0,
                "utf8", 0, "utf8") == null;
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
                s_logger
                        .error("Exception when getting the gxml for a leveraged segment.");
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

    /**
     * Machine translates the given Tuv's segment, and sets the translation into
     * the tuv. If MT cannot be performed, then the TUV is left unchanged, and
     * an error is written out to the log file. The Tuv's leverage type is set
     * to UNKNOWN_NAME
     * 
     * @param p_sourceLocale
     * @param p_targetLocale
     * @param p_newTuv
     */
    private Tuv machineTranslateSegment(GlobalSightLocale p_sourceLocale,
            GlobalSightLocale p_targetLocale, Tuv p_newTuv, long p_mtThreshold)
    {
        try
        {
            if (s_logger.isDebugEnabled())
            {
                s_logger.debug("Before MT: " + p_newTuv.getGxml());
            }

            String machineTranslatedGxml = m_machineTranslator
                    .translateSegment(p_sourceLocale.getLocale(),
                            p_targetLocale.getLocale(), p_newTuv.getGxml());

            if (s_logger.isDebugEnabled())
            {
                s_logger.debug("After MT: " + machineTranslatedGxml);
            }
            // replace the content in target tuv with mt result
            if (m_autoCommitMtToTm || p_mtThreshold == 100)
            {
                p_newTuv.setGxml(machineTranslatedGxml);
                p_newTuv.setMatchType(LeverageMatchType.UNKNOWN_NAME);
                p_newTuv.setLastModifiedUser(m_machineTranslator
                        .getEngineName());
            }
            if (m_autoCommitMtToTm)
            {
                // mark tuvs as localized so they get committed to the TM
                TuvImpl t = (TuvImpl) p_newTuv;
                t.setState(com.globalsight.everest.tuv.TuvState.LOCALIZED);
            }
        }
        catch (Exception e)
        {
            s_logger.error("Failed to machine translate segment "
                    + p_newTuv.getId(), e);
        }

        return p_newTuv;
    }

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
        if (m_machineTranslator == null)
        {
            return false;
        }

        boolean isSupported = false;

        try
        {
            isSupported = m_machineTranslator.supportsLocalePair(p_sourceLocale
                    .getLocale(), p_targetLocale.getLocale());
        }
        catch (Exception e)
        {
            s_logger.error("Failed to find if locale pair ("
                    + p_sourceLocale.getLocale() + "->"
                    + p_targetLocale.getLocale() + " is supported by MT "
                    + m_machineTranslator.getEngineName());
        }

        if (isSupported)
        {
            if (s_logger.isDebugEnabled())
            {
                s_logger
                        .info("Using machine translation to convert non-exact matches for locale pair "
                                + p_sourceLocale.getLocale()
                                + "->"
                                + p_targetLocale.getLocale()
                                + " with MT engine "
                                + m_machineTranslator.getEngineName());
            }

            if (m_autoCommitMtToTm && s_logger.isDebugEnabled())
            {
                s_logger.info("Automatically committing MT results to TM.");
            }
        }
        else
        {
            s_logger
                    .warn("Machine translation is desired, but not supported for locale pair "
                            + p_sourceLocale.getLocale()
                            + "->"
                            + p_targetLocale.getLocale()
                            + " for MT engine "
                            + m_machineTranslator.getEngineName());
        }

        return isSupported;
    }

    /**
     * 
     */
    private Tu2TuvMap getSourceTuvMap(SourcePage p_sourcePage)
    {
        Tu2TuvMap result = new Tu2TuvMap();

        // Assumes this page contains an extracted file, otherwise
        // wouldn't have reached this place in the code.
        Iterator it1 = getExtractedFile(p_sourcePage).getLeverageGroups()
                .iterator();
        while (it1.hasNext())
        {
            LeverageGroup leverageGroup = (LeverageGroup) it1.next();
            Collection tus = leverageGroup.getTus();

            for (Iterator it2 = tus.iterator(); it2.hasNext();)
            {
                Tu tu = (Tu) it2.next();
                Tuv tuv = tu.getTuv(p_sourcePage.getLocaleId());

                result.putTu2Tuv(tu, tuv);
            }
        }

        return result;
    }

    private TuvManager getTuvManager() throws Exception
    {
        return ServerProxy.getTuvManager();
    }

    /**
     * Returns the page's Extracted Primary File or NULL if it doesn't contain
     * an Extracted file.
     */
    private ExtractedFile getExtractedFile(SourcePage p_page)
    {
        ExtractedFile result = null;

        if (p_page.getPrimaryFileType() == ExtractedFile.EXTRACTED_FILE)
        {
            result = (ExtractedFile) p_page.getPrimaryFile();
        }

        return result;
    }

    private boolean compareTags(String str1, String str2)
    {
        Map str1Map = null;
        Map str2Map = null;
        try
        {
            str1Map = convertSegment2Pseudo(str1);
            str2Map = convertSegment2Pseudo(str2);
        }
        catch (Exception ex)
        {
            return false;
        }

        boolean hasSameTags = true;
        List str1Keys = null;
        if (str1Map != null)
        {
            str1Keys = new ArrayList(str1Map.keySet());
        }
        List str2Keys = null;
        if (str2Map != null)
        {
            str2Keys = new ArrayList(str2Map.keySet());
        }

        int str1Size = 0;
        int str2Size = 0;
        if (str1Keys != null)
        {
            str1Size = str1Keys.size();
        }
        if (str2Keys != null)
        {
            str2Size = str2Keys.size();
        }

        if (str1Size != str2Size)
        {
            hasSameTags = false;
        }
        else if (str1Size == 0)
        {
            // do nothing
        }
        else
        {
            for (int i = 0; i < str1Keys.size(); i++)
            {
                String key1 = (String) str1Keys.get(i);
                if (!str2Keys.contains(key1))
                {
                    hasSameTags = false;
                }
            }
        }

        return hasSameTags;
    }

    private Map convertSegment2Pseudo(String textContent)
            throws DiplomatBasicParserException
    {
        if (textContent == null || "".equals(textContent.trim()))
        {
            return null;
        }

        PseudoData PTagData = null;
        // Create PTag resources
        PTagData = new PseudoData();
        PTagData.setMode(2);

        // configure addable ptags for this format
        PTagData.setAddables("html");

        // convert the current source text and
        // set the native map to represent source tags
        textContent = XliffInternalTag.revertXliffInternalText(textContent);
        TmxPseudo.tmx2Pseudo(textContent, PTagData);

        return PTagData.getPseudo2TmxMap();
    }

    /**
     * To invoke MT, extra parameters must be transformed to MT engine for
     * "PROMT","MS_TRANSLATOR" or "ASIA_ONLINE".
     * 
     * @param p_tmProfile
     */
    private void setExtraOptionsForMT(TranslationMemoryProfile p_tmProfile,
            SourcePage p_sourcePage)
    {
        HashMap paramHM = new HashMap();

        // Set extra parameters for PROMT engine
        if (m_machineTranslator != null
                && m_machineTranslator.getEngineName().equalsIgnoreCase(
                        MachineTranslator.ENGINE_PROMT))
        {
            Long tmProfileID = p_tmProfile.getIdAsLong();
            String ptsurl = p_tmProfile.getPtsurl();
            String ptsUsername = p_tmProfile.getPtsUsername();
            String ptsPassword = p_tmProfile.getPtsPassword();

            paramHM.put(MachineTranslator.TM_PROFILE_ID, tmProfileID);
            paramHM.put(MachineTranslator.PROMT_PTSURL, ptsurl);
            paramHM.put(MachineTranslator.PROMT_USERNAME, ptsUsername);
            paramHM.put(MachineTranslator.PROMT_PASSWORD, ptsPassword);
            paramHM.put(MachineTranslator.SOURCE_PAGE_ID, p_sourcePage
                    .getIdAsLong());

            m_machineTranslator.setMtParameterMap(paramHM);
        }

        // Set extra parameters for MS_Translator engine
        if (m_machineTranslator != null
                && m_machineTranslator.getEngineName().equalsIgnoreCase(
                        MachineTranslator.ENGINE_MSTRANSLATOR))
        {
            String msMtEndPoint = p_tmProfile.getMsMTUrl();
            String msMtAppId = p_tmProfile.getMsMTAppID();
            String msMtUrlFlag = p_tmProfile.getMsMTUrlFlag();

            paramHM.put(MachineTranslator.MSMT_ENDPOINT, msMtEndPoint);
            paramHM.put(MachineTranslator.MSMT_APPID, msMtAppId);
            paramHM.put(MachineTranslator.MSMT_URLFLAG, msMtUrlFlag);

            m_machineTranslator.setMtParameterMap(paramHM);
        }

        // Set extra parameters for Asia Online MT engine
        if (m_machineTranslator != null
                && m_machineTranslator.getEngineName().equalsIgnoreCase(
                        MachineTranslator.ENGINE_ASIA_ONLINE))
        {
            Long tmProfileID = p_tmProfile.getIdAsLong();
            String aoMtUrl = p_tmProfile.getAoMtUrl();
            long aoMtPort = p_tmProfile.getAoMtPort();
            String aoMtUserName = p_tmProfile.getAoMtUsername();
            String aoMtPassword = p_tmProfile.getAoMtPassword();
            long aoMtAccountNumber = p_tmProfile.getAoMtAccountNumber();

            paramHM.put(MachineTranslator.TM_PROFILE_ID, tmProfileID);
            paramHM.put(MachineTranslator.SOURCE_PAGE_ID, p_sourcePage
                    .getIdAsLong());
            paramHM.put(MachineTranslator.AO_URL, aoMtUrl);
            paramHM.put(MachineTranslator.AO_PORT, (Long) aoMtPort);
            paramHM.put(MachineTranslator.AO_USERNAME, aoMtUserName);
            paramHM.put(MachineTranslator.AO_PASSWORD, aoMtPassword);
            paramHM.put(MachineTranslator.AO_ACCOUNT_NUMBER,
                    (Long) aoMtAccountNumber);

            m_machineTranslator.setMtParameterMap(paramHM);
        }
    }

}
